---
title: Add a brand-new endpoint
sidebar_label: Add a new endpoint
sidebar_position: 9
---

# Add a brand-new endpoint

**Goal:** add an endpoint the platform doesn't have — here, **bulk import** events via
`POST /events/import` — by extending the controller and declaring a new mapping.

- **Seam:** the abstract `EventController` base.
- **City files:** one `@RestController` (the same subclass you'd use to override endpoints).
- **Applies to:** both topologies.

## Recipe

The city subclass already exists once you extend `EventController`; add a `@…Mapping` method to
it. New endpoints can reuse the **inherited `protected`** use-case fields
(`createEventUseCase`, …) or inject your own beans.

```java
// com.modelcity.leisure.events.controller.MyEventController
@RestController
public class MyEventController extends EventController<EventDto, EventSummaryDto, EventRequestDto> {

    public MyEventController(/* the six platform use cases, as in the default */) {
        super(/* … */);
    }

    /** Brand-new endpoint: POST /events/import — create many events at once. */
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    public List<EventDto> importEvents(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid List<EventRequestDto> requests,
            Locale locale) {
        String code = SupportedLocale.from(locale).code();
        return requests.stream()
                .map(request -> createEventUseCase.execute(sub, request, code))   // inherited field
                .toList();
    }
}
```

The class-level `@RequestMapping("/events")` is inherited, so the new path is
`POST /events/import`.

## Notes

- **Guard it.** A new write endpoint that touches admin operations should carry a
  `@ModelCityAccess.*` annotation — it is enforced by the same aspect as the platform
  endpoints (which resolves the caller's role from `X-Auth-Sub` via `CoreClient`).
- **Need new logic?** Inject a [new use case](./add-a-new-use-case.md) instead of looping an
  existing one; keep controllers thin.
- Adding an endpoint requires the controller subclass to exist, which disables
  `DefaultEventController` — the inherited endpoints keep working because they are inherited,
  not re-declared.

## Verify

```bash
curl -X POST /events/import -H 'X-Auth-Sub: <admin>' -H 'Content-Type: application/json' \
  -d '[ { "...": "..." }, { "...": "..." } ]'
```

returns `201` with the created events; a non-admin caller gets `403`.
