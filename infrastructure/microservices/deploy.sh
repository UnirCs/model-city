#!/usr/bin/env bash
#
# Model City v2 — Unified Deployment Script
#
# Single ALB architecture:
#   - Port 443 (HTTPS, no mTLS): /* → frontend, /api/* → gateway
#   - Port 8443 (HTTPS, mTLS):  /*  → gateway (FNMT flows)
#   - Port 80: redirect to 443
#
# Operations:
#   1) Initial Deployment  — terraform + DB + backend + frontend build & deploy
#   2) Redeploy Services   — rebuild + redeploy selected service(s)
#   3) Cleanup             — destroy all AWS resources
#
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TERRAFORM_DIR="$SCRIPT_DIR/terraform/aws"
AWS_REGION="${AWS_REGION:-us-east-1}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

# Project directories — resolved during deployment from Terraform variables
# (backend_project_dir / frontend_project_dir).
BACKEND_DIR=""
FRONTEND_DIR=""

log_info()    { echo -e "${BLUE}>>>${NC} $*"; }
log_success() { echo -e "${GREEN}✓${NC} $*"; }
log_warn()    { echo -e "${YELLOW}⚠${NC} $*"; }
log_error()   { echo -e "${RED}✗${NC} $*"; }

die() {
  log_error "$*"
  exit 1
}

confirm() {
  local prompt="$1"
  read -p "$(echo -e "${YELLOW}${prompt}${NC} [y/N]: ")" -r
  [[ $REPLY =~ ^[Yy]$ ]]
}

banner() {
  local subtitle="${1:-}"
  echo
  printf '%b' "$GREEN"
  cat <<'EOF'
███╗   ███╗ ██████╗ ██████╗ ███████╗██╗      ██████╗██╗████████╗██╗   ██╗
████╗ ████║██╔═══██╗██╔══██╗██╔════╝██║     ██╔════╝██║╚══██╔══╝╚██╗ ██╔╝
██╔████╔██║██║   ██║██║  ██║█████╗  ██║     ██║     ██║   ██║    ╚████╔╝
██║╚██╔╝██║██║   ██║██║  ██║██╔══╝  ██║     ██║     ██║   ██║     ╚██╔╝
██║ ╚═╝ ██║╚██████╔╝██████╔╝███████╗███████╗╚██████╗██║   ██║      ██║
╚═╝     ╚═╝ ╚═════╝ ╚═════╝ ╚══════╝╚══════╝ ╚═════╝╚═╝   ╚═╝      ╚═╝
EOF
  printf '%b' "$NC"
  [[ -n "$subtitle" ]] && echo -e "${GREEN}$subtitle${NC}"
  echo
}

section() {
  echo
  echo -e "${GREEN}>> $*${NC}"
  echo
}

# ============================================================================
# PREREQUISITES
# ============================================================================

check_prerequisites() {
  log_info "Checking prerequisites..."

  for cmd in aws terraform docker mvn jq; do
    if ! command -v "$cmd" &> /dev/null; then
      die "Required command not found: $cmd"
    fi
  done

  log_success "All prerequisites found"
}

check_aws_credentials() {
  log_info "Verifying AWS credentials..."

  if ! aws sts get-caller-identity &> /dev/null; then
    die "AWS credentials not configured or expired. Update ~/.aws/credentials from AWS Academy."
  fi

  local account
  account=$(aws sts get-caller-identity --query Account --output text)
  log_success "AWS authenticated (Account: $account)"
}

check_ecr_access() {
  log_info "Checking ECR access..."

  local ecr_error
  ecr_error=$(aws ecr get-authorization-token --region "$AWS_REGION" 2>&1) || true

  if echo "$ecr_error" | grep -q "voc-cancel-cred\|explicit deny\|AccessDeniedException"; then
    echo
    echo -e "${RED}════════════════════════════════════════════════════${NC}"
    echo -e "${RED}  AWS Academy credentials have EXPIRED${NC}"
    echo -e "${RED}════════════════════════════════════════════════════${NC}"
    echo
    echo "The Vocareum lab session has ended and ECR access is blocked."
    echo
    echo -e "${YELLOW}To fix this:${NC}"
    echo "  1. Go to https://labs.vocareum.com (AWS Academy portal)"
    echo "  2. Click 'Start Lab' to start a new session"
    echo "  3. Click 'AWS Details' → 'Show'"
    echo "  4. Copy and paste into: ~/.aws/credentials"
    echo "  5. Re-run this script"
    echo
    echo -e "${RED}════════════════════════════════════════════════════${NC}"
    exit 1
  fi

  log_success "ECR access confirmed"
}

