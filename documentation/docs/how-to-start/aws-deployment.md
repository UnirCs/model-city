---
title: AWS deployment
sidebar_label: AWS deployment
sidebar_position: 8
---

# AWS configuration for deployment

The scripts `infrastructure/deploy.sh` (microservices) and
`infrastructure/deploy-monolith.sh` (monolith) use Terraform + AWS CLI + Docker to
provision and deploy the whole platform. They were originally written for **AWS
Academy**, which provides a temporary account with a preexisting role called
**`LabRole`** and session credentials pasted into `~/.aws/credentials`.

On a **standalone AWS account** (freshly created) you must reproduce those two
elements:

1. **The `LabRole` role**: an ECS role (execution + task) that Terraform references
   by name (`data.aws_iam_role.lab`, variable `lab_role_name`, default `LabRole`)
   and which the one-off database initialization task also uses.
2. **A deployment identity with permissions**: an IAM user with a policy covering
   everything the scripts do, and its **access keys** written to a credentials
   file.

:::note[Important nuance]

**Access keys belong to an IAM user, not a role.** A role is "assumed" and yields
temporary credentials (this is what AWS Academy does). For a `.credentials` file
with long-lived static keys, the identity must be an **IAM user**. Here we create
both: the *task role* `LabRole` and the *deployment user* with its keys.

:::

## What the scripts do (AWS services involved)

Terraform creates: VPC, subnets, IGW, NAT Gateway, EIP, route tables, security
groups (EC2); ALB, listeners, target groups, mTLS trust store (ELBv2);
repositories and lifecycle policies (ECR); cluster, task definitions and services
(ECS); instance and subnet group (RDS); replication group and subnet group
(ElastiCache); bucket, objects and public access block (S3); log groups
(CloudWatch Logs); and references a certificate (ACM). The scripts also run via
CLI: `sts get-caller-identity`, `ecr get-login-password`,
`ecs register-task-definition/run-task/update-service/...`,
`s3 cp/presign/rm`, `logs tail`, and `ec2 describe-*` queries.

---

## 1. Local prerequisites

1. Install the tools the script checks (`check_prerequisites`): `aws` (CLI v2),
   `terraform`, `docker`, `mvn` (Maven) and `jq`.
2. Verify: `aws --version && terraform -version && docker --version && mvn -v && jq --version`.

## 2. Create the ECS role `LabRole`

This role is assumed by ECS (Fargate) tasks to pull images from ECR and write
logs; the *db-init* task uses it as *execution* and *task* role.

### 2.1. Trust policy (who can assume it)

Save as `labrole-trust.json`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Service": "ecs-tasks.amazonaws.com" },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

### 2.2. Creation and permissions

```bash
# Create the role
aws iam create-role \
  --role-name LabRole \
  --assume-role-policy-document file://labrole-trust.json

# Standard ECS task execution permissions (ECR pull + logs)
aws iam attach-role-policy \
  --role-name LabRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
```

`AmazonECSTaskExecutionRolePolicy` does **not** include `logs:CreateLogGroup`, and
the *db-init* task starts with `awslogs-create-group: true`. Add an inline policy
for it:

```bash
aws iam put-role-policy \
  --role-name LabRole \
  --policy-name AllowCreateLogGroup \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      { "Effect": "Allow", "Action": "logs:CreateLogGroup", "Resource": "*" }
    ]
  }'
```

> If you prefer a different role name, create it with that name and add
> `lab_role_name = "<your-role>"` to `terraform.tfvars`.

## 3. Create the deployment user and its permission policy

### 3.1. Create the user

```bash
aws iam create-user --user-name modelcity-deployer
```

### 3.2. Grant permissions

You have three options, from simplest to most restricted.

**Option A — Maximum simplicity (personal/academic account):** attach
`AdministratorAccess`. Covers everything the scripts do with no tuning.

