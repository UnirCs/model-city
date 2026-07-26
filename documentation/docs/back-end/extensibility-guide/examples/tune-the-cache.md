---
title: Tune the cache (toggle & TTLs)
sidebar_label: Tune the cache
sidebar_position: 14
---

# Tune the cache (toggle & TTLs)

**Goal:** change caching **through configuration** — enable it, change the default TTL, and
override the TTL of individual caches — without writing any Java.

- **City files:** the deployable's `application.yml` (or environment variables).
- **Applies to:** both topologies (identical properties).

## The knobs

The cache is off by default and driven by `modelcity.cache.*` (defaults shipped in the
imported `modelcity-shared.yml`) plus the standard `spring.data.redis.*` connection:

```yaml
modelcity:
  cache:
    enabled: ${CACHE_ENABLED:false}     # master switch — nothing is cached while false
    default-ttl: ${CACHE_DEFAULT_TTL:30m}
    ttls:                               # per-cache TTL overrides, keyed by cache name
      events: 1m                        # platform default for this group is 5m
      cityPlaces: 2h                    # platform default is 60m
      userProfile: 5m
```

- **`enabled`** — when `false` (default) no `CacheManager` is registered, so every `@Cacheable`
  / `@CacheEvict` across the modules is inert and each call hits the database.
- **`default-ttl`** — applied to any cache without a specific entry.
- **`ttls.<cacheName>`** — overrides the built-in TTL of one cache. The canonical names live in
  `CacheNames` (e.g. `event`, `events`, `cityPlace`, `cityPlaces`, `userProfile`, `sanction`…);
  see the table in [Cache](../../architecture/cache.md#cache-names-and-ttls).

## As environment variables

The archetype wires the toggle and connection to env vars, so production usually sets:

```bash
CACHE_ENABLED=true
CACHE_DEFAULT_TTL=30m
VALKEY_HOST=my-valkey.internal
VALKEY_PORT=6379
# per-cache TTLs use the standard relaxed binding:
MODELCITY_CACHE_TTLS_EVENTS=1m
```

## Notes

- **TTL is not a disable.** Setting a tiny TTL throttles staleness; to make one operation ignore
  the cache entirely, override it (see
  [Disable caching for a use case](./disable-caching-for-a-use-case.md)).
- **Names must match `CacheNames`.** A typo in `ttls.<name>` silently does nothing (that cache
  just keeps its group default).
- **Keys are per language** (`events::en-MUSIC-true-0`, …) — TTL applies per entry, so tuning a
  TTL affects all locales of that cache. See
  [Internationalization](../../architecture/internationalization.md).
- Replacing the whole caching strategy (e.g. disabling dynamic cache creation, or dropping a
  cache) means providing your own `cacheManager` bean — the platform one is
  `@ConditionalOnMissingBean(name = "cacheManager")`, so a city bean of that name wins. That is
  the bean-level cousin of [overriding a config property](./override-a-config-property.md).

## Verify

Start with `CACHE_ENABLED=true`; the Valkey health indicator turns up and a second identical
`GET /events` within the `events` TTL is served from cache. Lower `ttls.events` and confirm the
entry expires sooner.
