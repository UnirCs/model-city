---
title: Getting started
sidebar_label: Overview
sidebar_position: 1
---

# Getting started

This section walks you through everything you need to stand up a Model City
deployment from scratch — as a developer, on your own machine and your own cloud
accounts.

There are two kinds of task here:

1. **Provision the external accounts and services** the platform integrates with,
   collecting the environment variables each one produces.
2. **Scaffold the code base** for your city from the published platform
   artifacts (Maven archetypes for the back-end, `create-model-city-app` for the
   front-end).

## Suggested order

| # | Guide | What you get |
| --- | --- | --- |
| 1 | [Scaffold the code base](./scaffolding.md) | The front-end and back-end projects for your city |
| 2 | [Local services (PostgreSQL & Valkey)](./local-services.md) | A database and cache to run against |
| 3 | [Auth0](./auth0.md) | Identity, JWT validation and the `AUTH0_*` variables |
| 4 | [Stripe](./stripe.md) | Payments and the `STRIPE_*` variables |
| 5 | [Gmail (SMTP)](./gmail.md) | Transactional email and the `MAIL_*` variables |
| 6 | [Local mTLS](./mtls-local.md) | An Nginx setup that simulates the AWS ALB client-certificate flow |
| 7 | [AWS deployment](./aws-deployment.md) | The IAM identity and role the deploy scripts need |

## Environment variables at a glance

Each integration guide ends with the exact variables it produces. They are
consumed either as container environment variables (local runs) or through
`infrastructure/terraform/aws*/terraform.tfvars` (AWS deployments).

- **Auth0** → `AUTH0_DOMAIN`, `AUTH0_ISSUER_URI`, `AUTH0_AUDIENCE`,
  `AUTH0_CLIENT_ID/SECRET`, `AUTH0_MGMT_CLIENT_ID/SECRET`, `AUTH0_DB_CONNECTION`,
  `AUTH0_*_ROLE_ID`, `AUTH0_SECRET`.
- **Stripe** → `STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY`,
  `STRIPE_WEBHOOK_SECRET`, `STRIPE_RESERVATION_WEBHOOK_SECRET`,
  `STRIPE_PARKING_PRODUCT_ID`.
- **Gmail** → `MAIL_USERNAME`, `MAIL_PASSWORD` (plus optional `MAIL_CITY_NAME`,
  `MAIL_ADDRESS`).
- **Local services** → `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CACHE_ENABLED`,
  `VALKEY_HOST`, `VALKEY_PORT`.

:::caution[Secrets]

`*_CLIENT_SECRET`, `AUTH0_SECRET`, `STRIPE_SECRET_KEY`, `*_WEBHOOK_SECRET` and
`MAIL_PASSWORD` are **secrets**. Never commit them to a public repository. Domain,
issuer, audience and role IDs are configuration, not secrets.

:::
