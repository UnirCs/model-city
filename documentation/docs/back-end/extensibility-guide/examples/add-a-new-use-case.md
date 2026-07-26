---
title: Add a brand-new use case
sidebar_label: Add a new use case
sidebar_position: 7
---

# Add a brand-new use case

**Goal:** add a capability the platform doesn't have — here, **export the current events
listing as CSV** — as a plain new bean. A brand-new use case has **no platform seam**, so
there is nothing to disable: it is just a `@Service`.

- **Seam:** none (new capability). Reuses existing seams as collaborators.
- **City files:** one `@Service`, plus an endpoint to reach it.
- **Applies to:** both topologies.

## Recipe

A new use case is an ordinary Spring bean. It can compose **existing** platform seams — here
it injects `GetEventsUseCase` (the platform default, or your override, whichever is active) and
reformats its output.

```java
// com.modelcity.leisure.events.usecase.ExportEventsCsvUseCase
@Service
@RequiredArgsConstructor
public class ExportEventsCsvUseCase {

    private final GetEventsUseCase<EventSummaryDto> getEventsUseCase;

    public String execute(EventType type, Boolean paid, String locale) {
        StringBuilder csv = new StringBuilder("id,name,startsAt\n");
        getEventsUseCase.execute(type, paid, 0, locale).forEach(e ->
                csv.append(e.getId()).append(',')
                   .append(e.getName().replace(",", " ")).append(',')
                   .append(e.getStartsAt()).append('\n'));
        return csv.toString();
    }
}
```

Expose it with a new endpoint on your overriding controller (see
[Add a new endpoint](./add-a-new-endpoint.md)):

```java
@GetMapping(value = "/export.csv", produces = "text/csv")
public String exportCsv(@RequestParam(required = false) EventType type,
                        @RequestParam(required = false) Boolean paid,
                        Locale locale) {
    return exportEventsCsvUseCase.execute(type, paid, SupportedLocale.from(locale).code());
}
```

## Notes

- **No `@ModelCityExtensionPoint` / `@ModelCityDisabledIfInherited`** — those mark *platform*
  seams and their defaults. A city-owned bean under `com.modelcity.<vertical>` is picked up by
  the existing component scan directly.
- If the new use case needs its **own persistence query**, add a repository as in
  [Add a query or filter](./add-a-query-or-filter.md); if it needs **new tables**, see
  [Add a new entity](./add-a-new-entity.md).
- You are free to keep the single-method shape (`execute`) the platform uses for consistency,
  but ArchUnit's one-method rule only applies to `@ModelCityExtensionPoint` seams, not your
  own beans.

## Verify

`curl "/events/export.csv"` returns a CSV of the first page of active events, localised to the
`Accept-Language` header.
