---
title: Delete event
sidebar_label: Delete event
sidebar_position: 5
---

# Delete event

`DELETE /api/leisure/events/{id}` → `DeleteEventUseCase`

Soft-deletes an event and automatically refunds all outstanding paid tickets in status
`PURCHASED`. **Admin only**. If the event is not active, `404`. On success, `204 No Content`.

For each outstanding ticket the use case:

1. Calls `StripeFacade.refund` using the stored `stripeCheckoutSessionId`.
2. Marks the ticket as `REFUNDED`.
3. Records an automatic refund and audits `EVENT_TICKET_AUTO_REFUNDED`.

Then it soft-deletes the event and audits `EVENT_DELETED` with the ticket count and total
refunded amount.

## Inputs

**`DELETE /api/leisure/events/{id}`** — no body.

## Outputs

- **`204 No Content`** — event deleted and tickets refunded.
- **`403 Forbidden`** — not admin.
- **`404 Not Found`** — event not found.
- **`502 Bad Gateway`** — Stripe refund fails.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant EC as EventController
        participant DU as DeleteEventUseCase
        participant ES as EventStore
        participant TS as EventTicketStore
        participant RS as EventRefundStore
        participant SF as StripeFacade
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant STR as Stripe API
    end

    C->>GW: DELETE /api/leisure/events/{id} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>EC: DELETE /events/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        EC->>DU: execute(id, sub)
        DU->>ES: findActiveById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>TS: findByEventAndStatus(id, PURCHASED)
            TS-->>DU: outstanding tickets
            loop each paid ticket
                DU->>SF: refund(checkoutSessionId, pricePaid, currency)
                SF->>STR: Refund.create(paymentIntent, amount)
                SF-->>DU: ok
                DU->>TS: markRefunded(ticketId, now)
                DU->>RS: create(ticketId, amount, currency, "Automatic refund...", true, sub)
                DU->>TG: eventTicketAutoRefunded (audit → leisure_trails)
            end
            DU->>ES: softDelete(id)
            ES->>ER: soft delete
            DU->>TG: eventDeleted (count, totalAmount)
            Note over DU: evicts event + events
            DU-->>C: 204 No Content
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant EC as EventController
        participant DU as DeleteEventUseCase
        participant ES as EventStore
        participant TS as EventTicketStore
        participant RS as EventRefundStore
        participant SF as StripeFacade
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant STR as Stripe API
    end

    C->>SEC: DELETE /api/leisure/events/{id} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>EC: DELETE /events/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        EC->>DU: execute(id, sub)
        DU->>ES: findActiveById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>TS: findByEventAndStatus(id, PURCHASED)
            TS-->>DU: outstanding tickets
            loop each paid ticket
                DU->>SF: refund(checkoutSessionId, pricePaid, currency)
                SF->>STR: Refund.create(paymentIntent, amount)
                SF-->>DU: ok
                DU->>TS: markRefunded(ticketId, now)
                DU->>RS: create(ticketId, amount, currency, "Automatic refund...", true, sub)
                DU->>TG: eventTicketAutoRefunded (audit → leisure_trails)
            end
            DU->>ES: softDelete(id)
            ES->>ER: soft delete [single modelcity DB]
            DU->>TG: eventDeleted (count, totalAmount)
            Note over DU: evicts event + events
            DU-->>C: 204 No Content
        end
    end
```
