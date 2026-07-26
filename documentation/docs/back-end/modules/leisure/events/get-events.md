---
title: List events
sidebar_label: List events
sidebar_position: 1
---

# List events

`GET /api/leisure/events` → `GetEventsUseCase`

Returns paginated active upcoming events. **Any authenticated user**. Optional filters:
`type` (one of `MUSIC`, `NIGHTLIFE`, `PERFORMING_ARTS`, `HOBBIES`, `BUSINESS`,
`FOOD_AND_DRINK`, `OTHER`) and `paid` (`true`/`false`). Page size is **6**, sorted by
`startsAt` ascending. Only events with `startsAt >= now` are returned.

Cached in `events` keyed by `locale-type-paid-page`.

## Inputs

**`GET /api/leisure/events?type=MUSIC&paid=false&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<EventSummaryDto>`.

```json
{
  "content": [
    {
      "id": 300,
      "placeId": 10,
      "name": "Jazz in the park",
      "eventType": "MUSIC",
      "requiresTicket": true,
      "paid": true,
      "price": 25.00,
      "currency": "EUR",
      "startsAt": "2026-08-10T19:00:00",
      "endsAt": "2026-08-10T22:00:00",
      "stripePriceId": "price_1Q...",
      "photoUrl": "https://cdn.modelcity.example/events/jazz-1.jpg"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 6
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant EC as EventController
        participant GU as GetEventsUseCase
        participant ES as EventStore
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
    end

    C->>GW: GET /api/leisure/events?type&paid&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>EC: GET /events
    EC->>GU: execute(type, paid, page, locale)
    Note over GU: cache events[locale-type-paid-page] if present
    GU->>ES: search(type, paid, now, PageRequest(0,6,startsAt asc))
    ES->>ER: findActiveByTypeAndPaidAndStartsAtAfter(...)
    ER-->>ES: Page<Event>
    ES-->>GU: Page<EventView>
    GU-->>C: 200 Page<EventSummaryDto>
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant EC as EventController
        participant GU as GetEventsUseCase
        participant ES as EventStore
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
    end

    C->>SEC: GET /api/leisure/events?type&paid&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>EC: GET /events
    EC->>GU: execute(type, paid, page, locale)
    Note over GU: cache events[locale-type-paid-page] if present
    GU->>ES: search(type, paid, now, PageRequest(0,6,startsAt asc))
    ES->>ER: findActiveByTypeAndPaidAndStartsAtAfter(...)
    ER-->>ES: Page<Event>
    ES-->>GU: Page<EventView>
    GU-->>C: 200 Page<EventSummaryDto>
```