# ============================================================================
# PROJECT LOCATIONS
# ============================================================================

resolve_backend_dir() {
  [[ -n "$BACKEND_DIR" ]] && return   # resolve once per run

  # Try to read from Terraform output first
  cd "$TERRAFORM_DIR"
  local tf_backend_dir
  tf_backend_dir=$(terraform output -raw backend_project_dir 2>/dev/null || true)
  cd - > /dev/null

  if [[ -n "$tf_backend_dir" ]]; then
    BACKEND_DIR="$tf_backend_dir"
    log_info "Backend project: $BACKEND_DIR (from Terraform output)"
  elif [[ -n "${BACKEND_PROJECT_DIR:-}" ]]; then
    BACKEND_DIR="$BACKEND_PROJECT_DIR"
    log_info "Backend project: $BACKEND_DIR (from BACKEND_PROJECT_DIR env var)"
  else
    echo
    echo -e "${BLUE}Backend project location${NC}"
    echo "The Spring Boot backend lives in a separate repository."
    echo "Set backend_project_dir in terraform.tfvars to skip this prompt."
    echo
    read -e -p "$(echo -e "${YELLOW}Enter the absolute path to the backend project:${NC} ")" -r backend_input
    BACKEND_DIR="${backend_input/#\~/$HOME}"
  fi

  if [[ ! -d "$BACKEND_DIR" ]]; then
    die "Backend directory not found: $BACKEND_DIR"
  fi

  if [[ ! -f "$BACKEND_DIR/pom.xml" ]]; then
    die "No pom.xml found in $BACKEND_DIR — expected the Maven backend project root."
  fi

  log_success "Backend project found: $BACKEND_DIR"
}

resolve_frontend_dir() {
  # Try to read from Terraform output first
  cd "$TERRAFORM_DIR"
  local tf_frontend_dir
  tf_frontend_dir=$(terraform output -raw frontend_project_dir 2>/dev/null || true)
  cd - > /dev/null

  if [[ -n "$tf_frontend_dir" ]]; then
    FRONTEND_DIR="$tf_frontend_dir"
    log_info "Frontend project: $FRONTEND_DIR (from Terraform output)"
  elif [[ -n "${FRONTEND_PROJECT_DIR:-}" ]]; then
    FRONTEND_DIR="$FRONTEND_PROJECT_DIR"
    log_info "Frontend project: $FRONTEND_DIR (from FRONTEND_PROJECT_DIR env var)"
  else
    echo
    echo -e "${BLUE}Frontend project location${NC}"
    echo "The Next.js frontend is expected to be in a separate repository."
    echo "Set frontend_project_dir in terraform.tfvars to skip this prompt."
    echo
    read -e -p "$(echo -e "${YELLOW}Enter the absolute path to the frontend project:${NC} ")" -r frontend_input
    FRONTEND_DIR="${frontend_input/#\~/$HOME}"
  fi

  if [[ ! -d "$FRONTEND_DIR" ]]; then
    die "Frontend directory not found: $FRONTEND_DIR"
  fi

  log_success "Frontend project found: $FRONTEND_DIR"

  if [[ ! -f "$FRONTEND_DIR/Dockerfile" ]]; then
    echo
    log_warn "No Dockerfile found in $FRONTEND_DIR"
    echo
    echo "The deploy script expects a production Dockerfile at the root of the Next.js project."
    echo "A typical Next.js standalone Dockerfile:"
    echo
    cat <<'DOCKERFILE'
  FROM node:20-alpine AS builder
  WORKDIR /app
  COPY package*.json ./
  RUN npm ci
  COPY . .
  RUN npm run build

  FROM node:20-alpine AS runner
  WORKDIR /app
  ENV NODE_ENV=production
  COPY --from=builder /app/.next/standalone ./
  COPY --from=builder /app/.next/static ./.next/static
  COPY --from=builder /app/public ./public
  EXPOSE 3000
  CMD ["node", "server.js"]
DOCKERFILE
    echo
    echo "Also add to next.config.js:  output: 'standalone'"
    echo
    if ! confirm "Continue without a Dockerfile? (will fail at docker build step)"; then
      die "Aborted. Add a Dockerfile to the frontend project and retry."
    fi
  fi
}

