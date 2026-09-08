# Reference — AWS deployment

`infrastructure/deploy.sh` (microservices) and `infrastructure/deploy-monolith.sh`
(monolith) use Terraform + AWS CLI + Docker to provision and deploy the whole
platform. They were written for AWS Academy (temporary account, preexisting
`LabRole`, session credentials). On a **standalone AWS account** you must reproduce
both pieces yourself: the `LabRole` ECS role, and a deployment IAM **user** (not a
role — roles yield temporary credentials; a `.credentials` file needs long-lived
user keys).

**Every command in sections 2–4 creates a real IAM identity or role. Every command in
section 7 creates or destroys real, billed infrastructure (VPC, ALB, ECS, RDS,
ElastiCache, ECR, S3, CloudWatch). State exactly what a command will do and get an
explicit go-ahead before running it — one confirmation per command, not a blanket
confirmation up front.**

Terraform creates: VPC/subnets/IGW/NAT/EIP/route tables/security groups (EC2);
ALB/listeners/target groups/mTLS trust store (ELBv2); repositories (ECR);
cluster/task definitions/services (ECS); instance/subnet group (RDS); replication
group (ElastiCache); bucket (S3); log groups (CloudWatch); references an ACM
certificate.

## 1. Prerequisites (read-only, run freely)

```bash
aws --version && terraform -version && docker --version && mvn -v && jq --version
```

## 2. Create the ECS role `LabRole` — confirm before running

Trust policy `labrole-trust.json`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    { "Effect": "Allow", "Principal": { "Service": "ecs-tasks.amazonaws.com" }, "Action": "sts:AssumeRole" }
  ]
}
```

```bash
aws iam create-role --role-name LabRole --assume-role-policy-document file://labrole-trust.json

aws iam attach-role-policy --role-name LabRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# AmazonECSTaskExecutionRolePolicy lacks logs:CreateLogGroup, needed by the db-init task
aws iam put-role-policy --role-name LabRole --policy-name AllowCreateLogGroup \
  --policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Action":"logs:CreateLogGroup","Resource":"*"}]}'
```

A different role name is fine — set `lab_role_name = "<name>"` in `terraform.tfvars`.

## 3. Deployment user — confirm before running

```bash
aws iam create-user --user-name modelcity-deployer
```

Ask the user which permission strategy they want:

- **A — simplest** (personal/academic account): `AdministratorAccess`.
  ```bash
  aws iam attach-user-policy --user-name modelcity-deployer \
    --policy-arn arn:aws:iam::aws:policy/AdministratorAccess
  ```
- **B — scoped by service (recommended)**:
  ```bash
  for p in \
    arn:aws:iam::aws:policy/AmazonEC2FullAccess \
    arn:aws:iam::aws:policy/AmazonECS_FullAccess \
    arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryFullAccess \
    arn:aws:iam::aws:policy/ElasticLoadBalancingFullAccess \
    arn:aws:iam::aws:policy/AmazonRDSFullAccess \
    arn:aws:iam::aws:policy/AmazonElastiCacheFullAccess \
    arn:aws:iam::aws:policy/AmazonS3FullAccess \
    arn:aws:iam::aws:policy/CloudWatchLogsFullAccess \
    arn:aws:iam::aws:policy/AWSCertificateManagerFullAccess \
    arn:aws:iam::aws:policy/IAMFullAccess ; do
    aws iam attach-user-policy --user-name modelcity-deployer --policy-arn "$p"
  done
  ```
  (`IAMFullAccess` is needed because Terraform reads and passes `LabRole`.)
- **C — least-privilege IAM**: same as B minus `IAMFullAccess`, plus:
  ```bash
  aws iam put-user-policy --user-name modelcity-deployer --policy-name PassLabRole \
    --policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Action":["iam:GetRole","iam:PassRole","iam:ListRoles"],"Resource":"arn:aws:iam::*:role/LabRole"}]}'
  ```

## 4. Access keys — confirm before running, never re-print the secret

```bash
aws iam create-access-key --user-name modelcity-deployer
```

The `SecretAccessKey` in the output is shown **only once** — write it straight into
the credentials file the user names (usually `~/.aws/credentials`), don't echo it
again afterward:

```ini
[default]
aws_access_key_id     = AKIA...
aws_secret_access_key = ...
region                = us-east-1
```

No `aws_session_token` (that's only for AWS Academy's assumed-role credentials). If
using a named profile, `export AWS_PROFILE=<profile>` before `deploy.sh`.

## 5. Verification (read-only, run freely)

```bash
aws sts get-caller-identity
aws ecr get-authorization-token --region us-east-1 >/dev/null && echo OK
aws iam get-role --role-name LabRole
```

## 6. Before `deploy.sh`: remaining `terraform.tfvars` inputs

- **ACM certificate**: import via `infrastructure/scripts/import-cert-to-acm.sh`
  (Let's Encrypt), copy the ARN into `acm_certificate_arn`.
- **Domain** (DuckDNS is fine for testing, `duckdns_domain`): point it at the ALB's
  IP after deployment (the script prints the update `curl`).
- **Secrets**: Stripe, Auth0, mail values collected in stage 4 of this skill.

## 7. Execution — confirm before every apply/destroy

```bash
./infrastructure/deploy.sh            # microservices
./infrastructure/deploy-monolith.sh   # monolith
```

Menu option **1) Initial deployment**: `terraform apply` → one-off DB-init ECS task
(`LabRole`) → build/push images to ECR → `ecs update-service`. Option **4) Cleanup**:
`terraform destroy` — confirm explicitly, this deletes the whole stack.

The two stacks (microservices/monolith) share resource names (ALB/cluster/RDS/domain)
and are **mutually exclusive** — destroy one before deploying the other. Confirm with
the user which one is active before running `deploy.sh` for the other topology.
