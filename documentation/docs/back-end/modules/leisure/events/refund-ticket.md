---
title: Refund ticket
sidebar_label: Refund ticket
sidebar_position: 8
---

# Refund ticket

`POST /api/leisure/events/{eventId}/tickets/{ticketId}/refunds` → `RefundTicketUseCase`

Issues a refund for a paid ticket. **Admin or backoffice**. The body is optional and may
contain a `reason`.

Validation:

- The ticket must exist and belong to the event.
- The ticket must not already be `REFUNDED`.
- If `pricePaid > 0` and a `stripeCheckoutSessionId` is present, a Stripe refund is created
  from the payment intent associated with the session.

The ticket is marked `REFUNDED`, a refund record is stored, and `EVENT_TICKET_REFUNDED` is
audited.

## Inputs

**`POST /api/leisure/events/{eventId}/tickets/{ticketId}/refunds`**

```json
{
  "reason": "Customer request"
}
```

The body is optional.

## Outputs

- **`201 Created`** — `RefundDto`.
- **`403 Forbidden`** — not admin or backoffice.
- **`404 Not Found`** — ticket not found.
- **`409 Conflict`** — ticket already refunded.
- **`502 Bad Gateway`** — Stripe refund fails.

```json
{
  "id": 500,
  "ticketId": 1001,
  "amount": 25.00,
  "currency": "EUR",
  "reason": "Customer request",
  "automatic": false,
  "issuedBySub": "auth0|admin01",
  "refundedAt": "2026-07-24T19:00:00"
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
        participant RU as RefundTicketUseCase
        participant TS as EventTicketStore
        participant RS as EventRefundStore
        participant SF as StripeFacade
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ETR as EventTicketRepository
        participant ERT as EventRefundRepository
        participant STR as Stripe API
    end

    C->>GW: POST /api/leisure/events/{id}/tickets/{tid}/refunds {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>ETC: POST /events/{id}/tickets/{tid}/refunds
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        ETC->>RU: execute(eventId, ticketId, issuedBySub, request)
        RU->>TS: findById(ticketId)
        TS->>ETR: findById(ticketId)
        alt not found or event mismatch
            TS-->>C: 404 Not Found
        else found
            alt already REFUNDED
                RU-->>C: 409 Conflict
            else ok
                alt pricePaid > 0 and has checkout session
                    RU->>SF: refund(checkoutSessionId, pricePaid, currency)
                    SF->>STR: Refund.create(paymentIntent, amount)
                    SF-->>RU: ok
                end
                RU->>TS: markRefunded(ticketId, now)
                TS->>ETR: save
                RU->>RS: create(ticketId, amount, currency, reason, false, issuedBySub)
                RS->>ERT: save(EventRefund)
                ERT-->>RS: EventRefund
                RS-->>RU: EventRefundView
                RU->>TG: eventTicketRefunded (audit → leisure_trails)
                RU-->>C: 201 RefundDto
            end
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
        participant RU as RefundTicketUseCase
        participant TS as EventTicketStore
        participant RS as EventRefundStore
        participant SF as StripeFacade
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ETR as EventTicketRepository
        participant ERT as EventRefundRepository
        participant STR as Stripe API
    end

    C->>SEC: POST /api/leisure/events/{id}/tickets/{tid}/refunds {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>ETC: POST /events/{id}/tickets/{tid}/refunds
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        ETC->>RU: execute(eventId, ticketId, issuedBySub, request)
        RU->>TS: findById(ticketId)
        TS->>ETR: findById(ticketId) [single modelcity DB]
        alt not found or event mismatch
            TS-->>C: 404 Not Found
        else found
            alt already REFUNDED
                RU-->>C: 409 Conflict
            else ok
                alt pricePaid > 0 and has checkout session
                    RU->>SF: refund(checkoutSessionId, pricePaid, currency)
                    SF->>STR: Refund.create(paymentIntent, amount)
                    SF-->>RU: ok
                end
                RU->>TS: markRefunded(ticketId, now)
                TS->>ETR: save
                RU->>RS: create(ticketId, amount, currency, reason, false, issuedBySub)
                RS->>ERT: save(EventRefund)
                ERT-->>RS: EventRefund
                RS-->>RU: EventRefundView
                RU->>TG: eventTicketRefunded (audit → leisure_trails)
                RU-->>C: 201 RefundDto
            end
        end
    end
```
