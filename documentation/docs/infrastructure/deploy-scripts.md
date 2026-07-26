---
title: Deploy scripts
sidebar_label: Deploy scripts
sidebar_position: 5
---

# Deploy scripts

Model City provides one interactive deploy script per topology:

- `infrastructure/microservices/deploy.sh` — for the microservices stack.
- `infrastructure/monolith/deploy-monolith.sh` — for the monolith stack.

Both scripts do the same high-level job: they run **Terraform** to create the
AWS infrastructure, build and push the application **Docker images** to **ECR**,esta
and then force an **ECS** redeployment so the new images start running.

:::caution[Mutually exclusive stacks]

The two topologies share resource names (same ALB, cluster, RDS names and
domain). Destroy one before deploying the other.

:::

## Prerequisites

The scripts check for the required local tools before doing anything.

| Tool | Microservices | Monolith |
| --- | --- | --- |
| `aws` (CLI v2) | required | required |
| `terraform` | required | required |
| `docker` | required | required |
| `mvn` (Maven) | required | required |
| `jq` | required | — |

They also verify that AWS credentials are active (`aws sts get-caller-identity`)
and that ECR is reachable (`aws ecr get-authorization-token`). The ECR check is
used to detect expired AWS Academy credentials and prints a specific reminder.

## Interactive menu

Running a script without arguments opens a menu:

```bash
cd infrastructure/microservices
./deploy.sh
```

```text
What would you like to do?
  1) Initial deployment      (terraform + build + deploy)
  2) Redeploy services       (rebuild image + ECS redeploy)
  3) Update configuration    (terraform apply + ECS redeploy)
  4) Cleanup                 (destroy all AWS resources)
  0) Exit
```

`deploy-monolith.sh` shows the same options but works with `monolith` and
`frontend` only.

## Option 1 — Initial deployment

This is the full end-to-end flow for a fresh environment.

### Microservices

1. `terraform apply` creates the VPC, ALB, security groups, ECR repos, ECS
   cluster, RDS instances, ElastiCache, S3 trust-store bucket and CloudWatch
   log groups.
2. Build Maven artifacts for:
   - `service-registry`
   - `gateway`
   - `core`
   - `engagement`
   - `leisure`
   - `mobility`
3. Build each module's Docker image with `--platform=linux/amd64` and push to
   the matching ECR repository (`modelcity/<service>:latest`).
4. Build the Next.js frontend Docker image with build arguments for the public
   domain and Stripe publishable key, then push `modelcity/frontend:latest`.
5. Call `aws ecs update-service --force-new-deployment` for every service and
   the frontend.
6. Wait 30 seconds and print a deployment summary with ALB DNS, RDS endpoints,
   cache endpoint and the DNS record to update.

### Monolith

1. `terraform apply` creates the same network and platform resources but with a
   single RDS instance and only two ECR repositories (`monolith`, `frontend`).
2. Build the single Maven monolith artifact (`mvn -pl . -am clean package`).
3. Build and push `modelcity/monolith:latest`.
4. Build and push `modelcity/frontend:latest` with the same build arguments as
   the microservices script.
5. Call `aws ecs update-service --force-new-deployment` for `monolith` and
   `frontend`.
6. Print the deployment summary.

## Option 2 — Redeploy services

Use this when the code has changed and you only want to rebuild and restart one
or more services.

### Microservices

The script asks which service to rebuild:

```text
1) service-registry
2) gateway
3) core
4) engagement
5) leisure
6) mobility
7) frontend
8) All backend services
9) All services
```

It then builds and pushes only the selected images and forces the corresponding
ECS services to start new tasks.

### Monolith

The script asks:

```text
1) monolith (backend)
2) frontend (Next.js)
3) Both
```

It rebuilds the selected images and forces the matching ECS service(s).

## Option 3 — Update configuration

Use this when you have changed `terraform.tfvars` values (secrets, feature
flags, environment variables) but **not** the application code.

The script runs `terraform apply` to update task definitions and other
resources, then asks which services should be redeployed so the running tasks
pick up the new configuration.

This is the fastest way to rotate a secret or change the `auth0_audience`
without rebuilding Docker images.

## Option 4 — Cleanup

This runs `terraform destroy -auto-approve` and removes:

- ECS services, tasks and CloudWatch log groups
- ALB, target groups and security groups
- RDS PostgreSQL instances (data is deleted)
- ECR repositories (images are deleted)
- ElastiCache replication group
- VPC, subnets, NAT Gateway, IGW and S3 trust-store bucket

The script retries the destroy up to three times because AWS sometimes holds
ENI dependencies after a service is deleted.

## How the scripts resolve project paths

Both scripts resolve the backend and frontend directories in this order:

1. **Terraform output** — preferred. `backend_project_dir` and
   `frontend_project_dir` are exposed as outputs so the script can read them
   without parsing `.tfvars`.
2. **Environment variables** — `BACKEND_PROJECT_DIR` and
   `FRONTEND_PROJECT_DIR`.
3. **Interactive prompt** — if neither of the above is set.

The script validates that the backend directory contains `pom.xml` and warns
if the frontend directory does not contain a `Dockerfile`.

## Environment overrides

You can override a few values at runtime without editing `terraform.tfvars`:

| Variable | Default | Effect |
| --- | --- | --- |
| `AWS_REGION` | `us-east-1` | Region used by the AWS CLI calls. |
| `IMAGE_TAG` | `latest` | Docker image tag to build, push and deploy. |
| `BACKEND_PROJECT_DIR` | — | Path to the Maven backend project. |
| `FRONTEND_PROJECT_DIR` | — | Path to the Next.js frontend project. |

For example, to deploy a tagged build from a different checkout:

```bash
export IMAGE_TAG=v1.2.0
export BACKEND_PROJECT_DIR=/Users/me/model-city-back-end
export FRONTEND_PROJECT_DIR=/Users/me/model-city-front-end
./deploy.sh
```

## What the scripts do not do

- They do **not** create the `LabRole` IAM role. See
  [AWS deployment](../how-to-start/aws-deployment.md) for that.
- They do **not** register or renew your DNS records. After the first
  deployment you must point the `domain` A/CNAME record at the ALB DNS name
  printed by the script.
- They do **not** import certificates into ACM. Run the certificate scripts
  described in [TLS and certificates](./certificates.md) first and then copy
  the ARN into `terraform.tfvars`.

## Tips

- Keep an eye on CloudWatch Logs after the initial deployment:
  ```bash
  aws logs tail /ecs/modelcity-academy/core --follow --region us-east-1
  ```
- If a deployment fails because an image is not found, make sure the ECR
  repository was created by `terraform apply` and that the image was pushed
  successfully.
- For AWS Academy, if `check_ecr_access` reports that credentials have
  expired, restart the lab and update `~/.aws/credentials` before continuing.
