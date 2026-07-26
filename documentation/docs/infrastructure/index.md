---
title: Infrastructure
sidebar_label: Overview
sidebar_position: 1
---

# Infrastructure

Model City runs on **AWS** and is provisioned with **Terraform**. Two deployment
topologies — **microservices** and **monolith** — are kept in the same
`infrastructure/` tree, but they are **mutually exclusive**: they reuse the same
resource names (ALB, cluster, RDS instance names, domain), so only one can be
deployed in the same account/region at a time.

The daily workflow is driven by interactive shell scripts:

- `infrastructure/microservices/deploy.sh`
- `infrastructure/monolith/deploy-monolith.sh`

Both scripts run `terraform apply`, build and push Docker images to **ECR**, and
then force **ECS** services to pick up the new images. Terraform itself creates
the VPC, subnets, ALB, security groups, ECR repositories, ECS cluster, RDS
databases, ElastiCache, S3 trust-store bucket, CloudWatch log groups and the
ACM certificate reference.

:::note[AWS account setup]

The IAM user, the `LabRole` task role and the minimum AWS permissions needed to
run the scripts are described in [AWS deployment](../how-to-start/aws-deployment.md).

:::

## In this section

| Page | What it covers |
| --- | --- |
| [AWS architecture](./aws-architecture.md) | The VPC, ALB listeners, security groups, ECS, RDS and ElastiCache layout for both topologies. |
| [TLS and certificates](./certificates.md) | How to create a Let's Encrypt test certificate, import it into ACM, and build the FNMT trust bundle for ALB `verify` mTLS. |
| [Terraform variables](./terraform-variables.md) | The `terraform.tfvars.example` variables and why each is required. |
| [Deploy scripts](./deploy-scripts.md) | What `deploy.sh` and `deploy-monolith.sh` do, their menu options, and how they tie Terraform, Docker and ECS together. |

:::info[Local mTLS]

You can test the FNMT client-certificate flow locally with Nginx before touching
AWS by following [Local mTLS](../how-to-start/mtls-local.md).

:::
