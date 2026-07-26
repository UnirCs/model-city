---
title: Event detail
sidebar_label: Event detail
sidebar_position: 2
---

# Event detail

`GET /api/leisure/events/{id}` → `GetEventUseCase`
`GET /api/leisure/events/{id}?translations=full` → `GetEventForEditUseCase`

Returns the detail of an active event. **Any authenticated user**. The standard response
includes an `acquired` boolean that is `true` when the caller already owns an active ticket
for the event (`PAID` or `PURCHASED`).

`?translations=full` returns every locale of `name` and `description` (admin editing).

Standard detail is cached in `event` keyed by `locale-id` (via `CachedEventReader`). The
`acquired` flag is resolved per request from `eventTicketStore` and overlaid on top of the
cached event.

## Inputs

**`GET /api/leisure/events/{id}`** — no body.

## Outputs

- **`200 OK`** — `EventDto`.
- **`404 Not Found`** — event not found.

```json
{
  "id": 300,
  "placeId": 10,
  "name": "Jazz in the park",
  "description": "An evening of live jazz in the city gardens.",
  "eventType": "MUSIC",
  "requiresTicket": true,
  "paid": true,
  "price": 25.00,
  "currency": "EUR",
  "capacity": 200,
  "startsAt": "2026-08-10T19:00:00",
  "endsAt": "2026-08-10T22:00:00",
  "stripePriceId": "price_1Q...",
  "photoUrls": ["https://cdn.modelcity.example/events/jazz-1.jpg"],
  "acquired": false
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
        participant GU as GetEventUseCase
        participant GF as GetEventForEditUseCase
        participant CR as CachedEventReader
        participant TS as EventTicketStore
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
    end

    C->>GW: GET /api/leisure/events/{id}?translations=full + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>EC: GET /events/{id}
    alt translations=full
        EC->>GF: execute(id, locale)
        GF->>CR: getForEdit(id, locale)
        CR->>ER: findActiveById(id)
        alt not found
            CR-->>C: 404 Not Found
        else found
            ER-->>CR: Event
            CR-->>GF: EventDto (all translations)
            GF-->>C: 200 EventDto
        end
    else standard
        EC->>GU: execute(id, sub, locale)
        GU->>CR: getById(id, locale)
        Note over CR: cache event[locale-id] if present
        CR->>ER: findActiveById(id)
        alt not found
            CR-->>C: 404 Not Found
        else found
            ER-->>CR: Event
            CR-->>GU: EventDto
            GU->>TS: existsByEventAndCitizenAndStatusIn(eventId, sub, [PAID, PURCHASED])
            TS-->>GU: boolean
            GU-->>C: 200 EventDto (with acquired)
        end
    end
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
        participant GU as GetEventUseCase
        participant GF as GetEventForEditUseCase
        participant CR as CachedEventReader
        participant TS as EventTicketStore
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
    end

    C->>SEC: GET /api/leisure/events/{id}?translations=full + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>EC: GET /events/{id}
    alt translations=full
        EC->>GF: execute(id, locale)
        GF->>CR: getForEdit(id, locale)
        CR->>ER: findActiveById(id) [single modelcity DB]
        alt not found
            CR-->>C: 404 Not Found
        else found
            ER-->>CR: Event
            CR-->>GF: EventDto (all translations)
            GF-->>C: 200 EventDto
        end
    else standard
        EC->>GU: execute(id, sub, locale)
        GU->>CR: getById(id, locale)
        Note over CR: cache event[locale-id] if present
        CR->>ER: findActiveById(id)
        alt not found
            CR-->>C: 404 Not Found
        else found
            ER-->>CR: Event
            CR-->>GU: EventDto
            GU->>TS: existsByEventAndCitizenAndStatusIn(eventId, sub, [PAID, PURCHASED])
            TS-->>GU: boolean
            GU-->>C: 200 EventDto (with acquired)
        end
    end
```