# ============================================================================
# TERRAFORM
# ============================================================================

terraform_apply() {
  log_info "Initializing and applying Terraform..."

  cd "$TERRAFORM_DIR"

  terraform init || die "Terraform init failed"

  log_warn "This will create AWS resources (VPC, RDS, ALB, ECS, ECR, etc.)"
  log_warn "Estimated time: 5-10 minutes"
  echo

  terraform apply -auto-approve || die "Terraform apply failed"

  log_success "Terraform infrastructure created"
}

terraform_destroy() {
  log_info "Destroying all AWS resources..."

  cd "$TERRAFORM_DIR"

  echo -e "\n${RED}=== WARNING ===${NC}"
  echo "This will PERMANENTLY DELETE:"
  echo "  - ECS services, tasks, CloudWatch logs"
  echo "  - Single ALB, Target Groups, Security Groups"
  echo "  - RDS PostgreSQL instance (DATA LOSS)"
  echo "  - ECR repositories (IMAGES LOSS)"
  echo "  - ElastiCache Valkey replication group (if created)"
  echo "  - VPC, subnets, NAT Gateway, IGW"
  echo
  echo "This action CANNOT be undone."
  echo

  local attempt=0
  local max_attempts=3

  while [[ $attempt -lt $max_attempts ]]; do
    attempt=$((attempt + 1))
    log_info "Destroy attempt $attempt/$max_attempts..."

    if terraform destroy -auto-approve; then
      log_success "All resources destroyed"
      return
    fi

    if [[ $attempt -lt $max_attempts ]]; then
      log_warn "Destroy failed — waiting 60s for AWS to release ENI dependencies..."
      sleep 60
    fi
  done

  die "Terraform destroy failed after $max_attempts attempts. Run 'terraform destroy' manually or check the AWS Console for remaining resources."
}

# ============================================================================
# BUILD & DEPLOY — BACKEND
# ============================================================================

build_and_push_images() {
  resolve_backend_dir

  log_info "Building Maven artifacts and Docker images..."

  cd "$BACKEND_DIR"

  local AWS_ACCOUNT_ID
  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
  local REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

  local SERVICES=(
    "service-registry:model-city-service-registry"
    "gateway:model-city-gateway"
    "core:model-city-core"
    "engagement:model-city-engagement"
    "leisure:model-city-leisure"
    "mobility:model-city-mobility"
  )

  log_info "Building Maven artifacts (mvn clean package -DskipTests)..."
  echo
  for entry in "${SERVICES[@]}"; do
    local MODULE="${entry##*:}"
    log_info "Building module: $MODULE"
    mvn -B -pl "$MODULE" -am clean package -DskipTests -q || die "Failed to build $MODULE"
    log_success "Built $MODULE"
  done

  echo
  log_success "All Maven builds completed"
  echo

  log_info "Logging in to ECR ($REGISTRY)"
  aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$REGISTRY"

  echo

  for entry in "${SERVICES[@]}"; do
    local NAME="${entry%%:*}"
    local MODULE="${entry##*:}"
    local IMAGE="${REGISTRY}/modelcity/${NAME}:${IMAGE_TAG}"

    echo
    log_info "Building Docker image: $IMAGE"
    docker build --platform=linux/amd64 -f "${MODULE}/Dockerfile" -t "$IMAGE" . || \
      die "Failed to build Docker image for $MODULE"

    log_info "Pushing $IMAGE"
    docker push "$IMAGE" || die "Failed to push $IMAGE"
    log_success "Pushed $IMAGE"

    log_info "Removing local image to free up disk space..."
    docker rmi "$IMAGE" || log_warn "Failed to remove local image: $IMAGE"
  done

  echo
  log_success "All backend images built and pushed with tag '$IMAGE_TAG'"
  echo
}

