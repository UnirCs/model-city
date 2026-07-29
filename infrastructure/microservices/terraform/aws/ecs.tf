resource "aws_ecs_cluster" "this" {
  name = "${local.name}-cluster"
  setting {
    name  = "containerInsights"
    value = "disabled"
  }
  tags = { Name = "${local.name}-cluster" }
}

# Backend services — log groups created as a group.
resource "aws_cloudwatch_log_group" "services" {
  for_each          = toset(local.backend_services)
  name              = "/ecs/${local.name}/${each.value}"
  retention_in_days = 7
  tags              = { Name = "${local.name}-${each.value}-logs" }
}

# Frontend — separate log group.
resource "aws_cloudwatch_log_group" "frontend" {
  name              = "/ecs/${local.name}/frontend"
  retention_in_days = 7
  tags              = { Name = "${local.name}-frontend-logs" }
}

locals {
  eureka_url = "http://${aws_lb.eureka.dns_name}:8761/eureka"

  # Backend services only (excludes frontend which is managed separately).
  backend_services = [
    "service-registry",
    "gateway",
    "core",
    "engagement",
    "leisure",
    "mobility",
  ]

  # Cache env vars injected into microservices that use distributed cache.
  # VALKEY_HOST/PORT resolve to the ElastiCache endpoint when create_elasticache = true.
  cache_env = {
    CACHE_ENABLED   = tostring(var.create_elasticache)
    VALKEY_HOST     = try(aws_elasticache_replication_group.this[0].primary_endpoint_address, "")
    VALKEY_PORT     = try(tostring(aws_elasticache_replication_group.this[0].port), "6379")
    VALKEY_USERNAME = ""
    VALKEY_PASSWORD = ""
  }

  # Per-service runtime config: port and environment-variable map.
  service_specs = {
    "service-registry" = {
      port = 8761
      env = {
        EUREKA_HOST                              = aws_lb.eureka.dns_name
        HOSTNAME                                 = "service-registry.${local.name}.local"
        PORT                                     = "8761"
        EUREKA_INSTANCE_PREFER_IP_ADDRESS        = "true"
        SPRING_CLOUD_INETUTILS_PREFERREDNETWORKS = "10.20"
      }
      cpu    = 256
      memory = 512
    }
    "gateway" = {
      port = 8762
      env = {
        EUREKA_URL                               = local.eureka_url
        ALLOWED_ORIGINS                          = var.allowed_origins
        AUTH0_AUDIENCE                           = var.auth0_audience
        AUTH0_DOMAIN                             = var.auth0_domain
        HOSTNAME                                 = "gateway.${local.name}.local"
        PORT                                     = "8762"
        EUREKA_INSTANCE_PREFER_IP_ADDRESS        = "true"
        SPRING_CLOUD_INETUTILS_PREFERREDNETWORKS = "10.20"
      }
      cpu    = 512
      memory = 1024
    }
    "core" = {
      port = 8090
      env = merge({
        EUREKA_URL   = local.eureka_url
        DB_URL       = "jdbc:postgresql://${aws_db_instance.postgres["core"].address}:5432/modelcity_core"
        DB_USERNAME  = var.db_username
        DB_PASSWORD  = var.db_password
        JPA_DDL_AUTO = "update"

        MAIL_HOST      = var.mail_host
        MAIL_PORT      = var.mail_port
        MAIL_USERNAME  = var.mail_username
        MAIL_PASSWORD  = var.mail_password
        MAIL_CITY_NAME = var.mail_city_name
        MAIL_ADDRESS   = var.mail_address

        AUTH0_DOMAIN                 = var.auth0_domain
        AUTH0_MGMT_CLIENT_ID         = var.auth0_mgmt_client_id
        AUTH0_MGMT_CLIENT_SECRET     = var.auth0_mgmt_client_secret
        AUTH0_DB_CONNECTION          = var.auth0_db_connection
        AUTH0_BACKOFFICE_ROLE_ID     = var.auth0_backoffice_role_id
        AUTH0_OPERATOR_ROLE_ID       = var.auth0_operator_role_id
        AUTH0_MOBILITY_AGENT_ROLE_ID = var.auth0_mobility_agent_role_id

        CERT_DNI_PEPPER   = var.cert_dni_pepper
        CERT_TOKEN_SECRET = var.cert_token_secret

        HOSTNAME                                 = "core.${local.name}.local"
        PORT                                     = "8090"
        EUREKA_INSTANCE_PREFER_IP_ADDRESS        = "true"
        SPRING_CLOUD_INETUTILS_PREFERREDNETWORKS = "10.20"
      }, local.cache_env)
      cpu    = 512
      memory = 1024
    }
    "engagement" = {
      port = 8091
      env = merge({
        EUREKA_URL                               = local.eureka_url
        DB_URL                                   = "jdbc:postgresql://${aws_db_instance.postgres["engagement"].address}:5432/modelcity_engagement"
        DB_USERNAME                              = var.db_username
        DB_PASSWORD                              = var.db_password
        JPA_DDL_AUTO                             = "update"
        HOSTNAME                                 = "engagement.${local.name}.local"
        PORT                                     = "8091"
        EUREKA_INSTANCE_PREFER_IP_ADDRESS        = "true"
        SPRING_CLOUD_INETUTILS_PREFERREDNETWORKS = "10.20"
      }, local.cache_env)
      cpu    = 512
      memory = 1024
    }
    "leisure" = {
      port = 8093
      env = merge({
        EUREKA_URL                               = local.eureka_url
        DB_URL                                   = "jdbc:postgresql://${aws_db_instance.postgres["leisure"].address}:5432/modelcity_leisure"
        DB_USERNAME                              = var.db_username
        DB_PASSWORD                              = var.db_password
        JPA_DDL_AUTO                             = "update"
        STRIPE_SECRET_KEY                        = var.stripe_secret_key
        STRIPE_WEBHOOK_SECRET                    = var.stripe_webhook_secret_leisure
        HOSTNAME                                 = "leisure.${local.name}.local"
        PORT                                     = "8093"
        EUREKA_INSTANCE_PREFER_IP_ADDRESS        = "true"
        SPRING_CLOUD_INETUTILS_PREFERREDNETWORKS = "10.20"
      }, local.cache_env)
      cpu    = 512
      memory = 1024
    }
    "mobility" = {
      port = 8094
      env = merge({
        EUREKA_URL                               = local.eureka_url
        DB_URL                                   = "jdbc:postgresql://${aws_db_instance.postgres["mobility"].address}:5432/modelcity_mobility"
        DB_USERNAME                              = var.db_username
        DB_PASSWORD                              = var.db_password
        JPA_DDL_AUTO                             = "update"
        STRIPE_SECRET_KEY                        = var.stripe_secret_key
        STRIPE_WEBHOOK_SECRET                    = var.stripe_webhook_secret_mobility
        HOSTNAME                                 = "mobility.${local.name}.local"
        PORT                                     = "8094"
        EUREKA_INSTANCE_PREFER_IP_ADDRESS        = "true"
        SPRING_CLOUD_INETUTILS_PREFERREDNETWORKS = "10.20"
      }, local.cache_env)
      cpu    = 512
      memory = 1024
    }
  }
}

