---
title: Replace a persistence adapter (store)
sidebar_label: Replace a store adapter
sidebar_position: 11
---

# Replace a persistence adapter (store)

**Goal:** change **how** an aggregate is persisted or read — here, publish every created event
to an external search index — without changing the use cases or the API. The persistence
**port** is a seam, so you replace its adapter.

- **Seam:** `EventStore<T extends EventView, R extends EventRequestDto>` (`@ModelCityExtensionPoint`).
- **Door:** `extends DefaultEventStore` (reuse the mapping) or `implements EventStore<…>` (full control).
- **City files:** one `@Component`.
- **Applies to:** both topologies.

## Why the store is the right seam

Controllers and use cases never touch JPA entities; they depend on the **store port**
(`EventStore`) and the read **view** (`EventView`). The platform adapter `DefaultEventStore`
(a `@Component` marked `@ModelCityDisabledIfInherited`) maps the port onto the JPA repository.
Replacing it lets you add side effects, change queries, or read from a different source, while
every caller stays untouched.

## Recipe — reuse the platform mapping, add a side effect

Extend the default adapter and override the one method you care about.

```java
// com.modelcity.leisure.events.store.MyEventStore
@Component
public class MyEventStore extends DefaultEventStore {

    private final SearchIndexClient searchIndex;   // your own bean

    public MyEventStore(EventRepository<Event> eventRepository, SearchIndexClient searchIndex) {
        super(eventRepository);
        this.searchIndex = searchIndex;
    }

    @Override
    public Event create(EventRequestDto request, String stripePriceId) {
        Event saved = super.create(request, stripePriceId);   // platform field mapping + save
        searchIndex.index("events", saved.getId(), saved.getName());
        return saved;
    }
}
```

`DefaultEventStore` is `@ModelCityDisabledIfInherited`; your subclass covers the `EventStore`
seam, so the default's bean definition is removed at startup.

## Recipe — implement the port from scratch

Implement the interface directly when you bind a **different entity subtype** (e.g. to persist
a new column, as in [Expose a new field](./expose-a-new-api-field.md)) or replace the queries
entirely:

```java
@Component
@RequiredArgsConstructor
public class MyEventStore implements EventStore<Event, EventRequestDto> {

    private final EventRepository<Event> repository;   // or your own MyEventRepository

    @Override public Optional<Event> findActiveById(Long id) { return repository.findByIdAndActiveTrue(id); }
    @Override public Page<Event> search(EventType type, Boolean paid, LocalDateTime now, Pageable pageable) {
        return repository.search(type, paid, now, pageable);
    }
    // create / update / softDelete — do the field mapping (mirror DefaultEventStore) plus your logic
}
```

## Notes

- **The store is on the shared `*-domain` for non-cross-vertical aggregates** (like events);
  the topology-divergent stores (tickets, reservations — the ones that reference `users`) live
  in `*-domain-{microservices,monolith}`. You override in your city app either way; the seam is
  the same.
- **Keep the port contract.** Return the same `EventView` shape callers expect. Persistence is
  the only thing you're changing.
- Side effects in a store method run **inside the surrounding transaction** — if you need
  after-commit semantics, publish a domain event instead.

## Verify

Create an event; it is persisted exactly as before **and** your index receives it. `GET /events`
and every use case behave identically, and the log shows `DefaultEventStore … disabled`.