```bash
aws iam attach-user-policy --user-name modelcity-deployer \
  --policy-arn arn:aws:iam::aws:policy/AdministratorAccess
```

**Option B — Scoped by service (recommended):** attach the managed policies of the
services touched:

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

`IAMFullAccess` is needed because Terraform reads the `LabRole` role
(`iam:GetRole`) and assigns it to the task definitions (`iam:PassRole`). To
restrict IAM, replace it with an inline policy allowing only `iam:GetRole`,
`iam:PassRole` and `iam:ListRoles` on `LabRole` (see Option C).

**Option C — Least-privilege IAM:** instead of `IAMFullAccess`, attach the other
Option B policies and add this inline policy:

```bash
aws iam put-user-policy --user-name modelcity-deployer \
  --policy-name PassLabRole \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": ["iam:GetRole", "iam:PassRole", "iam:ListRoles"],
        "Resource": "arn:aws:iam::*:role/LabRole"
      }
    ]
  }'
```

## 4. Generate the access keys and write them to a credentials file

1. Create the user's keys:
   ```bash
   aws iam create-access-key --user-name modelcity-deployer
   ```
2. The output contains `AccessKeyId` and `SecretAccessKey`. **The secret is shown
   only once.**
3. Write those values to your credentials file (`~/.aws/credentials` or the
   `.credentials` you use). Format:
   ```ini
   [default]
   aws_access_key_id     = AKIAxxxxxxxxxxxxxxxx
   aws_secret_access_key = xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   region                = us-east-1
   ```
   > Unlike AWS Academy, your own IAM user does **not** use `aws_session_token`:
   > only the first two lines. The scripts' default region is `us-east-1`
   > (`AWS_REGION`).
4. If you use a named profile instead of `[default]`, export
   `AWS_PROFILE=<profile>` before launching the script.

## 5. Verification

1. Check the identity (exactly what `check_aws_credentials` does):
   ```bash
   aws sts get-caller-identity
   ```
   It should return the account and the ARN of the `modelcity-deployer` user.
2. Check ECR access (what `check_ecr_access` does):
   ```bash
   aws ecr get-authorization-token --region us-east-1 >/dev/null && echo OK
   ```
3. Confirm the role exists:
   ```bash
   aws iam get-role --role-name LabRole
   ```

## 6. Additional requirements before `deploy.sh`

Independent of IAM, but needed for the deployment to finish (see
`terraform.tfvars`):

1. **ACM certificate** referenced by `acm_certificate_arn`: the ALB needs a TLS
   certificate. Import it into ACM (Let's Encrypt) with the script
   `infrastructure/scripts/import-cert-to-acm.sh`; copy the resulting ARN into
   `terraform.tfvars`. The deployment policy already includes
   `AWSCertificateManagerFullAccess`.
2. **Domain (use DuckDNS for testing)** (`duckdns_domain`. Change the variable name if desired): after deployment, point the domain to the
   ALB's IP (the script prints the update `curl` command at the end).
3. **Complete `terraform.tfvars`** with the Stripe, Auth0 and mail secrets (see
   [Stripe](./stripe.md), [Auth0](./auth0.md) and [Gmail](./gmail.md)).

## 7. Execution

1. Fill in `infrastructure/terraform/aws/terraform.tfvars` (micro) or
   `infrastructure/terraform/aws-monolith/terraform.tfvars` (monolith).
2. Launch the interactive menu:
   ```bash
   ./infrastructure/deploy.sh            # microservices
   ./infrastructure/deploy-monolith.sh   # monolith
   ```
3. Choose **1) Initial deployment**. The script does: `terraform apply` → DB init
   (one-off ECS task with `LabRole`) → build/push images to ECR →
   `ecs update-service` to deploy.
4. To tear everything down: option **4) Cleanup** (`terraform destroy`).

:::caution[Mutually exclusive stacks]

The two stacks share resource names (same ALB/cluster/RDS/domain) and are
**mutually exclusive**: destroy one before deploying the other.

:::