build_and_push_selected_images() {
  local SERVICES=("$@")

  resolve_backend_dir

  log_info "Building selected Maven artifacts and Docker images..."

  cd "$BACKEND_DIR"

  local AWS_ACCOUNT_ID
  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
  local REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

  log_info "Building Maven artifacts (mvn clean package -DskipTests)..."
  echo
  for entry in "${SERVICES[@]}"; do
    local MODULE="${entry##*:}"
    log_info "Building module: $MODULE"
    mvn -B -pl "$MODULE" -am clean package -DskipTests -q || die "Failed to build $MODULE"
    log_success "Built $MODULE"
  done

  echo
  log_success "Maven builds completed"
  echo

  log_info "Logging in to ECR ($REGISTRY)"
  aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$REGISTRY"

  echo

  for entry in "${SERVICES[@]}"; do
    local NAME="${entry%%:*}"
    local MODULE="${entry##*:}"
    local IMAGE="${REGISTRY}/modelcity/${NAME}:${IMAGE_TAG}"

    echo
    log_info "Building Docker image: $IMAGE"
    docker build --platform=linux/amd64 -f "${MODULE}/Dockerfile" -t "$IMAGE" . || \
      die "Failed to build Docker image for $MODULE"

    log_info "Pushing $IMAGE"
    docker push "$IMAGE" || die "Failed to push $IMAGE"
    log_success "Pushed $IMAGE"

    log_info "Removing local image to free up disk space..."
    docker rmi "$IMAGE" || log_warn "Failed to remove local image: $IMAGE"
  done

  echo
  log_success "Selected images built and pushed with tag '$IMAGE_TAG'"
  echo
}

# ============================================================================
# BUILD & DEPLOY — FRONTEND
# ============================================================================

build_and_push_frontend() {
  resolve_frontend_dir

  log_info "Building and pushing frontend Docker image..."

  local AWS_ACCOUNT_ID
  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
  local REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
  local IMAGE="${REGISTRY}/modelcity/frontend:${IMAGE_TAG}"

  # Resolve build-time config from Terraform outputs.
  # next.config.mjs maps MICROSERVICE_ALB_URL → NEXT_PUBLIC_MICROSERVICE_ALB_URL
  # and STRIPE_PUBLISHABLE_KEY → NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY internally,
  # so all external env var names are uniform (no NEXT_PUBLIC_ prefix).
  local DOMAIN STRIPE_PK BACKGROUND_IMAGE_URL

  cd "$TERRAFORM_DIR"
  DOMAIN=$(terraform output -raw domain 2>/dev/null || echo "")
  STRIPE_PK=$(terraform output -raw stripe_publishable_key 2>/dev/null || echo "")
  BACKGROUND_IMAGE_URL=$(terraform output -raw background_image_url 2>/dev/null || echo "")
  cd - > /dev/null

  if [[ -z "$DOMAIN" ]]; then
    log_warn "Could not read domain from Terraform outputs. Run 'terraform apply' first, or enter manually."
    read -p "$(echo -e "${YELLOW}Domain (e.g. model-city.example.org):${NC} ")" -r DOMAIN
  fi

  if [[ -z "$STRIPE_PK" ]]; then
    log_warn "stripe_publishable_key is empty in Terraform outputs."
    read -p "$(echo -e "${YELLOW}Stripe publishable key (pk_live_... / pk_test_...) or Enter to skip:${NC} ")" -r STRIPE_PK
  fi

  # mTLS endpoint: browser calls port 8443 for FNMT certificate flows.
  local MICROSERVICE_ALB_URL="https://${DOMAIN}:8443"

  log_info "Build args:"
  echo "  MICROSERVICE_ALB_URL=$MICROSERVICE_ALB_URL"
  echo "  STRIPE_PUBLISHABLE_KEY=${STRIPE_PK:-(empty)}"
  echo "  BACKGROUND_IMAGE_URL=${BACKGROUND_IMAGE_URL:-(empty)}"
  echo

  log_info "Logging in to ECR ($REGISTRY)"
  aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$REGISTRY"

  echo
  log_info "Building Docker image: $IMAGE"
  docker build --platform=linux/amd64 \
    --build-arg MICROSERVICE_ALB_URL="$MICROSERVICE_ALB_URL" \
    --build-arg STRIPE_PUBLISHABLE_KEY="${STRIPE_PK}" \
    --build-arg BACKGROUND_IMAGE_URL="${BACKGROUND_IMAGE_URL}" \
    -t "$IMAGE" \
    "$FRONTEND_DIR" || die "Failed to build frontend Docker image"

  log_info "Pushing $IMAGE"
  docker push "$IMAGE" || die "Failed to push frontend image"

  log_success "Frontend image pushed: $IMAGE"

  log_info "Removing local image to free up disk space..."
  docker rmi "$IMAGE" || log_warn "Failed to remove local image: $IMAGE"
  echo
}

