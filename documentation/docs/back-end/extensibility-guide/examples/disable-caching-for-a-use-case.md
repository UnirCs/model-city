---
title: Disable caching for a single use case
sidebar_label: Disable caching for a use case
sidebar_position: 13
---

# Disable caching for a single use case

**Goal:** make **one** cached use case stop using the cache — here, always serve
`GET /events` live — while every other cache keeps working with its defaults.

- **Seam:** the use-case interface (or its `Default*`).
- **City files:** one `@Service`.
- **Applies to:** both topologies.

## The key fact

`@Cacheable` / `@CacheEvict` live on the platform **default** implementation and act on the
**method that is actually invoked**. Annotations are **not** inherited across an override, so
an override that simply **omits** `@Cacheable` is not cached — and because you only replace that
one use case, every other cache (`cityPlaces`, `userProfile`, the event *detail* cache, …) is
untouched.

:::note[There is no per-cache "off" switch in YAML]

`modelcity.cache.*` exposes a **global** toggle (`enabled`), a `default-ttl` and per-cache
**TTLs** (`ttls.<name>`) — but no per-cache *disable* flag. Turning off caching for one
operation is an **override**, not a config line. (You *can* set a very short TTL — see
[Tune the cache](./tune-the-cache.md) — but that is throttling, not disabling.)

:::

## Recipe

Extend the default and override the method, dropping `@Cacheable` while keeping
`@Transactional`. Delegating to `super` reuses the platform body; the `super` call is a
self-invocation, so the default's `@Cacheable` never fires.

```java
// com.modelcity.leisure.events.usecase.UncachedGetEventsUseCase
@Service
public class UncachedGetEventsUseCase extends DefaultGetEventsUseCase {

    public UncachedGetEventsUseCase(EventStore<? extends EventView, EventRequestDto> eventStore) {
        super(eventStore);
    }

    @Override
    @Transactional(readOnly = true)     // kept; @Cacheable intentionally omitted → not cached
    public Page<EventSummaryDto> execute(EventType type, Boolean paid, int page, String locale) {
        return super.execute(type, paid, page, locale);
    }
}
```

`DefaultGetEventsUseCase` is `@ModelCityDisabledIfInherited`; your subclass covers the
`GetEventsUseCase` seam, so the default is removed at startup — no duplicate, no `events`
caching on this path.

## Notes

- **Where the annotation lives decides what you override.** The `events` *list* cache is on the
  use case (as above). The `event` *detail* cache is on a separate `@Component`
  (`CachedEventReader`) injected into `DefaultGetEventUseCase` — to bypass **that** one, your
  override of `GetEventUseCase` must read without going through `CachedEventReader`.
- **The opposite direction is symmetric:** an override that *wants* caching must **re-declare**
  `@Cacheable` (see [Override a read use case](./override-a-read-use-case.md)).
- Writes evict via `@CacheEvict`. If you disable a read cache, you can leave the matching evicts
  in place — evicting a cache nobody populates is harmless.

## Verify

Enable the cache (`CACHE_ENABLED=true`) and hit `GET /events` twice: with the default it is
served from Valkey on the second call; with this override every call hits PostgreSQL, while a
second `GET /city-places` is still cache-served.
