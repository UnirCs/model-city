---
title: Terraform variables
sidebar_label: Variables
sidebar_position: 4
---

# Terraform variables

Both topologies read their settings from `terraform.tfvars`. The example files
`infrastructure/microservices/terraform/aws/terraform.tfvars.example` and
`infrastructure/monolith/terraform/aws-monolith/terraform.tfvars.example` show
the values you must provide.

```bash
# Microservices
cd infrastructure/microservices/terraform/aws
cp terraform.tfvars.example terraform.tfvars

# Monolith
cd infrastructure/monolith/terraform/aws-monolith
cp terraform.tfvars.example terraform.tfvars
```

`terraform.tfvars` is **gitignored** because it contains secrets. Never commit
it.

## Required variables

These variables have no default and the stack cannot be applied without them.

| Variable | Example value | Why it is required |
| --- | --- | --- |
| `db_password` | `change-me` | Master password for the RDS PostgreSQL instances. Used by Terraform to create the databases and by the backend tasks to connect. |
| `acm_certificate_arn` | `arn:aws:acm:us-east-1:...` | The ALB listeners on `443` and `8443` need this imported server certificate. Generate it with the certificate scripts and copy the ARN. |
| `mtls_trust_bundle_path` | `../../../resources/fnmt-client-trust-bundle.pem` | Local path to the FNMT CA bundle. Terraform uploads it to S3 and attaches it to the ALB trust store used by the mTLS listener. |
| `backend_project_dir` | `/path/to/model-city-back-end` | Absolute path to the Maven backend project. The deploy script reads this from Terraform outputs and builds Docker images from it. |
| `frontend_project_dir` | `/path/to/model-city-front-end` | Absolute path to the Next.js frontend project. The deploy script reads this from Terraform outputs and builds the frontend Docker image. |

## Common overrides

These variables have defaults but you will usually override them per city.

| Variable | Default | Why override it |
| --- | --- | --- |
| `aws_region` | `us-east-1` | AWS region where everything is created. ACM certificates and ALBs must be in the same region. |
| `domain` | `model-city.example.org` | Public domain served by the ALB. Must match the ACM certificate and the DNS record you point at the ALB. |
| `auth0_domain` | `unir-software-engineering.eu.auth0.com` | Auth0 tenant domain (no protocol). Used by the Spring Security OAuth2 resource server and the Next.js frontend. |
| `auth0_audience` | `https://model-city.aranjuez.es` | Auth0 API audience identifier used to validate JWT access tokens. |
| `monolith_db_name` | `modelcity` | **Monolith only.** Name of the single logical database created on the shared RDS instance. |

## Backend runtime config

| Variable | Default | Why it is needed |
| --- | --- | --- |
| `auth0_db_connection` | `Username-Password-Authentication` | Auth0 database connection used by the core vertical when provisioning local users. |
| `mail_host` | `smtp.gmail.com` | SMTP server host for transactional emails sent by the core vertical. |
| `mail_port` | `587` | SMTP server port (587 is STARTTLS). |
| `mail_city_name` | `Ayuntamiento de Model City` | Display name of the city/authority in outgoing emails. |
| `mail_address` | `Plaza Mayor, 1. 00000 Model City` | Postal address shown in outgoing emails. |

## Secrets and integrations

These variables are optional in the sense that the stack will deploy if they are
empty, but the matching features will not work.

| Variable | Why it is needed |
| --- | --- |
| `auth0_mgmt_client_id` / `auth0_mgmt_client_secret` | Auth0 Management API credentials used by the core vertical to invite back-office users and agents. |
| `auth0_client_id` / `auth0_client_secret` | Auth0 application credentials for the Next.js frontend. |
| `mail_username` / `mail_password` | SMTP credentials for sending transactional emails. |
| `stripe_secret_key` | Stripe secret key used by the leisure and mobility verticals for payments. |
| `stripe_publishable_key` | Stripe publishable key embedded in the Next.js bundle at build time. |
| `stripe_webhook_secret_leisure` / `stripe_webhook_secret_mobility` | Stripe webhook endpoint secrets used to validate incoming webhooks. |
| `stripe_parking_product_id` | Stripe product identifier used by the mobility parking flow. |
| `nextauth_secret` | Session encryption secret for NextAuth.js / Auth0 SDK. Generate with `openssl rand -base64 32`. |
| `gemini_api_key` | Google Gemini API key used by the Next.js frontend for server-side AI features. |

## Auth0 roles and certificate secrets

| Variable | Why it is needed |
| --- | --- |
| `auth0_backoffice_role_id` | Role assigned by the core vertical when inviting back-office users. |
| `auth0_operator_role_id` | Role assigned when inviting operators. |
| `auth0_mobility_agent_role_id` | Role assigned when inviting mobility agents. |
| `cert_dni_pepper` | Secret pepper used when hashing DNI values during the FNMT certificate verification flow. |
| `cert_token_secret` | Secret used to sign certificate-verification tokens. |

## Other variables with defaults

These are defined in `variables.tf` and can be left alone for a first AWS
Academy deployment.

| Variable | Default | Purpose |
| --- | --- | --- |
| `env` | `academy` | Name suffix for most resources. |
| `project` | `modelcity` | Prefix for resource names and ECR repository paths. |
| `lab_role_name` | `LabRole` | Pre-existing IAM role used as ECS execution and task role. See [AWS deployment](../how-to-start/aws-deployment.md). |
| `vpc_cidr` | `10.20.0.0/16` | VPC CIDR block. |
| `public_subnet_cidrs` | `["10.20.0.0/24", "10.20.1.0/24"]` | Public subnet CIDRs. |
| `private_subnet_cidrs` | `["10.20.10.0/24", "10.20.11.0/24"]` | Private subnet CIDRs. |
| `db_engine_version` | `16.14` | PostgreSQL engine version for RDS. |
| `db_instance_class` | `db.t3.micro` | RDS instance class. |
| `db_allocated_storage` | `20` | Allocated storage in GiB. |
| `db_username` | `modelcity` | Master database username. |
| `image_tag` | `latest` | Docker image tag pushed and deployed. |
| `create_elasticache` | `true` | Create the ElastiCache Valkey replication group. Set to `false` if ElastiCache is not available in the lab session. |
| `monolith_cpu` | `1024` | **Monolith only.** Fargate CPU units for the monolith task. |
| `monolith_memory` | `2048` | **Monolith only.** Fargate memory (MiB) for the monolith task. |

## Reading the variables from the deploy scripts

Both deploy scripts read `backend_project_dir` and `frontend_project_dir` from
Terraform outputs so they do not need to parse `terraform.tfvars` themselves:

```bash
cd "$TERRAFORM_DIR"
BACKEND_DIR=$(terraform output -raw backend_project_dir 2>/dev/null || true)
FRONTEND_DIR=$(terraform output -raw frontend_project_dir 2>/dev/null || true)
```

You can override those paths at runtime with environment variables if you want
to build a different checkout without changing the Terraform state:

```bash
export BACKEND_PROJECT_DIR=/path/to/backend
export FRONTEND_PROJECT_DIR=/path/to/frontend
./deploy.sh
```
