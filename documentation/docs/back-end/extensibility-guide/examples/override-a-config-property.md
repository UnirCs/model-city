---
title: Override a platform configuration property
sidebar_label: Override a config property
sidebar_position: 15
---

# Override a platform configuration property

**Goal:** change platform runtime configuration — the Stripe currency, the JPA schema check, the
log level, the Auth0 audience — from your own configuration, without touching platform code.

- **City files:** the deployable's `application.yml` (and/or environment variables, Spring
  profiles).
- **Applies to:** both topologies.

## The city owns its `application.yml`

Unlike the business code, the **configuration is generated into the city project** and belongs
to it. The archetype's `application.yml` already exposes the platform's knobs as environment
placeholders and imports shared defaults:

```yaml
spring:
  config:
    import:
      - optional:classpath:modelcity-shared.yml   # cache + Valkey + observability defaults
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:validate}

leisure:
  stripe:
    default-currency: eur                         # ← edit here, or drive it from an env var
```

You customise by **editing the value** in your `application.yml` or by **setting the environment
variable** the placeholder reads. The imported `modelcity-shared.yml` only carries
env-var-backed defaults (cache toggle/TTLs, Valkey connection, log pattern), so the effective
value of those keys is whatever env var you set.

## Common overrides

```yaml
# Stripe currency for leisure (and, in the monolith, mobility) — default is eur
leisure:
  stripe:
    default-currency: gbp

# Tighten/loosen logging (shared default is DEBUG for com.modelcity)
logging:
  level:
    com.modelcity: INFO

# Change the JPA schema check (validate | none | update…)
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

…or as environment variables (relaxed binding), which is how the archetype expects production to
be configured:

```bash
LOG_LEVEL=INFO
JPA_DDL_AUTO=validate
AUTH0_AUDIENCE=https://model-city.mycity.es
CACHE_ENABLED=true
STRIPE_SECRET_KEY=sk_live_…
```

## Per-environment overrides with Spring profiles

Add an `application-<profile>.yml` and select it with the `PROFILE` env var (the monolith app
maps `PROFILE` to `spring.profiles.active` on boot); its values override the base
`application.yml` for that environment:

```yaml
# application-prod.yml
spring:
  jpa:
    show-sql: false
logging:
  level:
    com.modelcity: WARN
```

## Notes

- **Additive, not a fork.** You are setting values on properties the platform already reads
  (`@ConfigurationProperties` like `ModelCityCacheProperties`, `StripeProperties`, standard
  Spring keys). No platform file changes.
- **Secrets stay in env vars / a secret manager**, never committed — see the
  [Getting started](../../../how-to-start/) integration guides.
- **A property isn't enough?** Some platform beans are `@ConditionalOnMissingBean`, so you can
  replace them wholesale by declaring your own bean of the same type (e.g. a custom
  `cacheManager`; see [Tune the cache](./tune-the-cache.md)). That is the escape hatch when a
  behaviour has no property.
- **Cache-specific tuning** (enable, TTLs) has its own recipe:
  [Tune the cache](./tune-the-cache.md).

## Verify

Start the app and check the effective value — e.g. a paid event's Stripe price is created in the
currency you set, and the startup banner / logs reflect the new log level. Misconfigured
`ddl-auto: validate` against a stale schema fails fast, which is the intended safety net.
