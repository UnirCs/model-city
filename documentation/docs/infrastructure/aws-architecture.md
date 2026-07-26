---
title: AWS architecture
sidebar_label: Architecture
sidebar_position: 2
---

# AWS architecture

Both topologies share the same **single public ALB** design and the same VPC. The
difference is what sits behind the ALB and how many databases are used.

- **Microservices** — `Spring Cloud Gateway` + `Eureka service-registry` + one
  Spring Boot service per vertical (`core`, `engagement`, `leisure`, `mobility`).
  Each vertical gets its own RDS PostgreSQL instance.
- **Monolith** — all verticals are packaged into one Spring Boot artifact that
  runs in a single ECS service and talks to a single RDS database.

The two stacks are **mutually exclusive**: they create resources with the same
names (`modelcity-academy-alb`, `modelcity-academy-cluster`, the same domain,
etc.), so only one can exist in the same account/region at a time.

## Public ALB and listeners

A single public Application Load Balancer handles all inbound traffic:

| Listener | Port | Protocol / mTLS | Default action | Rule |
| --- | --- | --- | --- | --- |
| HTTP redirect | 80 | HTTP | 301 → `https://:443` | — |
| HTTPS | 443 | HTTPS, no mTLS | forward to Next.js frontend | `/api/*` → backend |
| HTTPS + mTLS | 8443 | HTTPS, `verify` mode | forward to backend | all traffic |

The listener on **8443** uses an ALB **trust store** (`aws_lb_trust_store`) that
points to the FNMT CA bundle uploaded to S3. In `verify` mode the ALB demands a
client certificate signed by one of those CAs, validates it, and forwards the
certificate metadata to the backend in these headers:

- `X-Amzn-Mtls-Clientcert-Subject`
- `X-Amzn-Mtls-Clientcert-Issuer`
- `X-Amzn-Mtls-Clientcert-Serial-Number`
- `X-Amzn-Mtls-Clientcert-Verify`
- `X-Amzn-Mtls-Clientcert-Leaf`

The Spring Boot back-end reads the headers and implements the certificate
verification flow. The same headers are reproduced by the local Nginx setup in
[Local mTLS](../how-to-start/mtls-local.md).

## Microservices topology

![Microservices topology](/img/infrastructure/microservices-arch-aws.png)

Key points for microservices:

- **ECS** tasks run in private subnets with no public IP. Outbound traffic uses
  the NAT Gateway.
- **Gateway** (`:8762`) is the only backend service registered on the public ALB;
  the other verticals are reached through it.
- **Eureka** is exposed through an internal ALB because AWS Academy Learner Lab
  does not allow `servicediscovery:CreatePrivateDnsNamespace`. The internal ALB
  gives the registry a stable private DNS name.
- **Database-per-service**: `core`, `engagement`, `leisure` and `mobility` each
  get a dedicated `db.t3.micro` PostgreSQL instance with `JPA_DDL_AUTO=update`.
- **ElastiCache Valkey** is optional (`create_elasticache=true` by default); the
  cache is only accessible from the ECS tasks security group.

## Monolith topology

![Monolith topology](/img/infrastructure/monolith-arch-aws.png)

Key points for the monolith:

- One **ECS** service (`monolith`) runs all verticals in a single task.
- One **RDS** instance with a single logical database (`modelcity` by default).
- `JPA_DDL_AUTO=validate` is used so Hibernate does not mutate the schema on
  redeployments; the schema is expected to be created/owned by the city.
- No Eureka or internal ALB is needed; the monolith listens directly on `:8762`
  and is reached through `/api/*`.
- The monolith task is sized larger by default (`1024` CPU / `2048` MiB memory)
  because it carries every vertical.

## Networking and security groups

Both topologies create the same network foundation:

- **VPC** `10.20.0.0/16` with DNS hostnames and support enabled.
- **Public subnets** (`10.20.0.0/24`, `10.20.1.0/24`) across two AZs; the ALB
  lives here.
- **Private subnets** (`10.20.10.0/24`, `10.20.11.0/24`) across two AZs; the
  ECS tasks, ElastiCache and the internal Eureka ALB live here.
- **Internet Gateway** for public subnet outbound/inbound.
- **NAT Gateway** (single, cost-optimized) in the first public subnet so private
  tasks can pull images and call external APIs.

### Security group rules

- `alb-sg` allows `80`, `443` and `8443` from `0.0.0.0/0`.
- `ecs-tasks-sg` allows inbound only from the ALB security group (and, in
  microservices, from itself and from the Eureka ALB security group) plus
  outbound to `0.0.0.0/0`.
- `rds-sg` allows PostgreSQL (`5432`) only from the ECS tasks security group.
- `elasticache-sg` allows Valkey (`6379`) only from the ECS tasks security group.

## Container registry and images

Terraform creates **ECR** repositories under the `modelcity/` namespace. The
lifecycle policy keeps the last 10 images to stay within AWS Academy storage
quotas.

| Topology | Repositories |
| --- | --- |
| Microservices | `modelcity/service-registry`, `modelcity/gateway`, `modelcity/core`, `modelcity/engagement`, `modelcity/leisure`, `modelcity/mobility`, `modelcity/frontend` |
| Monolith | `modelcity/monolith`, `modelcity/frontend` |

Images are built locally by the deploy scripts, pushed to ECR, and then pulled
by the ECS tasks using the `LabRole` execution role.

## Logging and monitoring

Each ECS service writes to a dedicated **CloudWatch Logs** log group under
`/ecs/modelcity-academy/<service>` with a 7-day retention. The deploy scripts
print the `aws logs tail` command at the end of each deployment so you can watch
a service start up.
