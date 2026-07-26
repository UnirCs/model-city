#!/usr/bin/env bash
#
# Model City v2 — Monolithic Deployment Script
#
# Same single-ALB edge as deploy.sh (HTTPS + mTLS, Let's Encrypt cert,
# one RDS instance, one ElastiCache, the Next.js frontend) but the backend is a
# single Spring Boot artifact instead of Spring Cloud Gateway + Eureka + 5 services.
#
#   - Port 443 (HTTPS, no mTLS): /* → frontend, /api/* → monolith ECS
#   - Port 8443 (HTTPS, mTLS):  /*  → monolith ECS (FNMT flows)
#   - Port 80: redirect to 443
#
# NOTE: this topology shares resource names with the microservices deployment
# (same ALB / cluster / RDS / domain). They are mutually exclusive — destroy the
# microservices stack (deploy.sh → Cleanup) before deploying the monolith, and
# vice versa. Each uses its own Terraform state directory.
#
# Operations:
#   1) Initial Deployment  — terraform + DB + monolith + frontend build & deploy
#   2) Redeploy Services   — rebuild + redeploy monolith and/or frontend
#   3) Update Configuration — terraform apply + force ECS redeploy
#   4) Cleanup             — destroy all AWS resources
#
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TERRAFORM_DIR="$SCRIPT_DIR/terraform/aws-monolith"
AWS_REGION="${AWS_REGION:-us-east-1}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

CLUSTER="modelcity-academy-cluster"
MONOLITH_MODULE="."

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

  for cmd in aws terraform docker mvn; do
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
    echo "The Spring Boot monolith lives in a separate repository."
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
    log_warn "No Dockerfile found in $FRONTEND_DIR — docker build will fail. Add a Next.js standalone Dockerfile."
    if ! confirm "Continue without a Dockerfile?"; then
      die "Aborted. Add a Dockerfile to the frontend project and retry."
    fi
  fi
}

# ============================================================================
# TERRAFORM
# ============================================================================

terraform_apply() {
  log_info "Initializing and applying Terraform (monolith stack)..."

  cd "$TERRAFORM_DIR"

  terraform init || die "Terraform init failed"

  log_warn "This will create AWS resources (VPC, RDS, ALB, ECS, ECR, ElastiCache)."
  log_warn "Estimated time: 5-10 minutes"
  echo

  terraform apply -auto-approve || die "Terraform apply failed"

  log_success "Terraform infrastructure created"
}

