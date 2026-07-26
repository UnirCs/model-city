---
title: Stripe webhook
sidebar_label: Stripe webhook
sidebar_position: 10
---

# Stripe webhook

`POST /api/leisure/stripe/webhook` → `StripeWebhookController`

Receives asynchronous Stripe lifecycle events for the **web Checkout Session** payment flow.
This endpoint is **unauthenticated**; security relies on Stripe signature verification with
the configured `webhookSecret` (`StripeProperties`).

Processed event types:

- `checkout.session.completed` → ticket status set to `PAID`.
- `checkout.session.expired` → ticket status set to `CANCELLED`.
- `checkout.session.async_payment_failed` → ticket status set to `CANCELLED`.

Other event types are ignored. The response is always `200 OK` with `{"received": true}`
once the signature is valid, even if the ticket is not found (logged as a warning).

Each status change audits `EVENT_TICKET_CONFIRMED` (for `PAID`) or `EVENT_TICKET_CANCELLED`
(for `CANCELLED`). The actor is the citizen who owns the ticket.

## Inputs

**`POST /api/leisure/stripe/webhook`**

```http
Stripe-Signature: t=1234567890,v1=...
Content-Type: application/json
```

Body is the raw Stripe event JSON.

## Outputs

- **`200 OK`** — `{"received": true}`.
- **`400 Bad Request`** — invalid signature or invalid payload.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor S as Stripe
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant SWC as StripeWebhookController
        participant SP as StripeProperties
        participant TS as EventTicketStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ETR as EventTicketRepository
        participant STR as Stripe API
    end

    S->>GW: POST /api/leisure/stripe/webhook (Stripe-Signature + payload)
    GW->>SWC: POST /stripe/webhook
    SWC->>STR: Webhook.constructEvent(payload, sigHeader, webhookSecret)
    alt invalid signature
        STR-->>SWC: SignatureVerificationException
        SWC-->>S: 400 {error: invalid_signature}
    else valid
        SWC->>STR: getObject() as Session
        alt checkout.session.completed
            SWC->>TS: findByCheckoutSessionId(session.id)
            TS->>ETR: findByStripeCheckoutSessionId(session.id)
            alt ticket found
                TS-->>SWC: EventTicketView
                SWC->>TS: markStatus(ticket.id, PAID)
                SWC->>TG: eventTicketStatusChanged (audit → leisure_trails, EVENT_TICKET_CONFIRMED)
                SWC-->>S: 200 {received: true}
            else not found
                SWC-->>S: 200 {received: true}
            end
        else checkout.session.expired / async_payment_failed
            SWC->>TS: findByCheckoutSessionId(session.id)
            alt ticket found
                SWC->>TS: markStatus(ticket.id, CANCELLED)
                SWC->>TG: eventTicketStatusChanged (audit → leisure_trails, EVENT_TICKET_CANCELLED)
                SWC-->>S: 200 {received: true}
            else not found
                SWC-->>S: 200 {received: true}
            end
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor S as Stripe
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant SWC as StripeWebhookController
        participant SP as StripeProperties
        participant TS as EventTicketStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ETR as EventTicketRepository
        participant STR as Stripe API
    end

    S->>SEC: POST /api/leisure/stripe/webhook (Stripe-Signature + payload)
    SEC->>SWC: POST /stripe/webhook
    SWC->>STR: Webhook.constructEvent(payload, sigHeader, webhookSecret)
    alt invalid signature
        STR-->>SWC: SignatureVerificationException
        SWC-->>S: 400 {error: invalid_signature}
    else valid
        SWC->>STR: getObject() as Session
        alt checkout.session.completed
            SWC->>TS: findByCheckoutSessionId(session.id)
            TS->>ETR: findByStripeCheckoutSessionId(session.id) [single modelcity DB]
            alt ticket found
                TS-->>SWC: EventTicketView
                SWC->>TS: markStatus(ticket.id, PAID)
                SWC->>TG: eventTicketStatusChanged (audit → leisure_trails, EVENT_TICKET_CONFIRMED)
                SWC-->>S: 200 {received: true}
            else not found
                SWC-->>S: 200 {received: true}
            end
        else checkout.session.expired / async_payment_failed
            SWC->>TS: findByCheckoutSessionId(session.id)
            alt ticket found
                SWC->>TS: markStatus(ticket.id, CANCELLED)
                SWC->>TG: eventTicketStatusChanged (audit → leisure_trails, EVENT_TICKET_CANCELLED)
                SWC-->>S: 200 {received: true}
            else not found
                SWC-->>S: 200 {received: true}
            end
        end
    end
```
