---
title: PlaceDetailView
sidebar_label: PlaceDetailView
sidebar_position: 11
---

# PlaceDetailView

`packages/leisure/components/organisms/PlaceDetailView.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

Renders the shared body of a city-place detail (banner, gallery, description with a
visit-duration footer, access and accessibility info, map, and an address card with
the "open in external maps" CTA). It is used by **both** the route-scoped detail page
and the standalone locations detail page since the backend payload is identical. The
back button is owned by each page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `place` | `object` | yes | — | `{ id, name, description?, address?, latitude?, longitude?, category?, visitDurationMinutes?, accessInfo?, accessibilityInfo?, photoUrls? }`. |
| `t` | `object` | yes | — | `tourism.placeDetail` dictionary. |
| `units` | `{ hours, minutes }` | yes | — | `tourism.units` dictionary. |
| `adminSlot` | `ReactNode` | no | `null` | Staff controls rendered in the aside. |

## Composition

- **Uses:** `molecules/leisure/PhotoGallery`, `molecules/leisure/LocationSection`,
  `atoms/core/Icon`, format utils.
- **Used by:** the standalone location detail page and the route-scoped place detail
  page.

## Internationalisation

Copy from `tourism.placeDetail`; durations via `units`.

## Accessibility

Structured sections with headings; the gallery and map carry their own a11y.

## Related

`organisms/leisure/RouteDetailView`, `molecules/leisure/LocationSection`,
`molecules/leisure/PhotoGallery`.