terraform_destroy() {
  log_info "Destroying all AWS resources (monolith stack)..."

  cd "$TERRAFORM_DIR"

  echo -e "\n${RED}=== WARNING ===${NC}"
  echo "This will PERMANENTLY DELETE:"
  echo "  - ECS service (monolith), task, CloudWatch logs"
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

# BUILD & DEPLOY — MONOLITH BACKEND
# ============================================================================

build_and_push_monolith() {
  resolve_backend_dir

  log_info "Building the monolith Maven artifact and Docker image..."

  cd "$BACKEND_DIR"

  local AWS_ACCOUNT_ID
  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
  local REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
  local IMAGE="${REGISTRY}/modelcity/monolith:${IMAGE_TAG}"

  log_info "Building Maven artifact (mvn -pl $MONOLITH_MODULE -am clean package -DskipTests)..."
  mvn -B -pl "$MONOLITH_MODULE" -am clean package -DskipTests -q || die "Failed to build the monolith"
  log_success "Monolith Maven build completed"

  log_info "Logging in to ECR ($REGISTRY)"
  aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$REGISTRY"

  echo
  log_info "Building Docker image: $IMAGE"
  docker build --platform=linux/amd64 -f "${MONOLITH_MODULE}/Dockerfile" -t "$IMAGE" . || \
    die "Failed to build Docker image for the monolith"

  log_info "Pushing $IMAGE"
  docker push "$IMAGE" || die "Failed to push $IMAGE"
  log_success "Pushed $IMAGE"

  log_info "Removing local image to free up disk space..."
  docker rmi "$IMAGE" || log_warn "Failed to remove local image: $IMAGE"
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
  local services=("$@")

  log_info "Forcing ECS service redeployment..."

  for svc in "${services[@]}"; do
    log_info "Redeploying $svc..."
    aws ecs update-service \
      --cluster "$CLUSTER" \
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

  local alb_dns rds_endpoint cache_endpoint domain
  alb_dns=$(terraform output -raw alb_dns_name 2>/dev/null || echo "N/A")
  rds_endpoint=$(terraform output -raw rds_endpoint 2>/dev/null || echo "N/A")
  cache_endpoint=$(terraform output -raw elasticache_primary_endpoint 2>/dev/null || echo "")
  domain=$(terraform output -raw domain 2>/dev/null || echo "N/A")

  local alb_ip="N/A"
  if [[ "$alb_dns" != "N/A" ]]; then
    alb_ip=$(dig +short "$alb_dns" 2>/dev/null | head -n1 || echo "N/A")
  fi

  echo
  echo -e "${GREEN}=== Monolith Deployment Complete ===${NC}"
  echo
  echo -e "${BLUE}Single ALB — $domain${NC}"
  echo "  DNS:  $alb_dns"
  echo "  IP:   $alb_ip"
  echo
  echo -e "${BLUE}Listeners:${NC}"
  echo "  443  HTTPS (no mTLS):"
  echo "    /* → Next.js frontend"
  echo "    /api/* → Monolith ECS (port 8762)"
  echo "  8443 HTTPS (mTLS):  /* → Monolith ECS"
  echo "  80   HTTP → redirect to 443"
  echo
  echo -e "${BLUE}RDS Endpoint:${NC}"
  echo "  $rds_endpoint"
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
  echo -e "${BLUE}Monitor:${NC}"
  echo "  aws ecs list-services --cluster $CLUSTER --region $AWS_REGION"
  echo "  aws logs tail /ecs/modelcity-academy/monolith --follow --region $AWS_REGION"
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
  section "Initial Deployment (Monolith)"

  check_prerequisites
  check_aws_credentials

  echo
  log_info "Step 1/4: Terraform provisioning"
  terraform_apply

  echo
  log_info "Step 2/4: Build and push monolith image"
  build_and_push_monolith

  echo
  log_info "Step 3/4: Build and push frontend image"
  build_and_push_frontend

  echo
  log_info "Step 4/4: Deploy services to ECS"
  force_ecs_redeploy monolith frontend

  echo
  log_info "Waiting 30 seconds for services to start..."
  sleep 30

  show_outputs
}

redeploy_services() {
  section "Service Redeploy (Monolith)"

  check_prerequisites
  check_aws_credentials

  echo "Available services:"
  echo "  1) monolith (backend)"
  echo "  2) frontend (Next.js)"
  echo "  3) Both"
  echo

  read -p "$(echo -e "${YELLOW}Select service to redeploy [1-3]:${NC} ")" -r service_choice

  local DEPLOY_MONOLITH=false DEPLOY_FRONTEND=false
  case "$service_choice" in
    1) DEPLOY_MONOLITH=true ;;
    2) DEPLOY_FRONTEND=true ;;
    3) DEPLOY_MONOLITH=true; DEPLOY_FRONTEND=true ;;
    *) log_error "Invalid option: $service_choice"; return ;;
  esac

  if ! confirm "Rebuild and redeploy selected service(s)?"; then
    echo "Cancelled."
    return
  fi

  echo
  check_ecr_access

  local REDEPLOY=()
  if [[ "$DEPLOY_MONOLITH" == "true" ]]; then
    build_and_push_monolith
    REDEPLOY+=("monolith")
  fi
  if [[ "$DEPLOY_FRONTEND" == "true" ]]; then
    build_and_push_frontend
    REDEPLOY+=("frontend")
  fi

  force_ecs_redeploy "${REDEPLOY[@]}"

  echo
  log_success "Redeploy complete"
  echo
  log_info "Services are restarting. Check status with:"
  echo "  aws ecs describe-services --cluster $CLUSTER --services <name> --region $AWS_REGION"
  echo
}

update_config() {
  section "Update Configuration (Monolith)"
  echo "Use this option when you have changed terraform.tfvars"
  echo "(e.g. added env vars, updated secrets) without changing code."
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
  echo "  1) monolith only"
  echo "  2) frontend only"
  echo "  3) Both"
  echo
  read -p "$(echo -e "${YELLOW}Select [1-3]:${NC} ")" -r redeploy_choice

  case "$redeploy_choice" in
    1) force_ecs_redeploy monolith ;;
    2) force_ecs_redeploy frontend ;;
    3) force_ecs_redeploy monolith frontend ;;
    *) log_warn "No redeploy selected. Run ECS update manually if needed." ;;
  esac

  echo
  log_success "Configuration update complete"
  echo
}

cleanup() {
  section "Cleanup (Monolith)"

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
  banner "Monolith  ·  Single ALB · Let's Encrypt"
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
