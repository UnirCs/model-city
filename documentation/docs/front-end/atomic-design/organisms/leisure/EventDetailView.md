---
title: EventDetailView
sidebar_label: EventDetailView
sidebar_position: 3
---

# EventDetailView

`packages/leisure/components/organisms/EventDetailView.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

Renders the body of an event detail page: banner (or title fallback), description and
photo gallery, the schedule/place/ticketing aside, and the staff administration
controls. The back button is intentionally **not** rendered here so the page owns it.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `event` | `object` | yes | — | Event payload from the leisure service. |
| `place` | `object \| null` | yes | — | Bound city place (soft-failed) or `null`. |
| `t` | `object` | yes | — | `leisure.eventDetail` dictionary. |
| `tAdmin` | `object` | yes | — | `leisure.eventAdmin` dictionary. |
| `tBuy` | `object` | yes | — | `leisure.buyTicket` dictionary. |
| `typeLabels` | `object` | yes | — | `leisure.eventTypes` dictionary. |
| `lang` | `string` | yes | — | Locale. |
| `canEdit` / `canDelete` / `canViewTickets` / `canBuy` | `boolean` | yes | — | Capability flags gating the ticketing and admin controls. |

## Composition

- **Uses:** `molecules/leisure/PhotoGallery`, `molecules/leisure/BuyTicketButton`,
  `molecules/leisure/DeleteEntityButton`, `molecules/core/AdminSection`,
  `atoms/core/Icon`, `next/link`, format utils.
- **Used by:** the event detail page.

## Internationalisation

Copy from `leisure.eventDetail` / `eventAdmin` / `buyTicket` and `eventTypes`.

## Accessibility

Structured detail sections with headings; admin controls only render when the
capability flags allow.

## Related

`molecules/leisure/BuyTicketButton`, `organisms/leisure/EventTicketsTable`,
`organisms/leisure/EventForm`.
