---
title: RouteMap
sidebar_label: RouteMap
sidebar_position: 12
---

# RouteMap

`packages/leisure/components/molecules/RouteMap.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

A **MapLibre** map that draws a tourist route as a dashed polyline with numbered,
keyboard-focusable markers (`01`, `02`, …) for each stop, fitting the bounds to all
places. Like `PlaceMap`, it offers a full-screen expand overlay. Must be imported via
[`RouteMapClient`](./RouteMapClient.md) (the `ssr: false` wrapper).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `places` | `Array<{ id, name, latitude, longitude }>` | yes | — | The ordered route stops. |
| `mapTitle` | `string` | yes | — | Accessible map title. |
| `expandLabel` | `string` | no | `'Ampliar mapa'` | Expand-button label. |
| `closeLabel` | `string` | no | (localized) | Overlay close label. |

## Behaviour

Adds a white "casing" line under the dashed secondary route line, drops numbered
markers, opens a popup per stop (Enter/Space), and fits bounds when there is more than
one place. OpenFreeMap positron style, controls top-left.

## Composition

- **Uses:** `maplibre-gl` (+ CSS), `atoms/core/Icon`, `useTranslations()`.
- **Used by:** `organisms/leisure/RouteDetailView` via `RouteMapClient`.

## Internationalisation

The a11y/expand strings come from the dictionary / caller.

## Accessibility

Numbered markers are focusable `role="button"` elements with `aria-label` and a
visible focus ring; the overlay is a `role="dialog" aria-modal`.

## Related

`molecules/leisure/RouteMapClient`, `molecules/leisure/PlaceMap`,
`organisms/leisure/RouteDetailView`.
