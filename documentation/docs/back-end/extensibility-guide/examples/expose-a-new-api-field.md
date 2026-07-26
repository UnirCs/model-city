---
title: Expose a new field on the API (end-to-end)
sidebar_label: Expose a new field (end-to-end)
sidebar_position: 2
---

# Expose a new field on the API (end-to-end)

**Goal:** add a field — here `videoUrl` on an **event** — that is **accepted** on create/update
**and returned** on reads. This is the full clean-architecture path: it touches the schema,
the entity, the DTOs, the store and the producing use cases, all through subclasses.

- **Seams:** `EventDto` / `EventRequestDto` (extensible DTOs), `EventStore` (port),
  `EventController` (abstract base), the read/write `*UseCase` interfaces.
- **Schema change:** additive `V2+` migration.
- **Applies to:** both topologies.

:::info[Why so many files?]

The platform DTOs, use cases and controller are **generic over the DTO types**
(`EventController<T extends EventDto, S extends EventSummaryDto, R extends EventRequestDto>`,
`GetEventUseCase<T extends EventDto>`, …). Binding the controller to your `MyEventDto` forces
Spring to inject use cases that **produce** `MyEventDto`, so a half-override (new DTO but old
use cases) **fails fast at startup** instead of silently dropping your field.

:::

## 1. The migration

```sql
-- V2__add_video_url_to_events.sql
ALTER TABLE events ADD COLUMN video_url VARCHAR(2048);
```

## 2. The entity + a widened read view

Add the column on a subclass entity, and widen the read **view** so the DTO factory can see
it (the platform `EventView` doesn't know about `videoUrl`).

```java
// store/model/MyEventView.java — extends the platform read port
public interface MyEventView extends EventView {
    String getVideoUrl();
}
```

```java
// repository/model/MyEvent.java
@Entity
@Table(name = "events")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class MyEvent extends EventBase implements MyEventView {

    @Column(name = "video_url", length = 2048)
    private String videoUrl;
}
```

```java
// repository/MyEventRepository.java
public interface MyEventRepository extends EventRepository<MyEvent> {}
```

## 3. The DTO subclasses

DTOs are **plain extensible classes** (not `record`s) precisely so a city can subclass them.

```java
// controller/model/MyEventDto.java
@Getter
@Setter
@NoArgsConstructor
public class MyEventDto extends EventDto {
    private String videoUrl;

    public static MyEventDto from(MyEventView e, String locale) {
        MyEventDto dto = new MyEventDto();
        // copy the platform fields, then add ours
        EventDto base = EventDto.from(e, locale);
        BeanUtils.copyProperties(base, dto);   // org.springframework.beans.BeanUtils
        dto.setVideoUrl(e.getVideoUrl());
        return dto;
    }
}
```

```java
// controller/model/MyEventRequestDto.java
@Getter
@Setter
@NoArgsConstructor
public class MyEventRequestDto extends EventRequestDto {
    private String videoUrl;
}
```

## 4. The store that persists it

Bind the port to your entity and request subtype; it replaces `DefaultEventStore`
(see [Replace a persistence adapter](./replace-a-store-adapter.md) for the full pattern).

```java
@Component
@RequiredArgsConstructor
public class MyEventStore implements EventStore<MyEvent, MyEventRequestDto> {

    private final MyEventRepository repository;

    @Override
    public MyEvent create(MyEventRequestDto request, String stripePriceId) {
        MyEvent event = new MyEvent();
        event.setActive(true);
        applyPlatformFields(event, request);   // name, description, price, translations… (mirror DefaultEventStore)
        event.setStripePriceId(stripePriceId);
        event.setVideoUrl(request.getVideoUrl());
        return repository.save(event);
    }

    // update(...), findActiveById(...), search(...), softDelete(...) mirror DefaultEventStore,
    // returning MyEvent instead of Event.
}
```

## 5. The producing use cases (bound to the subtype)

Every use case that returns an `EventDto` must be overridden to build `MyEventDto`.
Re-declare the platform caching/transaction annotations you want to keep
(see [Override a write use case](./override-a-write-use-case.md)).

```java
@Service
@RequiredArgsConstructor
public class MyCreateEventUseCase implements CreateEventUseCase<MyEventDto, MyEventRequestDto> {

    private final EventStore<MyEvent, MyEventRequestDto> eventStore;
    private final EventWriteValidator validator;
    private final StripeFacade stripeFacade;
    private final SystemTrailGenerator trails;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.EVENTS, allEntries = true)
    public MyEventDto execute(String sub, MyEventRequestDto request, String locale) {
        validator.validate(request);
        String priceId = request.isPaid()
                ? stripeFacade.createPrice(LocalizedText.requireDefault("name", request.getName()),
                                           request.getPrice(), request.getCurrency().toUpperCase())
                : null;
        MyEvent saved = eventStore.create(request, priceId);
        trails.eventCreated(sub, saved);
        return MyEventDto.from(saved, locale);
    }
}
```

Do the same for the reads you expose (`GetEventUseCase<MyEventDto>`,
`GetEventForEditUseCase<MyEventDto>`, `UpdateEventUseCase<MyEventDto, MyEventRequestDto>`).
`GetEventsUseCase` returns the lighter `EventSummaryDto`; override it only if `videoUrl` must
appear in the **list** too.

## 6. The controller, bound to the subtypes

```java
@RestController
public class MyEventController extends EventController<MyEventDto, EventSummaryDto, MyEventRequestDto> {

    public MyEventController(
            GetEventsUseCase<EventSummaryDto> getEventsUseCase,
            GetEventUseCase<MyEventDto> getEventUseCase,
            GetEventForEditUseCase<MyEventDto> getEventForEditUseCase,
            CreateEventUseCase<MyEventDto, MyEventRequestDto> createEventUseCase,
            UpdateEventUseCase<MyEventDto, MyEventRequestDto> updateEventUseCase,
            DeleteEventUseCase deleteEventUseCase) {
        super(getEventsUseCase, getEventUseCase, getEventForEditUseCase,
                createEventUseCase, updateEventUseCase, deleteEventUseCase);
    }
}
```

The abstract `EventController` already carries `@RequestMapping("/events")` and the endpoint
methods; you only bind the types. `DefaultEventController` and `DefaultEventStore` back off
automatically.

## Notes

- **`@RequestBody @Valid R request` binds your subtype**, so the extra field is validated and
  populated by Jackson without touching the base controller.
- Missing any producing use case for `MyEventDto` makes the app **fail to start** (unsatisfied
  constructor) — that is the intended guard, not a bug.
- `@JsonInclude(NON_NULL)` on `EventDto` is inherited, so `videoUrl` is simply omitted when
  null.

## Verify

Start the app (a green start proves the wiring and the `validate` schema check). Then:

```bash
curl -X POST /events -H 'X-Auth-Sub: <admin>' -H 'Content-Type: application/json' \
  -d '{ "...": "...", "videoUrl": "https://youtu.be/abc" }'
curl /events/{id}     # → the response includes "videoUrl"
```