resource "aws_ecs_task_definition" "this" {
  for_each                 = local.service_specs
  family                   = "${local.name}-${each.key}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = data.aws_iam_role.lab.arn
  task_role_arn            = data.aws_iam_role.lab.arn

  container_definitions = jsonencode([{
    name      = each.key
    image     = "${aws_ecr_repository.this[each.key].repository_url}:${var.image_tag}"
    essential = true
    portMappings = [{
      containerPort = each.value.port
      protocol      = "tcp"
    }]
    environment = [for k, v in each.value.env : { name = k, value = tostring(v) }]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.services[each.key].name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

resource "aws_ecs_service" "this" {
  for_each               = local.service_specs
  name                   = each.key
  cluster                = aws_ecs_cluster.this.id
  task_definition        = aws_ecs_task_definition.this[each.key].arn
  desired_count          = 1
  launch_type            = "FARGATE"
  enable_execute_command = true
  tags                   = { Name = "${local.name}-${each.key}" }

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  dynamic "load_balancer" {
    for_each = each.key == "gateway" ? [
      {
        target_group_arn = aws_lb_target_group.gateway.arn
        container_name   = "gateway"
        container_port   = 8762
      }
      ] : each.key == "service-registry" ? [
      {
        target_group_arn = aws_lb_target_group.eureka.arn
        container_name   = "service-registry"
        container_port   = 8761
      }
    ] : []
    content {
      target_group_arn = load_balancer.value.target_group_arn
      container_name   = load_balancer.value.container_name
      container_port   = load_balancer.value.container_port
    }
  }

  depends_on = [
    aws_lb_listener.https,
    aws_lb_listener.https_mtls,
    aws_lb_listener.http,
    aws_lb_listener.eureka,
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
      { name = "GEMINI_MODEL", value = var.gemini_model },
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

