---
title: EventCheckoutEmbed
sidebar_label: EventCheckoutEmbed
sidebar_position: 5
---

# EventCheckoutEmbed

`packages/leisure/components/molecules/EventCheckoutEmbed.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

A leisure-owned wrapper around the generic
[`StripeCheckoutEmbed`](../core/StripeCheckoutEmbed.md). It binds the event-ticket
checkout to Stripe by deriving `fetchClientSecret` from the leisure
`createEventCheckoutSession` server action — keeping the leisure-specific dependency
inside the leisure module instead of leaking into the core component.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `eventId` | `number \| string` | yes | — | The event to buy a ticket for. |
| `lang` | `string` | yes | — | Active locale (passed to the checkout action). |
| `labels` | `{ misconfigured: string }` | yes | — | Localized copy for the "Stripe not configured" state. |

## Behaviour

`fetchClientSecret` calls `createEventCheckoutSession(eventId, lang)` and resolves to
its `clientSecret` (throwing on error), which `EmbeddedCheckoutProvider` invokes once
on mount.

## Composition

- **Uses:** `molecules/core/StripeCheckoutEmbed`, `createEventCheckoutSession`
  (`leisure/lib/actions/stripeCheckout`), `useCallback`.
- **Used by:** the event checkout route (`/events/[id]/checkout`).

## Internationalisation

Only `labels.misconfigured` (supplied by the caller).

## Accessibility

Delegates to `StripeCheckoutEmbed` / Stripe's embedded checkout.

## Related

`molecules/core/StripeCheckoutEmbed`, `molecules/leisure/BuyTicketButton`,
[Data & API](../../../architecture/data-and-api.md).
