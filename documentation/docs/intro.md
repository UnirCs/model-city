---
slug: /
title: Model City platform
sidebar_label: Overview
sidebar_position: 1
description: >-
  Model City is a full-stack platform for building a city's digital services:
  a Next.js front-end, a Spring Boot back-end (microservices or monolith) and
  Terraform/AWS infrastructure, scaffolded from published archetypes and
  customised through well-defined extension points.
---

# Model City

**Model City** is a full-stack platform for building a city's digital services.
A concrete city is not forked from a reference project — it is **scaffolded** from
published platform artifacts (Maven archetypes for the back-end, an npm
`create-*` generator for the front-end) and then customised through well-defined
extension points.

This site is the developer documentation for the whole platform.

## What a city is made of

| Layer | Technology | How a city gets it |
| --- | --- | --- |
| **Front-end** | Next.js app consuming `@modelcity/*` npm packages | `npm create model-city-app` |
| **Back-end** | Spring Boot (Java) — microservices *or* a monolith | Maven archetypes |
| **Infrastructure** | Terraform + AWS (ECS, RDS, ElastiCache, ALB, S3) | `deploy.sh` scripts |

The back-end ships two topologies from the same domain code:

- **Microservices** — a Eureka registry, an API gateway and one Spring Boot app
  per vertical (`core`, `engagement`, `leisure`, `mobility`).
- **Monolith** — the same verticals assembled into a single deployable.

The feature verticals are:

- **core** — identity, users, roles, citizen onboarding (always present).
- **leisure** — events, sports spaces and tourism.
- **engagement** — participation (consultations) and security (alerts).
- **mobility** — citizen stays/cars and staff ticket/sanction operations.

## Documentation sections

- **[Getting started](./how-to-start/)** — everything you need to stand up a city
  from scratch: the external accounts (Auth0, Stripe, Gmail), local services,
  local mTLS, AWS, and how to scaffold the code base from the archetypes.
- **[Front-end](./front-end/)** — architecture of the Next.js platform packages
  and the per-city app.
- **[Back-end](./back-end/)** — domain architecture, verticals, persistence,
  extensibility, observability and i18n.
- **[Infrastructure](./infrastructure/)** — the Terraform stacks and the deploy
  workflow on AWS.
