---
title: Cache
sidebar_label: Cache
sidebar_position: 4
---

# Cache — Valkey / Spring Data Redis

Model City caches read-heavy master data in a **distributed cache** to keep hot reads off
PostgreSQL. The cache is **off by default** and behaves identically in both topologies.

## Stack

| Layer | Technology |
| --- | --- |
| Cache server | **Valkey** (Redis-protocol compatible) |
| Java client | **Lettuce** (managed by Spring Boot) |
| Spring dependency | `spring-boot-starter-data-redis` |
| Value serialization | `GenericJacksonJsonRedisSerializer` (Jackson 3, default typing) |
| Extra module | `PageJacksonModule` — round-trips `Page<T>` / `PageImpl` |

:::note[Why the standard Redis starter (not the Valkey fork)]

The Valkey Boot starter is a Boot 3.5 fork whose auto-configuration fails under Boot 4.
Valkey is protocol-compatible with Redis, so the standard Redis starter works unchanged.

:::

## Global toggle

The whole cache is enabled or disabled with a single property:

```yaml
modelcity:
  cache:
    enabled: ${CACHE_ENABLED:false}   # false by default
```

`ModelCityCacheAutoConfiguration` (in `model-city-commons`) is
`@ConditionalOnProperty(prefix = "modelcity.cache", name = "enabled", havingValue = "true")`
**and** `@ConditionalOnClass(RedisConnectionFactory.class)`. When disabled, no
`CacheManager` is registered, so every `@Cacheable` / `@CacheEvict` annotation across the
modules is **inert** and each call hits the database. The Redis health indicator is bound to
the same flag so a service deployed without Valkey does not report `DOWN`.

## Connection

Configured under the standard Spring Boot namespace `spring.data.redis.*`:

| Variable | Default | Purpose |
| --- | --- | --- |
| `CACHE_ENABLED` | `false` | Enables the cache and the Redis health indicator. |
| `CACHE_DEFAULT_TTL` | `30m` | Default TTL for caches without a group. |
| `VALKEY_HOST` | `localhost` | Valkey host. |
| `VALKEY_PORT` | `6379` | Port. |
| `VALKEY_USERNAME` / `VALKEY_PASSWORD` | *(empty)* | Credentials (if ACL is enabled). |
| `VALKEY_DATABASE` | `0` | Redis database index. |
| `VALKEY_TIMEOUT` | `2s` | Connection/command timeout. |

## Cache names and TTLs

The canonical cache names live in `CacheNames`; the TTL groups are defined in
`ModelCityCacheAutoConfiguration` and each can be overridden with
`modelcity.cache.ttls.<cacheName>=<duration>`.

| TTL group | Duration | Caches |
| --- | --- | --- |
| **Long** | 60 min | `cityPlace`, `cityPlaces`, `cityRoute`, `cityRoutes`, `cityRoutePlaces`, `publicSpace`, `publicSpaces`, `reservableResources`, `userCars`, `sanction`, `userSanctions` |
| **Medium** | 15 min | `publicQuestion`, `publicQuestions`, `userProfile`, `userList`, `citizenExists` |
| **Short** | 5 min | `event`, `events`, `securityAlerts` |

### Keys are per language

Every cache that serves localizable content prefixes the resolved language code to the key,
so `es`/`en`/`fr` versions never mix — e.g. `cityPlace::en-1`, `events::en-MUSIC-true-0`,
`userProfile::en-auth0|…`. Writes invalidate **all** languages of an entity (detail evicts
use `allEntries=true`, not `key="#id"`). See [Internationalization](./internationalization.md).

## Where the annotations live

`@Cacheable` / `@CacheEvict` are declared on the **use cases** (`@Service`), not the
controllers, and the pattern is symmetric across topologies. Representative examples:

| Use case | Annotation |
| --- | --- |
| `GetCityPlaceUseCase` | `@Cacheable(cityPlace, key="#id")` |
| `GetCityPlacesUseCase` | `@Cacheable(cityPlaces, key="#category + '-' + #page")` |
| `CreateCityPlaceUseCase` | `@CacheEvict(cityPlaces, allEntries=true)` + `@CacheEvict(cityRoutePlaces, allEntries=true)` |
| `GetUserUseCase` | `@Cacheable(userProfile, key="#targetUserId")` |
| `RegisterUserUseCase` | evicts `citizenExists`, `userProfile`, `userList` |

:::warning[Overrides must re-declare caching]

`@Cacheable`/`@Transactional` live on the platform **default** implementation. A city that
overrides a use case and wants the same caching must re-declare those annotations —
annotations are not inherited across separate implementations. See the
[Extensibility Guide](../extensibility-guide/index.md) and
[Disable caching for a use case](../extensibility-guide/examples/disable-caching-for-a-use-case.md).

:::

## Resilience

The cache manager installs a `ResilientCacheErrorHandler`:

- A cache **GET** error (e.g. a stale entry written by a different serializer version) is
  logged, the bad key is evicted, and the call falls through to the real method.
- **PUT / EVICT / CLEAR** errors are only logged, so a transient Valkey failure never breaks
  a write path.

Null values are not cached (`disableCachingNullValues`), and evicts are transaction-aware
(`transactionAware`), so they participate in the surrounding Spring transaction.

## Never cached

Excluded on purpose for security or high volatility: `operation-authorizations/*` (OTP),
`reservations/*` and `street-reservations/*` (live availability), `event-tickets/*` /
`citizen-tickets/*` (live stock/history), `certificate-verifications` (mTLS), and the Stripe
webhooks (non-idempotent payment effects).