# ============================================================================
# ECS REDEPLOY
# ============================================================================

force_ecs_redeploy() {
  log_info "Forcing ECS service redeployment..."

  local services=(service-registry gateway core engagement leisure mobility frontend)

  for svc in "${services[@]}"; do
    log_info "Redeploying $svc..."
    aws ecs update-service \
      --cluster modelcity-academy-cluster \
      --service "$svc" \
      --force-new-deployment \
      --region "$AWS_REGION" \
      > /dev/null || log_warn "Failed to redeploy service: $svc (may not exist yet)"
  done

  log_success "All services scheduled for redeployment"
}

force_ecs_redeploy_selected() {
  local SERVICES=("$@")

  log_info "Forcing ECS service redeployment for selected services..."

  for entry in "${SERVICES[@]}"; do
    local svc="${entry%%:*}"
    log_info "Redeploying $svc..."
    aws ecs update-service \
      --cluster modelcity-academy-cluster \
      --service "$svc" \
      --force-new-deployment \
      --region "$AWS_REGION" \
      > /dev/null || log_warn "Failed to redeploy service: $svc (may not exist yet)"
  done

  log_success "Selected services scheduled for redeployment"
}

# ============================================================================
# OUTPUTS
# ============================================================================

show_outputs() {
  log_info "Deployment summary..."

  cd "$TERRAFORM_DIR"

  local alb_dns
  alb_dns=$(terraform output -raw alb_dns_name 2>/dev/null || echo "N/A")
  local eureka_dns
  eureka_dns=$(terraform output -raw eureka_alb_dns 2>/dev/null || echo "N/A")
  local rds_endpoints_json
  rds_endpoints_json=$(terraform output -json rds_endpoints 2>/dev/null || echo "{}")
  local cache_endpoint
  cache_endpoint=$(terraform output -raw elasticache_primary_endpoint 2>/dev/null || echo "")
  local domain
  domain=$(terraform output -raw domain 2>/dev/null || echo "N/A")

  local alb_ip="N/A"
  if [[ "$alb_dns" != "N/A" ]]; then
    alb_ip=$(dig +short "$alb_dns" 2>/dev/null | head -n1 || echo "N/A")
  fi

  echo
  echo -e "${GREEN}=== Deployment Complete ===${NC}"
  echo
  echo -e "${BLUE}Single ALB — $domain${NC}"
  echo "  DNS:  $alb_dns"
  echo "  IP:   $alb_ip"
  echo
  echo -e "${BLUE}Listeners:${NC}"
  echo "  443  HTTPS (no mTLS):"
  echo "    /* → Next.js frontend"
  echo "    /api/* → Spring Cloud Gateway"
  echo "  8443 HTTPS (mTLS):  /* → Spring Cloud Gateway"
  echo "  80   HTTP → redirect to 443"
  echo
  echo -e "${BLUE}Internal Eureka:${NC}"
  echo "  http://$eureka_dns:8761"
  echo
  echo -e "${BLUE}RDS Endpoints (database-per-service):${NC}"
  echo "  core:        $(echo "$rds_endpoints_json" | jq -r '.core // "N/A"')"
  echo "  engagement:  $(echo "$rds_endpoints_json" | jq -r '.engagement // "N/A"')"
  echo "  leisure:     $(echo "$rds_endpoints_json" | jq -r '.leisure // "N/A"')"
  echo "  mobility:    $(echo "$rds_endpoints_json" | jq -r '.mobility // "N/A"')"
  echo
  if [[ -n "$cache_endpoint" ]]; then
    echo -e "${BLUE}ElastiCache Valkey:${NC}"
    echo "  $cache_endpoint:6379 (private VPC only)"
    echo
  fi
  echo -e "${BLUE}DNS Configuration:${NC}"
  echo "  Point $domain (A record) to: $alb_ip"
  echo "  or use a CNAME to the ALB DNS name: $alb_dns"
  echo
  echo -e "${BLUE}Monitor services:${NC}"
  echo "  aws ecs list-services --cluster modelcity-academy-cluster --region $AWS_REGION"
  echo "  aws logs tail /ecs/modelcity-academy/<service> --follow --region $AWS_REGION"
  echo
}

