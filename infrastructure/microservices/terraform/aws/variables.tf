variable "aws_region" {
  description = "AWS region."
  type        = string
  default     = "us-east-1"
}

variable "env" {
  description = "Environment name (dev, staging, prod...)."
  type        = string
  default     = "academy"
}

variable "project" {
  description = "Project name, used as prefix for most resources."
  type        = string
  default     = "modelcity"
}

variable "lab_role_name" {
  description = "Pre-existing IAM role to attach to ECS tasks. In AWS Academy this is 'LabRole'."
  type        = string
  default     = "LabRole"
}

variable "vpc_cidr" {
  type    = string
  default = "10.20.0.0/16"
}

variable "public_subnet_cidrs" {
  type    = list(string)
  default = ["10.20.0.0/24", "10.20.1.0/24"]
}

variable "private_subnet_cidrs" {
  type    = list(string)
  default = ["10.20.10.0/24", "10.20.11.0/24"]
}

# Database
variable "db_engine_version" {
  description = "PostgreSQL engine version for RDS."
  type        = string
  default     = "16.14"
}

variable "db_instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "db_allocated_storage" {
  type    = number
  default = 20
}

variable "db_username" {
  type    = string
  default = "modelcity"
}

variable "db_password" {
  description = "Master password for the RDS instance. Provide via terraform.tfvars or TF_VAR_db_password."
  type        = string
  sensitive   = true
}

# Container image tag deployed to all services
variable "image_tag" {
  type    = string
  default = "latest"
}

# TLS — Let's Encrypt cert imported into ACM via ../../scripts/import-cert-to-acm.sh
variable "acm_certificate_arn" {
  description = "ARN of the Let's Encrypt certificate imported into ACM."
  type        = string
}

# mTLS — FNMT CA bundle for client certificate verification
variable "mtls_trust_bundle_path" {
  description = "Local path to the FNMT CA bundle (PEM) that the ALB will use to verify client certificates."
  type        = string
}

# Public domain
variable "domain" {
  description = "Public domain name used by the ALB (e.g. model-city.example.org)."
  type        = string
  default     = "model-city.example.org"
}

# Auth0 — shared
variable "auth0_domain" {
  description = "Auth0 tenant domain without protocol (e.g. tenant.eu.auth0.com)."
  type        = string
  default     = "unir-software-engineering.eu.auth0.com"
}

variable "auth0_audience" {
  description = "Auth0 API audience identifier."
  type        = string
  default     = "https://model-city.aranjuez.es"
}

# Auth0 — backend management API (used by core service)
variable "auth0_mgmt_client_id" {
  type      = string
  default   = ""
  sensitive = true
}

variable "auth0_mgmt_client_secret" {
  type      = string
  default   = ""
  sensitive = true
}

# Auth0 — database connection name. Auth0's out-of-the-box DB is "Username-Password-Authentication".
variable "auth0_db_connection" {
  description = "Auth0 database connection name used by the core vertical for user provisioning."
  type        = string
  default     = "Username-Password-Authentication"
}

# Auth0 — role IDs assigned by the core vertical when inviting back-office users / agents.
# Tenant-specific: copy them from your Auth0 dashboard (Roles) into terraform.tfvars.
variable "auth0_backoffice_role_id" {
  type    = string
  default = ""
}

variable "auth0_operator_role_id" {
  type    = string
  default = ""
}

variable "auth0_mobility_agent_role_id" {
  type    = string
  default = ""
}

# Auth0 — frontend SPA / regular web application
variable "auth0_client_id" {
  description = "Auth0 Client ID for the Next.js frontend application."
  type        = string
  default     = ""
  sensitive   = true
}

variable "auth0_client_secret" {
  description = "Auth0 Client Secret for the Next.js frontend application."
  type        = string
  default     = ""
  sensitive   = true
}

# Kept for backward compat with gateway env var name.
variable "auth0_issuer_uri" {
  type    = string
  default = "https://unir-software-engineering.eu.auth0.com/"
}

# Mail
variable "mail_username" {
  type      = string
  default   = ""
  sensitive = true
}

variable "mail_password" {
  type      = string
  default   = ""
  sensitive = true
}

variable "mail_host" {
  description = "SMTP server host used to send transactional emails."
  type        = string
  default     = "smtp.gmail.com"
}

variable "mail_port" {
  description = "SMTP server port (587 = STARTTLS)."
  type        = string
  default     = "587"
}

variable "mail_city_name" {
  description = "City/authority display name shown in outgoing emails."
  type        = string
  default     = "Ayuntamiento de Model City"
}

variable "mail_address" {
  description = "Postal address shown in outgoing emails."
  type        = string
  default     = "Plaza Mayor, 1. 00000 Model City"
}

# Certificate verification (FNMT / DNI flows) — server-side secrets consumed by the core vertical.
variable "cert_dni_pepper" {
  description = "Secret pepper used when hashing DNI values during certificate verification."
  type        = string
  default     = ""
  sensitive   = true
}

variable "cert_token_secret" {
  description = "Secret used to sign certificate-verification tokens."
  type        = string
  default     = ""
  sensitive   = true
}

# Stripe
variable "stripe_secret_key" {
  type      = string
  default   = ""
  sensitive = true
}

variable "stripe_webhook_secret_leisure" {
  type      = string
  default   = ""
  sensitive = true
}

variable "stripe_webhook_secret_mobility" {
  type      = string
  default   = ""
  sensitive = true
}

variable "stripe_parking_product_id" {
  type      = string
  default   = ""
  sensitive = true
}

# Stripe publishable key — NOT sensitive, baked into the Next.js bundle at build time.
variable "stripe_publishable_key" {
  description = "Stripe publishable key (pk_live_... or pk_test_...). Embedded in the frontend bundle at build time."
  type        = string
  default     = ""
}

# Frontend (Next.js) — session encryption secret (openssl rand -base64 32)
variable "nextauth_secret" {
  description = "Auth0/NextAuth secret for session encryption. Generate with: openssl rand -base64 32"
  type        = string
  default     = ""
  sensitive   = true
}

variable "allowed_origins" {
  description = "CORS allowed origins for the gateway."
  type        = string
  default     = "*"
}

# Gemini API key — server-side only (Next.js server calls the Gemini API).
variable "gemini_api_key" {
  description = "Google Gemini API key for server-side AI features in the Next.js frontend."
  type        = string
  default     = ""
  sensitive   = true
}

# Gemini model id — falls back to the frontend's built-in default when empty.
variable "gemini_model" {
  description = "Google Gemini model id for server-side AI features in the Next.js frontend."
  type        = string
  default     = ""
}

# ElastiCache
variable "create_elasticache" {
  description = "Create ElastiCache Valkey replication group. Set to false if unavailable in the Academy session."
  type        = bool
  default     = true
}

# Backend project location — Maven multi-module root (built and dockerized by the deploy script).
variable "backend_project_dir" {
  description = "Absolute path to the Spring Boot microservices backend project directory (Maven root)."
  type        = string
}

# Frontend project location
variable "frontend_project_dir" {
  description = "Absolute path to the Next.js frontend project directory."
  type        = string
}

