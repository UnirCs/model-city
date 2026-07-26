# Single public ALB security group: HTTP, HTTPS, and HTTPS with mTLS
resource "aws_security_group" "alb" {
  name        = "${local.name}-alb-sg"
  description = "Public ALB - HTTP (80), HTTPS (443), HTTPS mTLS (8443)"
  vpc_id      = aws_vpc.this.id

  ingress {
    description = "HTTP redirect from anywhere"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS (no mTLS) from anywhere"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS with mTLS from anywhere"
    from_port   = 8443
    to_port     = 8443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ECS tasks: gateway and frontend receive from ALB, others from gateway and each other
resource "aws_security_group" "ecs_tasks" {
  name        = "${local.name}-ecs-tasks-sg"
  description = "ECS tasks: gateway/frontend from ALB, others internal"
  vpc_id      = aws_vpc.this.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Gateway port (8762) from ALB — receives both 443 and 8443 traffic
resource "aws_vpc_security_group_ingress_rule" "ecs_gateway_from_alb" {
  security_group_id            = aws_security_group.ecs_tasks.id
  description                  = "Gateway port from ALB"
  ip_protocol                  = "tcp"
  from_port                    = 8762
  to_port                      = 8762
  referenced_security_group_id = aws_security_group.alb.id
}

# Frontend port (3000) from ALB — receives 443 non-API traffic
resource "aws_vpc_security_group_ingress_rule" "ecs_frontend_from_alb" {
  security_group_id            = aws_security_group.ecs_tasks.id
  description                  = "Frontend port from ALB"
  ip_protocol                  = "tcp"
  from_port                    = 3000
  to_port                      = 3000
  referenced_security_group_id = aws_security_group.alb.id
}

# Eureka port (8761) from internal ALB
resource "aws_vpc_security_group_ingress_rule" "ecs_from_eureka_alb" {
  security_group_id            = aws_security_group.ecs_tasks.id
  description                  = "Eureka port from internal ALB"
  ip_protocol                  = "tcp"
  from_port                    = 8761
  to_port                      = 8761
  referenced_security_group_id = aws_security_group.eureka_alb.id
}

# All ports between ECS tasks (Eureka registration + inter-service calls)
resource "aws_vpc_security_group_ingress_rule" "ecs_self" {
  security_group_id            = aws_security_group.ecs_tasks.id
  description                  = "All ports between ECS tasks (Eureka + inter-service)"
  ip_protocol                  = "-1"
  referenced_security_group_id = aws_security_group.ecs_tasks.id
}

# RDS: only from ECS tasks
resource "aws_security_group" "rds" {
  name        = "${local.name}-rds-sg"
  description = "RDS Postgres - only from ECS tasks"
  vpc_id      = aws_vpc.this.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_vpc_security_group_ingress_rule" "rds_from_ecs" {
  security_group_id            = aws_security_group.rds.id
  ip_protocol                  = "tcp"
  from_port                    = 5432
  to_port                      = 5432
  referenced_security_group_id = aws_security_group.ecs_tasks.id
}

