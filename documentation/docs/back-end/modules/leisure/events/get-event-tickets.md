---
title: List event tickets
sidebar_label: List event tickets
sidebar_position: 7
---

# List event tickets

`GET /api/leisure/events/{eventId}/tickets` → `GetEventTicketsUseCase`

Returns paginated tickets for an event. **Admin or backoffice**. Page size is **20**, sorted
by `purchasedAt` ascending. The response uses `TicketDto.privilegedView`, so it includes
citizen identification.

If the event is not active, `404`.

## Inputs

**`GET /api/leisure/events/{eventId}/tickets?page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<TicketDto>`.
- **`403 Forbidden`** — not admin or backoffice.
- **`404 Not Found`** — event not found.

```json
{
  "content": [
    {
      "id": 1001,
      "eventId": 300,
      "pricePaid": 25.00,
      "currency": "EUR",
      "status": "PAID",
      "purchasedAt": "2026-07-24T18:00:00",
      "stripeCheckoutSessionId": "cs_test_a1...",
      "citizenSub": "auth0|123456",
      "citizenName": "Ana García"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / backoffice)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant ETC as EventTicketController
        participant GU as GetEventTicketsUseCase
        participant ES as EventStore
        participant TS as EventTicketStore
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant ETR as EventTicketRepository
    end

    C->>GW: GET /api/leisure/events/{eventId}/tickets?page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>ETC: GET /events/{eventId}/tickets
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        ETC->>GU: execute(eventId, page)
        GU->>ES: findActiveById(eventId)
        ES->>ER: findActiveById(eventId)
        alt not found
            ES-->>C: 404 Not Found
        else found
            GU->>TS: findByEvent(eventId, PageRequest(0,20,purchasedAt asc))
            TS->>ETR: findByEventId(eventId, pageable)
            ETR-->>TS: Page<EventTicket>
            TS-->>GU: Page<EventTicketView>
            GU-->>C: 200 Page<TicketDto>
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / backoffice)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant ETC as EventTicketController
        participant GU as GetEventTicketsUseCase
        participant ES as EventStore
        participant TS as EventTicketStore
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant ETR as EventTicketRepository
    end

    C->>SEC: GET /api/leisure/events/{eventId}/tickets?page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>ETC: GET /events/{eventId}/tickets
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        ETC->>GU: execute(eventId, page)
        GU->>ES: findActiveById(eventId)
        ES->>ER: findActiveById(eventId) [single modelcity DB]
        alt not found
            ES-->>C: 404 Not Found
        else found
            GU->>TS: findByEvent(eventId, PageRequest(0,20,purchasedAt asc))
            TS->>ETR: findByEventId(eventId, pageable)
            ETR-->>TS: Page<EventTicket>
            TS-->>GU: Page<EventTicketView>
            GU-->>C: 200 Page<TicketDto>
        end
    end
```
