---
title: LocationFilters
sidebar_label: LocationFilters
sidebar_position: 8
---

# LocationFilters

`packages/leisure/components/organisms/LocationFilters.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

A single-select city-place category filter. Each chip links to the locations list
with the matching `?category=` query (selecting the active one clears it).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | For building hrefs. |
| `t` | `object` | yes | — | `tourism.locations` dictionary. |
| `categoryLabels` | `object` | yes | — | `tourism.placeDetail.categoryLabels`. |
| `activeCategory` | `string \| null` | yes | — | The active category. |

## Composition

- **Uses:** `molecules/leisure/FilterSection`, `molecules/leisure/FilterChips`,
  `categoryIcon` (`leisure/lib/utils/format`).
- **Used by:** the tourism locations list page (alongside `LocationsList`).

## Internationalisation

Copy from `t`; category labels from `categoryLabels`.

## Accessibility

Delegates to `FilterSection`/`FilterChips`.

## Related

`organisms/leisure/LocationsList`, `organisms/leisure/EventFilters`,
`molecules/leisure/FilterChips`.
