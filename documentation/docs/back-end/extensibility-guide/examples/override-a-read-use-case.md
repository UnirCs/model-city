---
title: Override a read use case
sidebar_label: Override a read use case
sidebar_position: 5
---

# Override a read use case

**Goal:** change the behaviour of one read — here, list events **12 per page sorted by newest**
instead of the platform's 6-by-start-date — without touching the controller, the store or any
other use case.

- **Seam:** `GetEventsUseCase<T extends EventSummaryDto>` (single-method use-case interface).
- **City files:** one `@Service`.
- **Applies to:** both topologies.

## Recipe

Declare a `@Service` implementing the interface anywhere under `com.modelcity.<vertical>`.
`DefaultGetEventsUseCase` backs off automatically.

```java
// com.modelcity.leisure.events.usecase.MyGetEventsUseCase
@Service
@RequiredArgsConstructor
public class MyGetEventsUseCase implements GetEventsUseCase<EventSummaryDto> {

    private static final int PAGE_SIZE = 12;   // was 6

    private final EventStore<? extends EventView, EventRequestDto> eventStore;

    @Override
    @Cacheable(cacheNames = CacheNames.EVENTS, key = "#locale + '-' + #type + '-' + #paid + '-' + #page")
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> execute(EventType type, Boolean paid, int page, String locale) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("startsAt").descending());
        return eventStore.search(type, paid, LocalDateTime.now(), pageable)
                .map(e -> EventSummaryDto.from(e, locale));
    }
}
```

## Notes

- **Re-declare `@Cacheable` / `@Transactional`.** They live on `DefaultGetEventsUseCase`, and
  annotations are **not** inherited across separate implementations. Omit `@Cacheable` on
  purpose to [disable caching for this use case](./disable-caching-for-a-use-case.md) while the
  rest keep theirs.
- **Reuse the same ports and factories** (`EventStore`, `EventSummaryDto.from`) — you are
  changing policy, not re-plumbing persistence.
- To also return **extra fields**, this becomes an
  [expose-a-new-field](./expose-a-new-api-field.md) job (subtype the DTO and bind the
  controller).
- Overriding is **whole-method**: implement the smallest use case that isolates your change,
  not a coarse one, so you inherit platform fixes to the others.

## Verify

`npm`-free: start the app and `curl "/events?page=0"` — 12 items, newest first. A
non-overridden read (e.g. `GET /city-places`) is unchanged, and the startup log shows
`Model City default 'defaultGetEventsUseCase' … disabled: seam GetEventsUseCase is covered by
bean 'myGetEventsUseCase'`.
