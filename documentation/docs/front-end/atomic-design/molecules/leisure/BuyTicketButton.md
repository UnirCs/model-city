---
title: BuyTicketButton
sidebar_label: BuyTicketButton
sidebar_position: 1
---

# BuyTicketButton

`packages/leisure/components/molecules/BuyTicketButton.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The sole entry point for a citizen to acquire an event ticket. Two flows: **free**
events call the `claimFreeTicket` action directly and refresh; **paid** events open
an in-page modal with the Stripe Embedded Checkout.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `eventId` | `number \| string` | yes | — | The event to buy/claim. |
| `paid` | `boolean` | yes | — | Whether the event requires payment. |
| `disabled` | `boolean` | no | — | Disables the button (e.g. sold out / already owned). |
| `lang` | `string` | yes | — | Passed to the checkout embed. |
| `labels` | `object` | yes | — | Localized copy (`buyPaid`, `claimFree`, `processing`, `successFree`, `errorGeneric`, `errorForbidden`, `modalTitle`, `close`, `misconfigured`). |

## Behaviour

Uses `useTransition` for the free-claim action; maps `forbidden` to a specific error;
announces success/error via `useAnnounce`. Paid flow mounts a `Modal` containing
`EventCheckoutEmbed`.

## Composition

- **Uses:** `atoms/core/Button`, `atoms/core/Icon`, `molecules/core/Modal`,
  `molecules/leisure/EventCheckoutEmbed`, `claimFreeTicket`
  (`leisure/lib/actions/events`), `useAnnounce`.
- **Used by:** `organisms/leisure/EventDetailView`.

## Internationalisation

All copy via `labels`; results announced through the live region.

## Accessibility

Success/error messages are announced (`polite`/`assertive`); the paid modal is the
accessible `core/Modal`.

## Related

`molecules/leisure/EventCheckoutEmbed`, `molecules/leisure/TicketQrModal`,
`organisms/leisure/EventDetailView`.
