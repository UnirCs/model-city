---
title: Citizen ticket history
sidebar_label: Citizen ticket history
sidebar_position: 9
---

# Citizen ticket history

`GET /api/leisure/users/{userId}/tickets` → `GetCitizenTicketsUseCase`

Returns the paginated ticket history of a citizen. **Any authenticated user**. Citizens may
only query their own `userId`; admin and backoffice may query any user.

Optional `period` filter:

- `week` — current week (from today up to next Sunday).
- `future` — after the current week.
- `past` — before today.

Default page size is **20**, sorted by `purchasedAt` descending. The response includes event
name, start date and photo.

## Inputs

**`GET /api/leisure/users/{userId}/tickets?period=future&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<CitizenTicketDto>`.
- **`403 Forbidden`** — trying to view another citizen's tickets without admin/backoffice role.
- **`404 Not Found`** — event referenced by a ticket is no longer active.

### Public view (citizen viewing own history)

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
      "eventName": "Jazz in the park",
      "eventStart": "2026-08-10",
      "eventPhoto": "https://cdn.modelcity.example/events/jazz-1.jpg"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

### Privileged view (admin / backoffice)

Same as above plus `citizenSub` and `citizenName`.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant CTC as CitizenTicketController
        participant GU as GetCitizenTicketsUseCase
        participant CC as CoreClient
        participant TS as EventTicketStore
        participant ES as EventStore
    end
    box rgb(224,247,224) DB · third parties
        participant ETR as EventTicketRepository
        participant ER as EventRepository
    end

    C->>GW: GET /api/leisure/users/{userId}/tickets?period=future + JWT
    Note over GW: validates JWT and injects X-Auth-Sub (callerSub)
    GW->>CTC: GET /users/{userId}/tickets
    CTC->>GU: execute(targetSub, callerSub, page, period)
    GU->>CC: getUserRole(callerSub)
    CC-->>GU: role
    alt not privileged and targetSub != callerSub
        GU-->>C: 403 Forbidden
    else authorized
        GU->>TS: findByCitizenAndEventStartAfter/Before/Between(targetSub, ..., pageable)
        TS->>ETR: query by citizen and period
        ETR-->>TS: Page<EventTicket>
        TS-->>GU: Page<EventTicketView>
        loop each ticket
            GU->>ES: findActiveById(eventId)
            ES->>ER: findActiveById(eventId)
            alt not found
                ES-->>C: 404 Not Found
            else found
                ES-->>GU: EventView
            end
        end
        GU-->>C: 200 Page<CitizenTicketDto>
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
        participant CTC as CitizenTicketController
        participant GU as GetCitizenTicketsUseCase
        participant CC as CoreClient (in-process)
        participant TS as EventTicketStore
        participant ES as EventStore
    end
    box rgb(224,247,224) DB · third parties
        participant ETR as EventTicketRepository
        participant ER as EventRepository
    end

    C->>SEC: GET /api/leisure/users/{userId}/tickets?period=future + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub (callerSub)
    SEC->>CTC: GET /users/{userId}/tickets
    CTC->>GU: execute(targetSub, callerSub, page, period)
    GU->>CC: getUserRole(callerSub)
    CC-->>GU: role
    alt not privileged and targetSub != callerSub
        GU-->>C: 403 Forbidden
    else authorized
        GU->>TS: findByCitizenAndEventStartAfter/Before/Between(targetSub, ..., pageable)
        TS->>ETR: query by citizen and period [single modelcity DB]
        ETR-->>TS: Page<EventTicket>
        TS-->>GU: Page<EventTicketView>
        loop each ticket
            GU->>ES: findActiveById(eventId)
            ES->>ER: findActiveById(eventId)
            alt not found
                ES-->>C: 404 Not Found
            else found
                ES-->>GU: EventView
            end
        end
        GU-->>C: 200 Page<CitizenTicketDto>
    end
```
