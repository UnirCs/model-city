# S3 bucket that stores the CA bundle used by the ALB TrustStore for mTLS.
resource "aws_s3_bucket" "truststore" {
  bucket        = "${local.name}-mtls-truststore-${random_id.bucket_suffix.hex}"
  force_destroy = true
  tags          = { Name = "${local.name}-mtls-truststore" }
}

resource "random_id" "bucket_suffix" {
  byte_length = 4
}

resource "aws_s3_bucket_public_access_block" "truststore" {
  bucket                  = aws_s3_bucket.truststore.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_object" "fnmt_bundle" {
  bucket = aws_s3_bucket.truststore.id
  key    = "fnmt-client-trust-bundle.pem"
  source = var.mtls_trust_bundle_path
  etag   = filemd5(var.mtls_trust_bundle_path)
}

resource "aws_lb_trust_store" "fnmt" {
  name                             = "${local.name}-fnmt"
  ca_certificates_bundle_s3_bucket = aws_s3_bucket.truststore.id
  ca_certificates_bundle_s3_key    = aws_s3_object.fnmt_bundle.key
  tags                             = { Name = "${local.name}-fnmt-truststore" }
}

# Single public ALB serving all traffic.
resource "aws_lb" "public" {
  name               = "${local.name}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  enable_cross_zone_load_balancing = true
  tags                             = { Name = "${local.name}-alb" }
}

# Target group: the monolith (entire backend API + mTLS flows) on port 8762.
resource "aws_lb_target_group" "monolith" {
  name                 = "${local.name}-mono-tg"
  port                 = 8762
  protocol             = "HTTP"
  vpc_id               = aws_vpc.this.id
  target_type          = "ip"
  deregistration_delay = 30
  tags                 = { Name = "${local.name}-mono-tg" }

  # MonolithRoutingConfig prefixes only @RestController paths with /api, so actuator
  # endpoints keep their bare path and the health check stays at /actuator/health.
  health_check {
    path                = "/actuator/health"
    matcher             = "200"
    interval            = 30
    timeout             = 10
    healthy_threshold   = 2
    unhealthy_threshold = 5
  }
}

# Target group: Next.js frontend.
resource "aws_lb_target_group" "frontend" {
  name                 = "${local.name}-fe-tg"
  port                 = 3000
  protocol             = "HTTP"
  vpc_id               = aws_vpc.this.id
  target_type          = "ip"
  deregistration_delay = 30
  tags                 = { Name = "${local.name}-fe-tg" }

  health_check {
    path                = "/"
    matcher             = "200-399"
    interval            = 30
    timeout             = 10
    healthy_threshold   = 2
    unhealthy_threshold = 5
  }
}

# Port 80 — redirect to HTTPS.
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.public.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# Port 443 — HTTPS without mTLS.
# Default: frontend. Rule priority 10: /api/* → monolith.
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.public.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.acm_certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend.arn
  }
}

# Route /api/* to the monolith backend on port 443.
resource "aws_lb_listener_rule" "api" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 10

  condition {
    path_pattern {
      values = ["/api/*"]
    }
  }

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.monolith.arn
  }
}

# Port 8443 — HTTPS with mTLS (verify mode) for FNMT certificate flows.
# All traffic goes directly to the monolith.
resource "aws_lb_listener" "https_mtls" {
  load_balancer_arn = aws_lb.public.arn
  port              = 8443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.acm_certificate_arn

  mutual_authentication {
    mode            = "verify"
    trust_store_arn = aws_lb_trust_store.fnmt.arn
  }

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.monolith.arn
  }
}
