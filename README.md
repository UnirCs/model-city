<p align="center">
  <img src="documentation/static/img/LogoModelCity.png" alt="Model City" width="120" />
</p>

<h1 align="center">Model City</h1>

<p align="center">
  <b>Open-source platform for building the digital services of a city.</b><br />
  <a href="https://model-city-docs.vercel.app/">📖 Full developer documentation</a>
</p>

## What is Model City?

**Model City** is a full-stack platform — front-end, back-end and infrastructure —
for standing up a city's digital services. A concrete city is not *forked* from a
reference project: it is **scaffolded** from published platform artifacts (Maven
archetypes for the back-end, an npm `create-*` generator for the front-end) and then
customised through well-defined extension points, so each deployment can diverge
from the platform without losing the ability to pull in future updates.

## Goal

The goal is to give any city (or public administration) a production-ready,
batteries-included starting point covering the services citizens actually need —
identity, leisure, civic participation, security and mobility — instead of every
municipality building the same kind of portal from scratch. The platform ships as
**reusable packages and archetypes**, not as a single monolithic app to copy-paste.

## What it offers

The platform is organised in **feature verticals**:

- **core** — identity, users, roles and citizen onboarding (always present).
- **leisure** — events, sports spaces and tourism.
- **engagement** — civic participation (consultations) and security (alerts).
- **mobility** — citizen stays/vehicles and staff ticket/sanction operations.

Every vertical is available on both sides of the stack (front-end packages and
back-end domain modules) and can be composed into either of the two back-end
topologies described below.

## Front-end

`front-end-web/` — a **Next.js 16 (App Router) + React 19** app, organised as an
npm-workspaces monorepo (`packages/*` + `create-model-city-app`), styled with
**Tailwind CSS 4**.

- Ships as installable packages: `@modelcity/core`, `@modelcity/leisure`,
  `@modelcity/engagement`, `@modelcity/mobility` and a `@modelcity/cli` code
  generator, scaffolded via `create-model-city-app`.
- **Extensibility by overrides**: any component, provider or config from a
  `@modelcity/*` package can be replaced per-city by placing a file under
  `overrides/<package>/...` — no fork of the base package required.
- Built-in **internationalisation** (Spanish/English/French via a `[lang]` route
  segment), **Auth0** authentication, and a dedicated accessibility lint pass
  (`npm run a11y:lint`).

## Back-end

`back-end/` — **Java 25 + Spring Boot 4**, distributed via Maven archetypes under
the `io.github.unircs` group.

- The **same domain code** (`model-city-back-end-domain`, split per vertical) can
  be assembled into two topologies from two archetypes:
  - `model-city-back-end-microservices-archetype` — a Eureka service registry, an
    API gateway and one Spring Boot service per vertical.
  - `model-city-back-end-monolith-archetype` — every vertical assembled into a
    single deployable.
- **Extensibility by seams**: extension points are enforced and verified with
  ArchUnit architecture tests, not just documented conventions.
- Integrations already wired in: **PostgreSQL** persistence, **Auth0** auth and
  **Stripe** payments.

## Infrastructure

`infrastructure/` — **Terraform on AWS**, one self-contained stack per topology
(`microservices/` and `monolith/`), each with its own `deploy*.sh` script.

- Fully **Fargate**-based (`aws_ecs_cluster` / `aws_ecs_service`, no servers to
  manage) — 6 ECS services for the microservices topology (registry, gateway, and
  one per vertical) plus a front-end task, or a single `monolith` task plus a
  front-end task for the monolith topology.
- **RDS PostgreSQL** for persistence and **ElastiCache (Valkey)** for caching.
- An **Application Load Balancer** with HTTP/HTTPS/**mTLS** listeners, backed by an
  S3 bucket for the certificate truststore.

## Repository structure

```
model-city/
  front-end-web/     Next.js app + @modelcity/* packages + CLI generator
  back-end/           Spring Boot domain modules + microservices/monolith archetypes
  infrastructure/     Terraform (AWS) for both topologies + deploy scripts
  documentation/       Docusaurus developer documentation site
```

## Documentation

The full developer documentation — getting started guides, front-end and
back-end architecture, the component/API reference and the infrastructure
guides — lives at:

**https://model-city-docs.vercel.app/**

## License

[MIT](./LICENSE)
