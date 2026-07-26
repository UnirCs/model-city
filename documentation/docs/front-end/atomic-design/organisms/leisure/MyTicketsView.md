---
title: MyTicketsView
sidebar_label: MyTicketsView
sidebar_position: 10
---

# MyTicketsView

`packages/leisure/components/organisms/MyTicketsView.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

Renders the citizen's tickets: a header, an optional fetch-error banner, the
always-visible "this week" section, collapsible **upcoming** and **past** sections
(each independently paginated via URL search params), and an empty state. Each ticket
card can open its QR via [`TicketQrModal`](../../molecules/leisure/TicketQrModal.md).
The back button is owned by the page, not this view.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `t` | `object` | yes | — | `leisure.myTickets` dictionary. |
| `tBuy` | `object` | yes | — | `leisure.buyTicket` dictionary (for the free label). |
| `fetchError` | `boolean` | yes | — | Renders the error banner. |
| `isEmpty` | `boolean` | yes | — | Renders the empty state. |
| `sections` | `{ week, future, past }` | yes | — | Each `{ tickets, currentPage, totalPages }`. |

## Behaviour

An internal `TicketGrid` renders `CatalogueCard`s (with a QR secondary action);
past tickets are shown grayscaled. Selecting a card's QR opens the shared
`TicketQrModal`.

## Composition

- **Uses:** `molecules/core/CatalogueCard`, `molecules/core/CollapsibleHeader`,
  `molecules/leisure/TicketQrModal`, `atoms/core/Icon`, format utils.
- **Used by:** the `/events/my-tickets` page.

## Internationalisation

Copy from `leisure.myTickets` / `buyTicket`.

## Accessibility

Collapsible sections via `CollapsibleHeader` (`aria-expanded`); the QR is the
accessible `core/Modal`.

## Related

`molecules/leisure/TicketQrModal`, `organisms/leisure/EventDetailView`,
`molecules/core/CatalogueCard`.
