---
title: Override a write use case
sidebar_label: Override a write use case
sidebar_position: 6
---

# Override a write use case

**Goal:** add city-specific behaviour to a write — here, reject creating an event whose
capacity exceeds a city limit, then fall back to the platform logic — reusing the platform
implementation instead of re-writing it.

- **Seam:** `CreateEventUseCase<T extends EventDto, R extends EventRequestDto>`.
- **Door:** `extends DefaultCreateEventUseCase` (reuse the platform body).
- **City files:** one `@Service`.
- **Applies to:** both topologies.

## Recipe

Extend the platform default, add your rule, then delegate to `super`. Because the platform
default carries `@Transactional` **and** `@CacheEvict` on `execute`, and those annotations act
on the **called** method, you must **re-declare** them on your override — a `super.execute(…)`
call is a self-invocation and would not re-trigger them.

```java
// com.modelcity.leisure.events.usecase.MyCreateEventUseCase
@Service
public class MyCreateEventUseCase extends DefaultCreateEventUseCase {

    private static final int CITY_CAPACITY_LIMIT = 5000;

    public MyCreateEventUseCase(EventStore<? extends EventView, EventRequestDto> eventStore,
                                EventWriteValidator validator,
                                StripeFacade stripeFacade,
                                SystemTrailGenerator trails) {
        super(eventStore, validator, stripeFacade, trails);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.EVENTS, allEntries = true)
    public EventDto execute(String sub, EventRequestDto request, String locale) {
        if (request.getCapacity() != null && request.getCapacity() > CITY_CAPACITY_LIMIT) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event capacity exceeds the city limit of " + CITY_CAPACITY_LIMIT);
        }
        return super.execute(sub, request, locale);   // platform validation, Stripe, persist, trail
    }
}
```

`DefaultCreateEventUseCase` is `@ModelCityDisabledIfInherited`; your subclass covers the
`CreateEventUseCase` seam, so the default's bean definition is removed at startup and there is
no duplicate.

## Notes

- **Two doors, pick per need.** `extends DefaultCreateEventUseCase` reuses the whole body
  (best for *add a guard / a side effect*). `implements CreateEventUseCase<…>` from scratch is
  better when you replace the logic entirely or bind **subtype DTOs**
  (see [Expose a new field](./expose-a-new-api-field.md)).
- **Always re-declare the annotations you want.** Dropping `@CacheEvict` here would leave the
  `events` list cache stale after a create; dropping `@Transactional` would lose atomicity.
- Reuse the platform `EventWriteValidator`, `StripeFacade` and `SystemTrailGenerator` — they
  are ordinary beans injected through the inherited constructor.
- A thrown `ResponseStatusException` is turned into the right HTTP status by the platform's
  `GlobalExceptionHandler` (in commons), so you get a clean `422` without extra wiring. For a
  domain error with a code, throw `ModelCityException` instead.

## Verify

`POST /events` with `capacity > 5000` returns the validation error; a normal create still
persists, evicts the list cache and writes the audit trail exactly as before.
