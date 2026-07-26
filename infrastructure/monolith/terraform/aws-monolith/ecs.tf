resource "aws_ecs_cluster" "this" {
  name = "${local.name}-cluster"
  setting {
    name  = "containerInsights"
    value = "disabled"
  }
  tags = { Name = "${local.name}-cluster" }
}

# Monolith backend — log group.
resource "aws_cloudwatch_log_group" "monolith" {
  name              = "/ecs/${local.name}/monolith"
  retention_in_days = 7
  tags              = { Name = "${local.name}-monolith-logs" }
}

# Frontend — separate log group.
resource "aws_cloudwatch_log_group" "frontend" {
  name              = "/ecs/${local.name}/frontend"
  retention_in_days = 7
  tags              = { Name = "${local.name}-frontend-logs" }
}

locals {
  # The monolith talks to a single logical database (no per-vertical split).
  monolith_db_url = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/${var.monolith_db_name}"

  # Cache env vars. VALKEY_HOST/PORT resolve to the ElastiCache endpoint when
  # create_elasticache = true. Same contract the shared commons cache code expects.
  cache_env = {
    CACHE_ENABLED   = tostring(var.create_elasticache)
    VALKEY_HOST     = try(aws_elasticache_replication_group.this[0].primary_endpoint_address, "")
    VALKEY_PORT     = try(tostring(aws_elasticache_replication_group.this[0].port), "6379")
    VALKEY_USERNAME = ""
    VALKEY_PASSWORD = ""
  }

  # Single backend task: every vertical bundled into one Spring Boot artifact.
  monolith_env = merge({
    PORT        = "8762"
    DB_URL      = local.monolith_db_url
    DB_USERNAME = var.db_username
    DB_PASSWORD = var.db_password
    # validate: the schema is owned by init-databases-monolith.sql. Hibernate must not
    # mutate the DB on successive deployments.
    JPA_DDL_AUTO = "validate"

    AUTH0_ISSUER_URI             = var.auth0_issuer_uri
    AUTH0_AUDIENCE               = var.auth0_audience
    AUTH0_DOMAIN                 = var.auth0_domain
    AUTH0_MGMT_CLIENT_ID         = var.auth0_mgmt_client_id
    AUTH0_MGMT_CLIENT_SECRET     = var.auth0_mgmt_client_secret
    AUTH0_DB_CONNECTION          = var.auth0_db_connection
    AUTH0_BACKOFFICE_ROLE_ID     = var.auth0_backoffice_role_id
    AUTH0_OPERATOR_ROLE_ID       = var.auth0_operator_role_id
    AUTH0_MOBILITY_AGENT_ROLE_ID = var.auth0_mobility_agent_role_id

    MAIL_HOST      = var.mail_host
    MAIL_PORT      = var.mail_port
    MAIL_USERNAME  = var.mail_username
    MAIL_PASSWORD  = var.mail_password
    MAIL_CITY_NAME = var.mail_city_name
    MAIL_ADDRESS   = var.mail_address

    # Certificate verification (FNMT / DNI) secrets.
    CERT_DNI_PEPPER   = var.cert_dni_pepper
    CERT_TOKEN_SECRET = var.cert_token_secret

    # One Stripe account in the monolith: same secret key, two webhook secrets.
    STRIPE_SECRET_KEY                 = var.stripe_secret_key
    STRIPE_WEBHOOK_SECRET             = var.stripe_webhook_secret_leisure
    STRIPE_RESERVATION_WEBHOOK_SECRET = var.stripe_webhook_secret_mobility
  }, local.cache_env)
}

resource "aws_ecs_task_definition" "monolith" {
  family                   = "${local.name}-monolith"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.monolith_cpu
  memory                   = var.monolith_memory
  execution_role_arn       = data.aws_iam_role.lab.arn
  task_role_arn            = data.aws_iam_role.lab.arn

  container_definitions = jsonencode([{
    name      = "monolith"
    image     = "${aws_ecr_repository.this["monolith"].repository_url}:${var.image_tag}"
    essential = true
    portMappings = [{
      containerPort = 8762
      protocol      = "tcp"
    }]
    environment = [for k, v in local.monolith_env : { name = k, value = tostring(v) }]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.monolith.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

resource "aws_ecs_service" "monolith" {
  name                   = "monolith"
  cluster                = aws_ecs_cluster.this.id
  task_definition        = aws_ecs_task_definition.monolith.arn
  desired_count          = 1
  launch_type            = "FARGATE"
  enable_execute_command = true
  tags                   = { Name = "${local.name}-monolith" }

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.monolith.arn
    container_name   = "monolith"
    container_port   = 8762
  }

  depends_on = [
    aws_lb_listener.https,
    aws_lb_listener.https_mtls,
    aws_lb_listener.http,
    aws_lb_listener_rule.api,
  ]
}

# Frontend (Next.js) ECS task definition.
resource "aws_ecs_task_definition" "frontend" {
  family                   = "${local.name}-frontend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = data.aws_iam_role.lab.arn
  task_role_arn            = data.aws_iam_role.lab.arn

  container_definitions = jsonencode([{
    name      = "frontend"
    image     = "${aws_ecr_repository.this["frontend"].repository_url}:${var.image_tag}"
    essential = true
    portMappings = [{
      containerPort = 3000
      protocol      = "tcp"
    }]
    environment = [
      { name = "PORT", value = "3000" },
      { name = "NODE_ENV", value = "production" },
      # Server-side routing — Next.js server calls the backend through the public ALB.
      { name = "MICROSERVICE_BASE_URL", value = "https://${var.domain}/api" },
      # Auth0 SDK base URL used for callback/logout URL construction.
      { name = "APP_BASE_URL", value = "https://${var.domain}" },
      # Auth0 SDK server-side variables (compatible with @auth0/nextjs-auth0 and NextAuth).
      { name = "AUTH0_DOMAIN", value = var.auth0_domain },
      { name = "AUTH0_ISSUER_BASE_URL", value = "https://${var.auth0_domain}" },
      { name = "AUTH0_CLIENT_ID", value = var.auth0_client_id },
      { name = "AUTH0_CLIENT_SECRET", value = var.auth0_client_secret },
      { name = "AUTH0_AUDIENCE", value = var.auth0_audience },
      { name = "AUTH0_BASE_URL", value = "https://${var.domain}" },
      { name = "AUTH0_SECRET", value = var.nextauth_secret },
      # NextAuth.js aliases.
      { name = "NEXTAUTH_URL", value = "https://${var.domain}" },
      { name = "NEXTAUTH_SECRET", value = var.nextauth_secret },
      # Stripe server-side secret.
      { name = "STRIPE_SECRET_KEY", value = var.stripe_secret_key },
      { name = "STRIPE_PARKING_PRODUCT_ID", value = var.stripe_parking_product_id },
      # Gemini server-side API key.
      { name = "GEMINI_API_KEY", value = var.gemini_api_key },
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.frontend.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

# Frontend ECS service — registered with the frontend target group.
resource "aws_ecs_service" "frontend" {
  name                   = "frontend"
  cluster                = aws_ecs_cluster.this.id
  task_definition        = aws_ecs_task_definition.frontend.arn
  desired_count          = 1
  launch_type            = "FARGATE"
  enable_execute_command = true
  tags                   = { Name = "${local.name}-frontend" }

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.frontend.arn
    container_name   = "frontend"
    container_port   = 3000
  }

  depends_on = [
    aws_lb_listener.https,
    aws_lb_listener_rule.api,
  ]
}
