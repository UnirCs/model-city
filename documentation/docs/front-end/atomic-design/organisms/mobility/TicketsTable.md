---
title: TicketsTable
sidebar_label: TicketsTable
sidebar_position: 7
---

# TicketsTable

`packages/mobility/components/organisms/TicketsTable.js` · **Tier:** organism · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

A read-only staff card grid listing street reservations. Each card exposes a "View on
map" link and an "Issue sanction" button (which deep-links to
`CreateSanctionForm` with the plate/coordinates pre-filled).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `reservations` | `Array<object>` | yes | — | The active reservations. |
| `labels` | `object` | yes | — | Table/card copy. |
| `lang` | `string` | yes | — | Locale. |

## Composition

- **Uses:** `atoms/core/Icon`/`Button`, `next/link`.
- **Used by:** the `/mobility/tickets` page (staff).

## Internationalisation

All copy via `labels`.

## Accessibility

Empty state and per-card actions are labelled links/buttons.

## Related

`organisms/mobility/CreateSanctionForm`, `molecules/mobility/MobilityFilters`,
`molecules/mobility/PaginationBar`, `organisms/leisure/EventTicketsTable`.
