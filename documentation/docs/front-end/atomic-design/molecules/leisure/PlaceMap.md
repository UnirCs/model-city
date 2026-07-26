---
title: PlaceMap
sidebar_label: PlaceMap
sidebar_position: 10
---

# PlaceMap

`packages/leisure/components/molecules/PlaceMap.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

A focused **MapLibre** map for a single city place, with a teardrop marker and a
popup opened on load. An expand button (top-right) opens a full-screen overlay with a
blurred backdrop and a scale animation; Escape or an outside click closes it. Must be
imported via [`PlaceMapClient`](./PlaceMapClient.md) (the `ssr: false` wrapper).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `latitude` | `number` | yes | — | Marker latitude. |
| `longitude` | `number` | yes | — | Marker longitude. |
| `name` | `string` | yes | — | Place name (marker `aria-label` + popup). |
| `mapTitle` | `string` | yes | — | Accessible map title. |
| `lang` | `string` | no | `'es'` | Drives the map label language (`name:<code>` with a fallback to `name`). |
| `expandLabel` | `string` | no | `'Ampliar mapa'` | Expand-button label. |
| `closeLabel` | `string` | no | `'Cerrar mapa'` | Overlay close label. |

## Behaviour

Uses the OpenFreeMap positron style (no API key). Navigation controls sit top-left so
they never overlap the expand/close button. Markers are keyboard-focusable
(Enter/Space toggles the popup) with a visible focus ring.

## Composition

- **Uses:** `maplibre-gl` (+ CSS), `atoms/core/Icon`, `useTranslations()`.
- **Used by:** `molecules/leisure/LocationSection` and place detail views via
  `PlaceMapClient`.

## Internationalisation

Map labels follow `lang`; the a11y strings come from the dictionary / caller.

## Accessibility

Marker is a focusable `role="button"` with `aria-label`; the overlay is a
`role="dialog" aria-modal`.

## Related

`molecules/leisure/PlaceMapClient`, `molecules/leisure/RouteMap`,
`molecules/core/LocationPickerMap`.