# ============================================================================
# MENU
# ============================================================================

show_menu() {
  echo
  echo -e "${GREEN}What would you like to do?${NC}"
  echo
  echo "  1) Initial deployment      (terraform + DB + build + deploy)"
  echo "  2) Redeploy services       (rebuild image + ECS redeploy)"
  echo "  3) Update configuration    (terraform apply + ECS redeploy)"
  echo "  4) Cleanup                 (destroy all AWS resources)"
  echo "  0) Exit"
  echo
}

initial_deployment() {
  section "Initial Deployment"

  check_prerequisites
  check_aws_credentials

  echo
  log_info "Step 1/4: Terraform provisioning"
  terraform_apply

  echo
  log_info "Step 2/4: Build and push backend images"
  build_and_push_images

  echo
  log_info "Step 3/4: Build and push frontend image"
  build_and_push_frontend

  echo
  log_info "Step 4/4: Deploy all services to ECS"
  force_ecs_redeploy

  echo
  log_info "Waiting 30 seconds for services to start..."
  sleep 30

  show_outputs
}

redeploy_services() {
  section "Service Redeploy"

  check_prerequisites
  check_aws_credentials

  echo "Available services:"
  echo "  1) service-registry"
  echo "  2) gateway"
  echo "  3) core"
  echo "  4) engagement"
  echo "  5) leisure"
  echo "  6) mobility"
  echo "  7) frontend (Next.js)"
  echo "  8) All backend services"
  echo "  9) All services (backend + frontend)"
  echo

  read -p "$(echo -e "${YELLOW}Select service to redeploy [1-9]:${NC} ")" -r service_choice

  local SELECTED_SERVICES=()
  local DEPLOY_FRONTEND=false

  case "$service_choice" in
    1)  SELECTED_SERVICES=("service-registry:model-city-service-registry") ;;
    2)  SELECTED_SERVICES=("gateway:model-city-gateway") ;;
    3)  SELECTED_SERVICES=("core:model-city-core") ;;
    4)  SELECTED_SERVICES=("engagement:model-city-engagement") ;;
    5)  SELECTED_SERVICES=("leisure:model-city-leisure") ;;
    6)  SELECTED_SERVICES=("mobility:model-city-mobility") ;;
    7)  DEPLOY_FRONTEND=true ;;
    8)
      SELECTED_SERVICES=(
        "service-registry:model-city-service-registry"
        "gateway:model-city-gateway"
        "core:model-city-core"
        "engagement:model-city-engagement"
        "leisure:model-city-leisure"
        "mobility:model-city-mobility"
      )
      ;;
    9)
      SELECTED_SERVICES=(
        "service-registry:model-city-service-registry"
        "gateway:model-city-gateway"
        "core:model-city-core"
        "engagement:model-city-engagement"
        "leisure:model-city-leisure"
        "mobility:model-city-mobility"
      )
      DEPLOY_FRONTEND=true
      ;;
    *)
      log_error "Invalid option: $service_choice"
      return
      ;;
  esac

  if ! confirm "Rebuild and redeploy selected service(s)?"; then
    echo "Cancelled."
    return
  fi

  echo
  check_ecr_access

  if [[ ${#SELECTED_SERVICES[@]} -gt 0 ]]; then
    build_and_push_selected_images "${SELECTED_SERVICES[@]}"
    force_ecs_redeploy_selected "${SELECTED_SERVICES[@]}"
  fi

  if [[ "$DEPLOY_FRONTEND" == "true" ]]; then
    echo
    build_and_push_frontend
    force_ecs_redeploy_selected "frontend:frontend"
  fi

  echo
  log_success "Redeploy complete"
  echo
  log_info "Services are restarting. Check status with:"
  echo "  aws ecs describe-services --cluster modelcity-academy-cluster --services <name> --region $AWS_REGION"
  echo
}

update_config() {
  section "Update Configuration"
  echo "Use this option when you have changed terraform.tfvars"
  echo "(e.g. added env vars, updated secrets) without changing code."
  echo "It will run 'terraform apply' and then force ECS to pick up"
  echo "the new task definitions."
  echo

  check_prerequisites
  check_aws_credentials

  cd "$TERRAFORM_DIR"

  log_info "Applying Terraform configuration changes..."
  terraform init -upgrade=false > /dev/null || die "Terraform init failed"
  terraform apply -auto-approve || die "Terraform apply failed"

  log_success "Terraform applied"

  echo
  echo "Which services should be redeployed to pick up the new config?"
  echo "  1) frontend only"
  echo "  2) gateway only"
  echo "  3) All services"
  echo
  read -p "$(echo -e "${YELLOW}Select [1-3]:${NC} ")" -r redeploy_choice

  case "$redeploy_choice" in
    1)
      log_info "Redeploying frontend..."
      aws ecs update-service \
        --cluster modelcity-academy-cluster \
        --service frontend \
        --force-new-deployment \
        --region "$AWS_REGION" > /dev/null || log_warn "Could not redeploy frontend"
      ;;
    2)
      log_info "Redeploying gateway..."
      aws ecs update-service \
        --cluster modelcity-academy-cluster \
        --service gateway \
        --force-new-deployment \
        --region "$AWS_REGION" > /dev/null || log_warn "Could not redeploy gateway"
      ;;
    3)
      force_ecs_redeploy
      ;;
    *)
      log_warn "No redeploy selected. Run ECS update manually if needed."
      ;;
  esac

  echo
  log_success "Configuration update complete"
  echo
  log_info "Check service status with:"
  echo "  aws ecs describe-services --cluster modelcity-academy-cluster --services <name> --region $AWS_REGION"
  echo "  aws logs tail /ecs/modelcity-academy/<service> --follow --region $AWS_REGION"
  echo
}

cleanup() {
  section "Cleanup"

  check_prerequisites
  check_aws_credentials
  terraform_destroy

  echo
  log_success "Cleanup completed successfully"
  echo
}

# ============================================================================
# MAIN
# ============================================================================

main() {
  banner "Microservices  ·  Single ALB · Let's Encrypt"
  while true; do
    show_menu

    read -p "$(echo -e "${YELLOW}Select option [0-4]:${NC} ")" -r choice

    case "$choice" in
      1) initial_deployment ;;
      2) redeploy_services ;;
      3) update_config ;;
      4) cleanup ;;
      0)
        echo "Goodbye."
        exit 0
        ;;
      *)
        log_error "Invalid option: $choice"
        ;;
    esac

    echo
    read -p "$(echo -e "${BLUE}Press Enter to continue...${NC}")"
  done
}

if [[ $# -eq 0 ]]; then
  main "$@"
else
  "$@"
fi

