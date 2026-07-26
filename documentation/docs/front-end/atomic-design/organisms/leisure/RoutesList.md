---
title: RoutesList
sidebar_label: RoutesList
sidebar_position: 15
---

# RoutesList

`packages/leisure/components/organisms/RoutesList.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

A card grid of city routes with pagination, plus fetch-error and empty states. Staff
see a "create route" card as the first item in the grid. Unlike `EventsList`, the
data is fetched by the page and passed in as props.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `t` | `object` | yes | — | `tourism.routes` dictionary. |
| `units` | `object` | yes | — | `tourism.units` dictionary. |
| `canCreate` | `boolean` | yes | — | Shows the create card. |
| `fetchError` | `boolean` | yes | — | Renders the error banner. |
| `routes` | `object[]` | yes | — | The route page content. |
| `page` | `number` | yes | — | Page index. |
| `totalPages` | `number` | yes | — | Total pages. |
| `pageHref` | `(pageNumber) => string` | yes | — | Pager href builder. |

## Composition

- **Uses:** `molecules/core/CatalogueCard`, `molecules/core/FetchErrorBanner`,
  `molecules/core/SectionPager`, `molecules/leisure/CreateCard`, `atoms/core/Icon`,
  `formatDuration`.
- **Used by:** the tourism routes list page.

## Internationalisation

Copy from `tourism.routes`; place counts and durations use `t`/`units`.

## Accessibility

`<section aria-labelledby>` with an `<h2>`.

## Related

`organisms/leisure/RouteDetailView`, `organisms/leisure/LocationsList`,
`molecules/core/CatalogueCard`.
