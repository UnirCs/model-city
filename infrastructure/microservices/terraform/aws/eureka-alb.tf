# Internal load balancer for Eureka. AWS Academy Learner Lab does not allow
# servicediscovery:CreatePrivateDnsNamespace, so this replaces Cloud Map with a
# stable private DNS name managed by ELB.
resource "aws_security_group" "eureka_alb" {
  name        = "${local.name}-eureka-alb-sg"
  description = "Internal ALB for Eureka"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "Eureka from ECS tasks"
    from_port       = 8761
    to_port         = 8761
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_lb" "eureka" {
  name               = "${local.name}-eureka-alb"
  internal           = true
  load_balancer_type = "application"
  security_groups    = [aws_security_group.eureka_alb.id]
  subnets            = aws_subnet.private[*].id
  tags               = { Name = "${local.name}-eureka-alb" }
}

resource "aws_lb_target_group" "eureka" {
  name                 = "${local.name}-eureka-tg"
  port                 = 8761
  protocol             = "HTTP"
  vpc_id               = aws_vpc.this.id
  target_type          = "ip"
  deregistration_delay = 30
  tags                 = { Name = "${local.name}-eureka-tg" }

  health_check {
    path                = "/"
    matcher             = "200-399"
    interval            = 30
    timeout             = 10
    healthy_threshold   = 2
    unhealthy_threshold = 5
  }
}

resource "aws_lb_listener" "eureka" {
  load_balancer_arn = aws_lb.eureka.arn
  port              = 8761
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.eureka.arn
  }
}

