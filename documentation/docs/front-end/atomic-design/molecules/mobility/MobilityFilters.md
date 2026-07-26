---
title: MobilityFilters
sidebar_label: MobilityFilters
sidebar_position: 4
---

# MobilityFilters

`packages/mobility/components/molecules/MobilityFilters.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

A generic filter bar that drives URL search params for the staff mobility tables. It
supports a license-plate input, a date range and (optionally) an `active` status
selector. On submit it re-navigates to the same pathname with the new params, which
triggers an SSR re-fetch.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `labels` | `object` | yes | — | Localized copy (field labels, apply/reset). |
| `showActive` | `boolean` | no | `false` | Shows the `active` status selector. |
| `initial` | `{ licensePlate?, fromDate?, toDate?, active? }` | yes | — | Initial values (re-synced from the URL). |

## Composition

- **Uses:** `atoms/core/FormField`/`Button`/`Icon`,
  `useRouter`/`useSearchParams`/`usePathname`.
- **Used by:** `organisms/mobility/TicketsTable`,
  `organisms/mobility/SanctionsManageList`.

## Internationalisation

All copy via `labels`.

## Accessibility

Fields wrapped in `FormField`; apply/reset are labelled buttons.

## Related

`molecules/core/AdminUserFilters`, `molecules/core/SystemTrailFilters`,
`molecules/mobility/PaginationBar`.
