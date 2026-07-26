---
title: EventTicketsTable
sidebar_label: EventTicketsTable
sidebar_position: 6
---

# EventTicketsTable

`packages/leisure/components/organisms/EventTicketsTable.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

Renders the (paginated) table of sold tickets for a given event. A single shared
refund modal is mounted once at the table level and opened with the selected ticket's
data — avoiding the N-modals / N-effects pattern that causes cascading re-renders.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `eventId` | `number \| string` | yes | — | The event whose tickets are shown. |
| `tickets` | `Array<object>` | yes | — | The sold tickets. |
| `t` | `object` | yes | — | Table dictionary (column labels, empty state). |
| `statusLabels` | `Record<string,string>` | yes | — | Localized ticket-status labels. |
| `refundLabels` | `object` | yes | — | Refund modal copy (title, reason label/placeholder, error messages). |
| `lang` | `string` | yes | — | Locale. |

## Behaviour

The refund modal validates a required reason (≤ 512 chars) and calls the
`refundTicket` action, mapping `reason_required` / `reason_too_long` / `stripe_error`
to specific messages. It remounts (via key) per selected ticket so its local state is
clean.

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/Button`, `atoms/core/Icon`,
  `refundTicket` action, `useState`/`useTransition`.
- **Used by:** the event tickets page (staff).

## Internationalisation

All copy via `t` / `statusLabels` / `refundLabels`.

## Accessibility

The refund modal is the accessible `core/Modal`; the reason textarea is labelled with
a live character counter.

## Related

`organisms/leisure/EventDetailView`, `molecules/core/skeletons/TableSkeleton`,
`organisms/mobility/TicketsTable`.
