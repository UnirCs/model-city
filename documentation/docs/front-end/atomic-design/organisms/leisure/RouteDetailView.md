---
title: RouteDetailView
sidebar_label: RouteDetailView
sidebar_position: 14
---

# RouteDetailView

`packages/leisure/components/organisms/RouteDetailView.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

Renders the body of a city-route detail page: a banner with the route name, an
"about" panel (description, practical details and route map), the paginated list of
points of interest, and the staff administration controls. The back button is owned
by the page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `route` | `object` | yes | — | City route payload. |
| `places` | `object[]` | yes | — | Current page of route points of interest. |
| `routeId` | `string` | yes | — | Route id (for pagination hrefs). |
| `page` | `number` | yes | — | Page index. |
| `totalPages` | `number` | yes | — | Total pages. |
| `t` | `object` | yes | — | `tourism.routeDetail` dictionary. |
| `units` | `object` | yes | — | `tourism.units` dictionary. |
| `tAdmin` | `object` | yes | — | `tourism.adminActions` dictionary. |
| `categoryLabels` | `object` | yes | — | Category labels for the place cards. |
| `lang` | `string` | yes | — | Locale. |
| `canManage` | `boolean` | yes | — | Shows the admin controls. |

## Composition

- **Uses:** `molecules/leisure/CityPlaceCard`, `molecules/leisure/RouteMapClient`,
  `molecules/core/SectionPager`, `molecules/core/AdminSection`,
  `molecules/leisure/DeleteEntityButton`, `atoms/core/Icon`, `next/link`.
- **Used by:** the tourism route detail page.

## Internationalisation

Copy from `tourism.routeDetail` / `adminActions`; durations via `units`.

## Accessibility

Structured sections with headings; POIs are rendered as `CityPlaceCard` links.

## Related

`molecules/leisure/CityPlaceCard`, `molecules/leisure/RouteMap`,
`organisms/leisure/CityRouteForm`.
