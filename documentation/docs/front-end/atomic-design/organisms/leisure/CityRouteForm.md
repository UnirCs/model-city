---
title: CityRouteForm
sidebar_label: CityRouteForm
sidebar_position: 2
---

# CityRouteForm

`packages/leisure/components/organisms/CityRouteForm.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The shared form for creating (POST) or updating (PUT) a city route. Two columns: the
left holds the form fields and the two place pickers ("available" + "selected"); the
right holds a live map preview that re-renders as the selected places change. The
selected list is capped at 6 entries and supports manual reordering (up/down),
because the backend expects the ids in itinerary order.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `mode` | `'create' \| 'edit'` | yes | — | Create or update. |
| `route` | `object` | no | — | Existing route (edit): name, description, targetAudience, imageUrl, estimatedDurationMinutes, cityPlaces. |
| `availablePlaces` | `Array<object>` | yes | — | Catalogue of selectable places. |
| `t` / `lang` | — | yes | — | Route-form dictionary + locale. |

(See the source JSDoc for the full prop set.)

## Composition

- **Uses:** `MultilingualFields`, `molecules/leisure/RouteMapClient`,
  `atoms/core/FormField`, `atoms/core/Button`, the route server actions,
  `useLocalizedBack`.
- **Used by:** the create/edit route pages.

## Internationalisation

Translatable fields via `MultilingualFields`; labels from the dictionary.

## Accessibility

Reorder controls are labelled buttons; fields via `FormField`; the map preview is a
`RouteMap`.

## Related

`molecules/leisure/RouteMapClient`, `organisms/leisure/RouteDetailView`,
`molecules/core/MultilingualFields`.
