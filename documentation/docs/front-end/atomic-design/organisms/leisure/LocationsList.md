---
title: LocationsList
sidebar_label: LocationsList
sidebar_position: 9
---

# LocationsList

`packages/leisure/components/organisms/LocationsList.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component (async)

## Purpose

Fetches one page of standalone city places (optionally filtered by category) and
renders the card grid with pagination, plus fetch-error and empty states. Staff see a
"create location" card in the grid and in the empty state.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `page` | `number` | yes | — | Page index. |
| `activeCategory` | `string \| null` | yes | — | Category filter. |
| `accessToken` | `string \| undefined` | yes | — | Bearer token. |
| `canCreate` | `boolean` | yes | — | Shows the create card. |
| `t` | `object` | yes | — | `tourism.locations` dictionary. |
| `units` | `object` | yes | — | `tourism.units` dictionary (for durations). |
| `pageHref` | `(pageNumber) => string` | yes | — | Pager href builder. |

## Behaviour

`await getCityPlaces({ page, category }, accessToken)`; cards show a category
fallback icon and an optional visit-duration extra.

## Composition

- **Uses:** `molecules/core/CatalogueCard`, `molecules/core/FetchErrorBanner`,
  `molecules/core/SectionPager`, `molecules/leisure/CreateCard`, `atoms/core/Icon`,
  `getCityPlaces` + format utils.
- **Used by:** the tourism locations list page.

## Internationalisation

Copy from `tourism.locations`; durations formatted with `units`.

## Accessibility

`<section aria-labelledby>` with an `<h2>`.

## Related

`organisms/leisure/LocationFilters`, `organisms/leisure/PlaceDetailView`,
`molecules/core/CatalogueCard`.
