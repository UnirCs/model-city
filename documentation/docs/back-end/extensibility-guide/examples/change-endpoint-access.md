---
title: Change an endpoint's role restriction
sidebar_label: Change endpoint access
sidebar_position: 10
---

# Change an endpoint's role restriction

**Goal:** change **who** may call an existing endpoint — here, restrict creating events to
**platform admins only** (the platform allows admin *or* backoffice) — by overriding the method
and re-declaring its access annotations.

- **Seam:** the abstract `EventController` base + the `@ModelCityAccess.*` annotations.
- **City files:** one `@RestController`.
- **Applies to:** both topologies.

## How access is enforced

Endpoint access is declared with `@ModelCityAccess.PlatformAdmin` / `.BackOffice` / `.Operator`
/ `.MobilityAgent` and enforced by `ModelCityAccessAspect`: a `@Before` advice that reads the
annotations **on the invoked method**, fetches the caller's role from `core` via the
`X-Auth-Sub` header, and returns `403` if none match. Combining annotations means *any of them*
(logical OR).

The platform `createEvent` carries **both** admin and backoffice:

```java
// platform EventController.createEvent
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
@ModelCityAccess.PlatformAdmin
@ModelCityAccess.BackOffice
public T createEvent(...) { ... }
```

## Recipe

Override the method and declare **only** the annotation you want to keep.

```java
@RestController
public class MyEventController extends EventController<EventDto, EventSummaryDto, EventRequestDto> {

    public MyEventController(/* the six platform use cases */) { super(/* … */); }

    @Override
    @ModelCityAccess.PlatformAdmin        // backoffice removed → admins only
    public EventDto createEvent(String sub, EventRequestDto request, Locale locale) {
        return super.createEvent(sub, request, locale);   // unchanged behaviour
    }
}
```

To **open up** an endpoint instead (e.g. let operators create), add the extra annotation:
`@ModelCityAccess.PlatformAdmin @ModelCityAccess.BackOffice @ModelCityAccess.Operator`.

## Notes

:::caution[Overriding drops the annotations — re-declare them]

Access annotations are **not inherited** by an overriding method. If you override a protected
endpoint and forget the `@ModelCityAccess.*` annotations, the aspect's pointcut no longer
matches and the endpoint becomes **completely open** (no role check). Always re-declare the
intended access on any protected endpoint you override.

:::

- The role check calls `core` (HTTP in microservices, in-process in the monolith), so behaviour
  is identical in both topologies.
- This only changes the **method-level** role check. The coarse public/authenticated split
  (which paths need a JWT at all) is the gateway's / `MonolithSecurityConfig`'s job — see
  [Dual topology](../../architecture/dual-topology.md).

## Verify

With the override in place, a backoffice user calling `POST /events` now gets `403`, while a
platform admin still succeeds; `PUT /events/{id}` (not overridden) still accepts both roles.
