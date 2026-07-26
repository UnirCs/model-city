---
title: RenewStayModal
sidebar_label: RenewStayModal
sidebar_position: 6
---

# RenewStayModal

`packages/mobility/components/molecules/RenewStayModal.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

A lightweight modal hosting a duration slider for renewing an active street
reservation. On submit (after validation) it opens a Stripe Embedded Checkout inside
the same modal; payment triggers the backend renewal via the
`/{lang}/mobility/parking-checkout/return` return page. The parent remounts it with a
fresh `key` per reservation target so internal state resets.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Visibility. |
| `reservation` | `{ id, carId, licensePlate, latitude, longitude } \| null` | yes | — | The reservation to renew. |
| `labels` | `object` | yes | — | Modal copy. |
| `stayLabels` | `object` | yes | — | Stay/duration copy. |
| `lang` | `string` | yes | — | Locale (checkout return path). |
| `onClose` | `() => void` | yes | — | Close handler. |

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/DurationSlider`,
  `molecules/core/StripeCheckoutEmbed`, the mobility Stripe-checkout action.
- **Used by:** `organisms/mobility/StaysPanel`.

## Internationalisation

Copy via `labels` / `stayLabels`.

## Accessibility

The accessible `core/Modal`; the slider carries its own labelling.

## Related

`atoms/core/DurationSlider`, `molecules/core/StripeCheckoutEmbed`,
`organisms/mobility/StaysPanel`.
