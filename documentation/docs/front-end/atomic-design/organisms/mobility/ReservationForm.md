---
title: ReservationForm
sidebar_label: ReservationForm
sidebar_position: 4
---

# ReservationForm

`packages/mobility/components/organisms/ReservationForm.js` · **Tier:** organism · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

The two-column "register stay" form: map picker on the left, car selector + duration
slider on the right. On submit (after validation) it opens a Stripe Embedded Checkout
modal; payment triggers the backend reservation creation via the
`/{lang}/mobility/parking-checkout/return` return page. Pricing is 1 cent per minute
(min 20 min = €0.20, max 2h30 = €1.50).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `cars` | `Array<{ id, licensePlate, nickname? }>` | yes | — | The user's cars (selector). |
| `labels` | `object` | yes | — | Form copy. |
| `lang` | `string` | yes | — | Locale (checkout return path). |

## Composition

- **Uses:** `molecules/core/LocationPickerMapClient`, `atoms/core/DurationSlider`,
  `molecules/core/StripeCheckoutEmbed`, `molecules/core/Modal`,
  `atoms/core/FormField`, the mobility Stripe-checkout action.
- **Used by:** the `/mobility/reserve` page.

## Internationalisation

All copy via `labels`.

## Accessibility

Fields via `FormField`; the map picker and duration slider are keyboard-operable; the
checkout runs in the accessible `core/Modal`.

## Related

`atoms/core/DurationSlider`, `molecules/core/StripeCheckoutEmbed`,
`molecules/mobility/RenewStayModal`.
