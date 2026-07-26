---
title: Modules & REST API
sidebar_label: Overview
sidebar_position: 1
---

# Modules & REST API

This section documents the REST API each vertical exposes. Every operation has its own page
with a **description**, its **inputs** and **outputs**, and the **sequence diagrams for both
topologies** (microservices and monolith). The diagrams and payloads are adapted from the
platform's sequence material and verified against the controllers in the `*-domain` modules.

For the structural picture behind these APIs — the topology split, the data model, the cache,
i18n, extensibility and audit — see the [Architecture](../architecture/) section.

## Verticals

| Vertical | Scope | Base path |
| --- | --- | --- |
| [core](./core/) | Users (JIT provisioning, mTLS certificate), OTP / operation authorizations, audit | `/api/core` |
| engagement | Civic questions & answers, security alerts | `/api/engagement` |
| leisure | City places & routes, public spaces & reservations, events & tickets | `/api/leisure` |
| mobility | Cars, street reservations, sanctions | `/api/mobility` |

:::info[Migration status]

The **core** vertical is documented. `engagement`, `leisure` and `mobility` are being migrated
from `documentation/seed-data/back-end/sequence/` next.

:::

## Conventions common to every operation

These apply to all endpoints unless a page says otherwise, so the per-operation pages don't
repeat them.

### Entry edge and identity

The client always sends the **Auth0 access token** as a Bearer JWT. The token is validated at
the entry edge, which injects the caller's `sub` as the **`X-Auth-Sub`** header before the
request reaches the controller:

- **Microservices** — the **Gateway** (the single OAuth2 Resource Server) validates the JWT
  (RS256 via JWKS) and routes `/api/{vertical}/**` by Eureka.
- **Monolith** — an in-process security filter (`MonolithSecurityConfig` +
  `XAuthSubFilterServlet`) validates the JWT; `MonolithRoutingConfig` keeps the
  `/api/<vertical>` prefixes.

Controllers declare **relative** paths (e.g. `@RequestMapping("/users")`); the `/api/<vertical>`
prefix is applied by the routing layer. See [Dual topology](../architecture/dual-topology.md).

### Role verification

Endpoints annotated with `@ModelCityAccess.*` (e.g. `@ModelCityAccess.PlatformAdmin`) are
guarded by the `ModelCityAccessAspect` `@Before` advice, which resolves the caller's role
through `CoreClient` **before** the method body runs. If the role is not allowed it returns
`403`. The resolution chain differs by topology (HTTP to `core` in microservices, in-process in
the monolith) but the outcome is identical; the diagrams show it as a single
`ModelCityAccessAspect → CoreClient` interaction.

### Internationalization

Localizable reads resolve to the request's `Accept-Language` (`es`/`en`/`fr`, default `es`,
per-field fallback to `es`) and echo `Content-Language`. Writes take translatable fields as
`language → text` maps with `es` required. See
[Internationalization](../architecture/internationalization.md).

### Pagination

List endpoints return the standard Spring Data `Page` envelope (serialized via
`PageJacksonModule`), **20 items per page**, with a 0-based `page` query parameter.

### Error format

Errors use the shared `ApiErrorResponse` body (the Stripe webhooks are the only exception —
they return literal JSON by contract with Stripe):

```json
{
  "timestamp": "2026-06-17T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "path": "/users",
  "message": "Neighbourhood not found: el-recreo-norte"
}
```

### Caching and audit as diagram notes

Caching (Valkey) and audit trails appear in the diagrams as notes (e.g. "served from cache",
"invalidates cache …", "audit → `<vertical>_trails`"). The cache is a no-op when
`modelcity.cache.enabled=false`. See [Cache](../architecture/cache.md) and
[Audit trails](../architecture/audit-trails.md).
