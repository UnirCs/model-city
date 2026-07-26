---
title: Create event
sidebar_label: Create event
sidebar_position: 3
---

# Create event

`POST /api/leisure/events` → `CreateEventUseCase`

Creates a new cultural or leisure event. **Backoffice or admin**. The request is validated
by `EventWriteValidator`:

- `endsAt` must be after `startsAt`.
- A paid event must require a ticket and have `price > 0`.
- A free event must have `price = 0`.
- `placeId` must reference an existing city place.

For paid events the use case calls `StripeFacade.createPrice` to create a Stripe Product and
Price; the returned `stripePriceId` is stored with the event. Returns `201` with `EventDto`.

Audits `EVENT_CREATED` and evicts `events`.

## Inputs

**`POST /api/leisure/events`**

```json
{
  "placeId": 10,
  "name": {
    "es": "Jazz en el parque",
    "en": "Jazz in the park"
  },
  "description": {
    "es": "Una tarde de jazz en vivo.",
    "en": "An evening of live jazz."
  },
  "eventType": "MUSIC",
  "requiresTicket": true,
  "paid": true,
  "price": 25.00,
  "currency": "EUR",
  "capacity": 200,
  "startsAt": "2026-08-10T19:00:00",
  "endsAt": "2026-08-10T22:00:00",
  "photoUrls": ["https://cdn.modelcity.example/events/jazz-1.jpg"]
}
```

## Outputs

- **`201 Created`** — `EventDto`.
- **`400 Bad Request`** — validation error or Stripe price creation failure.
- **`403 Forbidden`** — not backoffice or admin.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (backoffice / admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant EC as EventController
        participant CU as CreateEventUseCase
        participant VAL as EventWriteValidator
        participant SF as StripeFacade
        participant ES as EventStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant CPS as CityPlaceStore
    end

    C->>GW: POST /api/leisure/events {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>EC: POST /events (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        EC->>CU: execute(sub, request, locale)
        CU->>VAL: validate(request)
        VAL->>CPS: existsById(placeId)
        alt validation fails
            CU-->>C: 400 Bad Request
        else ok
            alt paid
                CU->>SF: createPrice(defaultName, price, currency)
                SF-->>CU: stripePriceId
            else free
                Note over CU: stripePriceId = null
            end
            CU->>ES: create(request, stripePriceId)
            ES->>ER: save(Event)
            ER-->>ES: Event
            ES-->>CU: EventView
            CU->>TG: eventCreated (audit → leisure_trails)
            Note over CU: evicts events
            CU-->>C: 201 EventDto
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (backoffice / admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant EC as EventController
        participant CU as CreateEventUseCase
        participant VAL as EventWriteValidator
        participant SF as StripeFacade
        participant ES as EventStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant CPS as CityPlaceStore
    end

    C->>SEC: POST /api/leisure/events {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>EC: POST /events (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        EC->>CU: execute(sub, request, locale)
        CU->>VAL: validate(request)
        VAL->>CPS: existsById(placeId)
        alt validation fails
            CU-->>C: 400 Bad Request
        else ok
            alt paid
                CU->>SF: createPrice(defaultName, price, currency)
                SF-->>CU: stripePriceId
            else free
                Note over CU: stripePriceId = null
            end
            CU->>ES: create(request, stripePriceId)
            ES->>ER: save(Event) [single modelcity DB]
            ER-->>ES: Event
            ES-->>CU: EventView
            CU->>TG: eventCreated (audit → leisure_trails)
            Note over CU: evicts events
            CU-->>C: 201 EventDto
        end
    end
```
