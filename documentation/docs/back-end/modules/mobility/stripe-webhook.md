---
title: Stripe payment webhook
sidebar_label: Stripe webhook
sidebar_position: 8
---

# Stripe payment webhook (reservations)

`POST /api/mobility/stripe/webhook` → `StripeWebhookController`

Receives Stripe's notifications about a reservation's payment. **Public route** (no JWT; Stripe
calls it). Authenticity is guaranteed by verifying the **signature** (`Stripe-Signature`)
against the `webhookSecret`; if the signature is invalid, `400`
(`{"error":"invalid_signature"}`) and nothing is processed.

Depending on the event type it updates the reservation status, locating it by its
`checkoutSessionId`:

- `checkout.session.completed` → `PAID`.
- `checkout.session.expired` / `checkout.session.async_payment_failed` → `CANCELLED`.
- Any other type is ignored.

It always responds `200 {"received": true}` when the signature is valid (including ignored
events), as Stripe requires to avoid retries. The status change is audited with a `SYSTEM`
actor (system event).

:::note[Not the `ApiErrorResponse` format]

This endpoint returns the literal JSON bodies above because it is a contract with Stripe, not
with the front-end.

:::

## Inputs

**`POST /api/mobility/stripe/webhook`** — body: a Stripe event; header `Stripe-Signature:
t=...,v1=...`. Simplified payload example:

```json
{
  "id": "evt_1Nxyz",
  "type": "checkout.session.completed",
  "data": {
    "object": {
      "id": "cs_test_a1b2c3d4e5",
      "object": "checkout.session",
      "payment_status": "paid"
    }
  }
}
```

## Outputs

- **`200 OK`** — `{ "received": true }` (valid signature; also for ignored events).
- **`400 Bad Request`** — `{ "error": "invalid_signature" }`.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    participant ST as Stripe
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant WC as StripeWebhookController
        participant RS as StreetReservationStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    ST->>GW: POST /api/mobility/stripe/webhook (payload + Stripe-Signature)
    Note over GW: public route — no JWT
    GW->>WC: POST /stripe/webhook
    WC->>WC: Webhook.constructEvent(payload, sig, webhookSecret)
    alt invalid signature
        WC-->>ST: 400 {"error":"invalid_signature"}
    else valid signature
        alt checkout.session.completed
            WC->>RS: markStatusByCheckoutSession(sessionId, PAID)
            RS->>RR: findByStripeCheckoutSessionId + save(status)
            WC->>TG: streetReservationConfirmed (audit, actor=SYSTEM)
        else checkout.session.expired / async_payment_failed
            WC->>RS: markStatusByCheckoutSession(sessionId, CANCELLED)
            RS->>RR: findByStripeCheckoutSessionId + save(status)
            WC->>TG: streetReservationCancelled (audit, actor=SYSTEM)
        else other type
            Note over WC: ignored
        end
        WC-->>ST: 200 {"received": true}
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    participant ST as Stripe
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (public permitAll route)
    end
    box rgb(224,242,254) Component
        participant WC as StripeWebhookController
        participant RS as StreetReservationStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    ST->>SEC: POST /api/mobility/stripe/webhook (payload + Stripe-Signature)
    Note over SEC: route permitted without JWT (MonolithSecurityConfig)
    SEC->>WC: POST /stripe/webhook
    WC->>WC: Webhook.constructEvent(payload, sig, webhookSecret)
    alt invalid signature
        WC-->>ST: 400 {"error":"invalid_signature"}
    else valid signature
        alt checkout.session.completed
            WC->>RS: markStatusByCheckoutSession(sessionId, PAID)
            RS->>RR: findByStripeCheckoutSessionId + save(status) [single modelcity DB]
            WC->>TG: streetReservationConfirmed (audit, actor=SYSTEM)
        else checkout.session.expired / async_payment_failed
            WC->>RS: markStatusByCheckoutSession(sessionId, CANCELLED)
            RS->>RR: findByStripeCheckoutSessionId + save(status)
            WC->>TG: streetReservationCancelled (audit, actor=SYSTEM)
        else other type
            Note over WC: ignored
        end
        WC-->>ST: 200 {"received": true}
    end
```
