---
title: StripeCheckoutEmbed
sidebar_label: StripeCheckoutEmbed
sidebar_position: 29
---

# StripeCheckoutEmbed

`packages/core/components/molecules/StripeCheckoutEmbed.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A generic Stripe **Embedded Checkout** wrapper. The caller supplies a
`fetchClientSecret` function that `EmbeddedCheckoutProvider` invokes once on mount
to obtain the checkout session's client secret (the official quickstart pattern,
which avoids any infinite re-render loop). It is intentionally
**payment-domain-agnostic**: every feature module owns its own checkout-session
server action and passes the resulting `fetchClientSecret`, so this core component
never depends on a feature module.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `fetchClientSecret` | `() => Promise<string>` | yes | — | Invoked once on mount to fetch the session client secret. |
| `labels` | `{ misconfigured: string }` | yes | — | Localized copy shown when Stripe is not configured. |

## Behaviour

`loadStripe` is called once at module level with
`NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY` (singleton). When the key is missing, it
renders the `labels.misconfigured` error instead of the checkout.

## Composition

- **Uses:** `@stripe/stripe-js` (`loadStripe`), `@stripe/react-stripe-js`
  (`EmbeddedCheckoutProvider`, `EmbeddedCheckout`), `atoms/core/Icon`.
- **Used by:** `molecules/leisure/EventCheckoutEmbed`,
  `molecules/mobility/RenewStayModal` and the mobility reservation checkout.

## Internationalisation

Only the `labels.misconfigured` string (supplied by the caller).

## Accessibility

Delegates to Stripe's embedded checkout iframe; the misconfigured fallback is an
error-tone message with an icon.

## Styling & tokens

`border-outline-variant`, `text-error` for the fallback.

## Usage

```jsx
<StripeCheckoutEmbed
  fetchClientSecret={fetchClientSecret}
  labels={{ misconfigured: t.payments.misconfigured }}
/>
```

## Related

`molecules/leisure/EventCheckoutEmbed`, and the
[Data & API](../../../architecture/data-and-api.md) Stripe env vars.
