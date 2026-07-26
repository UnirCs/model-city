---
title: AdminUserFilters
sidebar_label: AdminUserFilters
sidebar_position: 2
---

# AdminUserFilters

`packages/core/components/molecules/AdminUserFilters.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A filter bar that drives the URL search params for the Administration → Citizens and
Workers grids (the same SSR re-fetch pattern as `MobilityFilters`). It always
exposes a name search; it adds a neighbourhood select in `citizens` mode and a role
select in `workers` mode. Every change resets `page` to 0.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `mode` | `'citizens' \| 'workers'` | yes | — | Which extra filter to show (neighbourhood vs role). |
| `labels` | `object` | yes | — | Localized copy (`filtersTitle`, `nameLabel`, `namePlaceholder`, `neighbourhoodLabel`/`Any`, `roleLabel`/`Any`, `apply`, `reset`, `roleLabels`). |
| `initial` | `{ name?, neighbourhoodId?, role? }` | yes | — | Initial values (re-synced from the URL on back/forward). |

## Behaviour

Builds a `URLSearchParams` from the non-empty fields and `router.push`es the path.
Neighbourhood options come from `core/lib/config/neighbourhoods` (grouped by zone);
worker roles are `OPERATOR`, `BACKOFFICE`, `MOBILITY_AGENT` from `ROLES`.

## Composition

- **Uses:** `atoms/core/Button`, `atoms/core/FormField`, `atoms/core/Icon`,
  `ROLES` + `neighbourhoods` config, `useRouter`/`useSearchParams`/`usePathname`.
- **Used by:** `organisms/core/UserCardGrid` / the administration list pages.

## Internationalisation

All copy via `labels`; role option text via `labels.roleLabels`.

## Accessibility

Each control is wrapped in `FormField` (labelled); selects use a custom chevron with
`appearance-none`.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, responsive
grid.

## Usage

```jsx
<AdminUserFilters mode="citizens" labels={t.admin.filters} initial={initial} />
```

## Related

`molecules/mobility/MobilityFilters`, `organisms/core/UserCardGrid`,
`atoms/core/FormField`.
