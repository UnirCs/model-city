---
title: Override an existing endpoint
sidebar_label: Override an endpoint
sidebar_position: 8
---

# Override an existing endpoint

**Goal:** change how one HTTP endpoint behaves at the **web layer** — here, hide the internal
`stripePriceId` from the `GET /events/{id}` response — without touching the use cases.

- **Seam:** the abstract `EventController` base (`@ModelCityExtensionPoint`).
- **City files:** one `@RestController`.
- **Applies to:** both topologies.

## Why it works

Controllers are exposed as an **abstract** base carrying `@RestController`, the class-level
`@RequestMapping("/events")` and the endpoint methods, **generic over the DTOs**. A city
extends it, `@Override`s the methods it wants, and the concrete subclass inherits the request
mappings. As soon as such a bean exists, `DefaultEventController` backs off — so there is never
a duplicate `/events` mapping.

## Recipe

Extend the base, bind the platform DTOs, and override the one method. The overriding method
keeps the **same signature** (that is how it inherits the mapping); Spring MVC inherits the
`@PathVariable` / `@RequestParam` binding from the base method, so you don't re-declare them.

```java
// com.modelcity.leisure.events.controller.MyEventController
@RestController
public class MyEventController extends EventController<EventDto, EventSummaryDto, EventRequestDto> {

    public MyEventController(
            GetEventsUseCase<EventSummaryDto> getEventsUseCase,
            GetEventUseCase<EventDto> getEventUseCase,
            GetEventForEditUseCase<EventDto> getEventForEditUseCase,
            CreateEventUseCase<EventDto, EventRequestDto> createEventUseCase,
            UpdateEventUseCase<EventDto, EventRequestDto> updateEventUseCase,
            DeleteEventUseCase deleteEventUseCase) {
        super(getEventsUseCase, getEventUseCase, getEventForEditUseCase,
                createEventUseCase, updateEventUseCase, deleteEventUseCase);
    }

    @Override
    public EventDto getEvent(Long id, String sub, String translations, Locale locale) {
        EventDto dto = super.getEvent(id, sub, translations, locale);
        dto.setStripePriceId(null);        // don't expose the Stripe price id
        return dto;
    }
}
```

The other endpoints (`getEvents`, `createEvent`, `updateEvent`, `deleteEvent`) are inherited
unchanged from `EventController`.

## Notes

- **Re-declare access annotations on any *protected* endpoint you override.** `@ModelCityAccess.*`
  is read by an aspect from the **invoked** method; method annotations are not inherited, so an
  override that omits them turns the endpoint **open**. `getEvent` is public (no annotation), so
  nothing to re-declare here — but see
  [Change an endpoint's role restriction](./change-endpoint-access.md).
- The generated project ships exactly this pattern as `ExampleCityEventController`
  (delete or adapt it).
- To change **business logic** rather than HTTP shaping, override the
  [use case](./override-a-read-use-case.md) instead — it is reused by every topology and every
  controller.

## Verify

`curl /events/{id}` no longer contains `stripePriceId`; every other event endpoint is
unchanged, and the startup log shows `DefaultEventController … disabled`.
