---
title: Purchase ticket (web)
sidebar_label: Purchase ticket (web)
sidebar_position: 6
---

# Purchase ticket (web)

`POST /api/leisure/events/{eventId}/tickets` → `PurchaseTicketUseCase`

Purchases an event ticket using the web Checkout Session flow. **Any authenticated user**
(citizen). The request body may contain `checkoutSessionId` for paid events; free events do
not require it.

Validation:

- The event must be active and `requiresTicket = true`.
- If `capacity` is set, the number of active tickets (`PENDING`, `PAID`, `PURCHASED`) must
  not reach it.
- For paid events, `checkoutSessionId` is required.

For a **free** event the ticket is created in `PURCHASED` status immediately. For a **paid**
event a `PENDING` ticket is created; Stripe confirms the payment asynchronously and updates
status via the [Stripe webhook](stripe-webhook.md).

The use case resolves the citizen name from `CoreClient` and stores it. Audits
`EVENT_TICKET_PURCHASED`.

## Inputs

**`POST /api/leisure/events/{eventId}/tickets`**

```json
{
  "checkoutSessionId": "cs_test_a1..."
}
```

For a free event, the body can be `{}` or omitted.

## Outputs

- **`201 Created`** — `TicketDto` (privileged view).
- **`400 Bad Request`** — event does not require a ticket, or missing `checkoutSessionId` for paid event.
- **`404 Not Found`** — event not found.
- **`409 Conflict`** — event sold out.

```json
{
  "id": 1001,
  "eventId": 300,
  "pricePaid": 25.00,
  "currency": "EUR",
  "status": "PENDING",
  "purchasedAt": "2026-07-24T18:00:00",
  "stripeCheckoutSessionId": "cs_test_a1...",
  "citizenSub": "auth0|123456",
  "citizenName": "Ana García"
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Citizen
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ETC as EventTicketController
        participant PU as PurchaseTicketUseCase
        participant ES as EventStore
        participant TS as EventTicketStore
        participant CC as CoreClient
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant ETR as EventTicketRepository
    end

    C->>GW: POST /api/leisure/events/{eventId}/tickets {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>ETC: POST /events/{eventId}/tickets
    ETC->>PU: execute(eventId, citizenSub, request)
    PU->>ES: findActiveById(eventId)
    ES->>ER: findActiveById(eventId)
    alt not found
        ES-->>C: 404 Not Found
    else found
        ES-->>PU: EventView
        Note over PU: check requiresTicket, capacity not exceeded
        alt invalid
            PU-->>C: 400/409
        else ok
            PU->>CC: getUserName(citizenSub)
            CC-->>PU: citizenName
            PU->>TS: createPending(eventId, citizenSub, name, price, currency, sessionId)
            TS->>ETR: save(EventTicket)
            ETR-->>TS: EventTicket
            TS-->>PU: EventTicketView
            PU->>TG: eventTicketPurchased (audit → leisure_trails)
            alt free event
                PU->>TS: markStatus(id, PURCHASED)
            end
            PU-->>C: 201 TicketDto
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Citizen
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ETC as EventTicketController
        participant PU as PurchaseTicketUseCase
        participant ES as EventStore
        participant TS as EventTicketStore
        participant CC as CoreClient (in-process)
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant ETR as EventTicketRepository
    end

    C->>SEC: POST /api/leisure/events/{eventId}/tickets {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>ETC: POST /events/{eventId}/tickets
    ETC->>PU: execute(eventId, citizenSub, request)
    PU->>ES: findActiveById(eventId)
    ES->>ER: findActiveById(eventId) [single modelcity DB]
    alt not found
        ES-->>C: 404 Not Found
    else found
        ES-->>PU: EventView
        Note over PU: check requiresTicket, capacity not exceeded
        alt invalid
            PU-->>C: 400/409
        else ok
            PU->>CC: getUserName(citizenSub)
            CC-->>PU: citizenName
            PU->>TS: createPending(eventId, citizenSub, name, price, currency, sessionId)
            TS->>ETR: save(EventTicket)
            ETR-->>TS: EventTicket
            TS-->>PU: EventTicketView
            PU->>TG: eventTicketPurchased (audit → leisure_trails)
            alt free event
                PU->>TS: markStatus(id, PURCHASED)
            end
            PU-->>C: 201 TicketDto
        end
    end
```
