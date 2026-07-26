---
title: Add a new query or filter
sidebar_label: Add a query or filter
sidebar_position: 3
---

# Add a new query or filter

**Goal:** query existing data in a way the platform doesn't — here, list active events **by
venue** (`placeId`). The column already exists, so there is **no schema change**; you add a
repository query and expose it.

- **Seam:** the `@NoRepositoryBean` generic repository (bind a second concrete repository).
- **City files:** a repository, a use case, one endpoint.
- **Schema change:** none.
- **Applies to:** both topologies.

## Why a second repository

The platform store **port** (`EventStore`) is a stable seam — a city must not add methods to
it. Instead, bind the generic `EventRepository<Event>` a second time in your own interface and
add the derived query there. Spring Data happily creates a second proxy for the same entity;
`DefaultEventRepository` is untouched.

## Recipe

### 1. The repository query

```java
// com.modelcity.leisure.events.repository.MyEventQueries
public interface MyEventQueries extends EventRepository<Event> {

    Page<Event> findByPlaceIdAndActiveTrueAndStartsAtGreaterThanEqual(
            Long placeId, LocalDateTime now, Pageable pageable);
}
```

Spring Data derives the SQL from the method name — no `@Query` needed. For anything more
complex, use a `@Query("SELECT e FROM #{#entityName} e WHERE …")` exactly as
`EventRepository.search` does.

### 2. A use case (a plain new bean — no seam to override)

```java
@Service
@RequiredArgsConstructor
public class GetEventsByPlaceUseCase {

    private static final int PAGE_SIZE = 6;
    private final MyEventQueries queries;

    @Transactional(readOnly = true)
    public Page<EventSummaryDto> execute(Long placeId, int page, String locale) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("startsAt").ascending());
        return queries
                .findByPlaceIdAndActiveTrueAndStartsAtGreaterThanEqual(placeId, LocalDateTime.now(), pageable)
                .map(e -> EventSummaryDto.from(e, locale));
    }
}
```

### 3. Expose it on a new endpoint

Add it to your overriding controller (see [Add a new endpoint](./add-a-new-endpoint.md)):

```java
@RestController
public class MyEventController extends EventController<EventDto, EventSummaryDto, EventRequestDto> {

    private final GetEventsByPlaceUseCase getEventsByPlace;

    public MyEventController(/* platform use cases */ GetEventsByPlaceUseCase getEventsByPlace,
                             /* … */) {
        super(/* … */);
        this.getEventsByPlace = getEventsByPlace;
    }

    @GetMapping("/by-place/{placeId}")
    public Page<EventSummaryDto> getByPlace(@PathVariable Long placeId,
                                            @RequestParam(defaultValue = "0") int page,
                                            Locale locale) {
        return getEventsByPlace.execute(placeId, page, SupportedLocale.from(locale).code());
    }
}
```

## Notes

- **Reuse the platform view→DTO factories** (`EventSummaryDto.from`) so localisation and photo
  handling stay consistent.
- If the filter is on a **new** column, add it first as in
  [Add an internal-only field](./add-an-internal-field.md) (entity subclass + migration) and
  bind that subclass in the repository instead of `Event`.
- You **cannot** add a query parameter to an existing platform handler by overriding — the
  overriding method must keep the base signature to inherit the mapping. Add a **new** endpoint
  (as above) instead.

## Verify

`curl "/events/by-place/42?page=0"` returns only active, future events at place `42`, while
the platform `GET /events` keeps its type/paid filters.
