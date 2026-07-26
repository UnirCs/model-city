resource "aws_db_subnet_group" "this" {
  name       = "${local.name}-db-subnets"
  subnet_ids = aws_subnet.public[*].id
  tags       = { Name = "${local.name}-db-subnets" }
}

locals {
  # Database-per-service: one dedicated Postgres instance per vertical.
  # Each service only ever receives its own instance's endpoint (see ecs.tf).
  db_verticals = ["core", "engagement", "leisure", "mobility"]
}

# One Postgres instance per vertical. Terraform creates the database automatically
# at launch via db_name; schema + seed data are owned by each service's Flyway migrations.
resource "aws_db_instance" "postgres" {
  for_each = toset(local.db_verticals)

  identifier                 = "${local.name}-postgres-${each.key}"
  engine                     = "postgres"
  engine_version             = var.db_engine_version
  instance_class             = var.db_instance_class
  allocated_storage          = var.db_allocated_storage
  storage_type               = "gp3"
  storage_encrypted          = true
  db_name                    = "modelcity_${each.key}"
  username                   = var.db_username
  password                   = var.db_password
  port                       = 5432
  db_subnet_group_name       = aws_db_subnet_group.this.name
  vpc_security_group_ids     = [aws_security_group.rds.id]
  publicly_accessible        = false
  multi_az                   = false
  skip_final_snapshot        = true
  deletion_protection        = false
  apply_immediately          = true
  auto_minor_version_upgrade = true
  backup_retention_period    = 0
  tags                       = { Name = "${local.name}-postgres-${each.key}" }
}
