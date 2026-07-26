# ElastiCache Valkey — subnet group en subredes privadas (sin acceso público)
resource "aws_elasticache_subnet_group" "this" {
  count      = var.create_elasticache ? 1 : 0
  name       = "${local.name}-cache-subnets"
  subnet_ids = aws_subnet.private[*].id
  tags       = { Name = "${local.name}-cache-subnets" }
}

# Security group: puerto 6379 abierto SOLO desde el SG de las ECS tasks.
# Ninguna regla permite acceso desde internet ni desde las subredes públicas.
resource "aws_security_group" "elasticache" {
  count       = var.create_elasticache ? 1 : 0
  name        = "${local.name}-elasticache-sg"
  description = "ElastiCache Valkey - inbound 6379 from ECS tasks only"
  vpc_id      = aws_vpc.this.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_vpc_security_group_ingress_rule" "elasticache_from_ecs" {
  count                        = var.create_elasticache ? 1 : 0
  security_group_id            = aws_security_group.elasticache[0].id
  description                  = "Valkey port from ECS tasks only"
  ip_protocol                  = "tcp"
  from_port                    = 6379
  to_port                      = 6379
  referenced_security_group_id = aws_security_group.ecs_tasks.id
}

# Replication group de Valkey con un único nodo primario (sin réplicas).
# Sin TLS ni autenticación porque el acceso queda restringido por VPC + SG.
# cost-optimized para AWS Academy: cache.t3.micro ~0.017 $/h.
resource "aws_elasticache_replication_group" "this" {
  count                      = var.create_elasticache ? 1 : 0
  replication_group_id       = "${local.name}-cache"
  description                = "Model City distributed cache - Valkey, private VPC only"
  engine                     = "valkey"
  engine_version             = "7.2"
  node_type                  = "cache.t3.micro"
  num_cache_clusters         = 1
  port                       = 6379
  subnet_group_name          = aws_elasticache_subnet_group.this[0].name
  security_group_ids         = [aws_security_group.elasticache[0].id]
  at_rest_encryption_enabled = false
  transit_encryption_enabled = false
  apply_immediately          = true
  tags                       = { Name = "${local.name}-cache" }
}
