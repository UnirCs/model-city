output "alb_dns_name" {
  description = "Single public ALB DNS name. Point model-city.example.org to this."
  value       = aws_lb.public.dns_name
}

output "alb_zone_id" {
  description = "ALB hosted zone ID (for Route53 alias records)."
  value       = aws_lb.public.zone_id
}

# Exposed so the deploy script can read it without parsing terraform.tfvars.
output "domain" {
  description = "Public domain configured for this deployment."
  value       = var.domain
}

# Exposed so the deploy script can pass it as --build-arg to docker build.
output "stripe_publishable_key" {
  description = "Stripe publishable key baked into the Next.js frontend bundle."
  value       = var.stripe_publishable_key
}

output "background_image_url" {
  description = "Background image URL baked into the Next.js frontend bundle."
  value       = var.background_image_url
}

output "ecr_repository_urls" {
  description = "ECR repo URLs to push images to."
  value       = { for k, r in aws_ecr_repository.this : k => r.repository_url }
}

output "rds_endpoint" {
  value = aws_db_instance.postgres.address
}

output "rds_port" {
  value = aws_db_instance.postgres.port
}

output "db_username" {
  description = "Database username for RDS. Used by deploy script for database initialization."
  value       = var.db_username
  sensitive   = true
}

output "db_password" {
  description = "Database password for RDS. Used by deploy script for database initialization."
  value       = var.db_password
  sensitive   = true
}

# Single logical database the monolith connects to.
output "monolith_db_name" {
  description = "Database name created for the monolith."
  value       = var.monolith_db_name
}

output "backend_project_dir" {
  description = "Path to the backend project. Used by deploy script to build and deploy the backend."
  value       = var.backend_project_dir
}

output "frontend_project_dir" {
  description = "Path to the Next.js frontend project. Used by deploy script to build and deploy frontend."
  value       = var.frontend_project_dir
}

output "truststore_s3_bucket" {
  value = aws_s3_bucket.truststore.id
}

output "private_subnet_ids" {
  description = "IDs of the private subnets. Used by the deploy script to run ECS one-off tasks."
  value       = aws_subnet.private[*].id
}

output "ecs_tasks_sg_id" {
  description = "ID of the ECS tasks security group. Used by the deploy script to run ECS one-off tasks."
  value       = aws_security_group.ecs_tasks.id
}

output "elasticache_primary_endpoint" {
  description = "ElastiCache Valkey primary endpoint hostname. Empty when create_elasticache = false."
  value       = try(aws_elasticache_replication_group.this[0].primary_endpoint_address, "")
}

output "elasticache_port" {
  description = "ElastiCache Valkey port."
  value       = try(tostring(aws_elasticache_replication_group.this[0].port), "6379")
}
