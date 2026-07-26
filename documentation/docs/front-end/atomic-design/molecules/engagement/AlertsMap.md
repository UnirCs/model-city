---
title: AlertsMap
sidebar_label: AlertsMap
sidebar_position: 1
---

# AlertsMap

`packages/engagement/components/molecules/AlertsMap.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

A multi-marker **MapLibre** map that places every security alert. It includes a
Map/List view toggle for keyboard-only access, keyboard-focusable markers, native
zoom controls, a compact list view exposing all alert info without the map, and a
full-screen overlay (Escape to close). Must be imported via
[`AlertsMapClient`](./AlertsMapClient.md).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `alerts` | `Array<{ id, severity, description, title?, latitude, longitude }>` | yes | — | The alerts to plot. |
| `mapTitle` | `string` | yes | — | Accessible map title. |
| `expandLabel` | `string` | no | `'Expand map'` | Expand-button label. |
| `closeLabel` | `string` | no | — | Overlay close label. |

## Behaviour

Markers are severity-coloured and keyboard-operable (Tab + Enter/Space opens the
popup). The List view renders the same alerts as an accessible list — satisfying SC
2.1.1 / 2.1.3 / 2.5.1 / 2.5.7.

## Composition

- **Uses:** `maplibre-gl` (+ CSS), `atoms/core/Icon`, severity helpers.
- **Used by:** `organisms/engagement/AlertCenterView` via `AlertsMapClient`.

## Internationalisation

Map/expand/close labels come from the caller.

## Accessibility

Map/List toggle for non-pointer users; focusable markers; overlay is a
`role="dialog" aria-modal`.

## Related

`molecules/engagement/AlertsMapClient`, `molecules/engagement/SecurityAlertCard`,
`molecules/leisure/PlaceMap`.
