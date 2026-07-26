---
title: LocationPickerMap
sidebar_label: LocationPickerMap
sidebar_position: 13
---

# LocationPickerMap

`packages/core/components/molecules/LocationPickerMap.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

An interactive **MapLibre** map that lets the user pick a single point. Each click
updates the latitude/longitude through `onChange`. It also offers a "use my
location" geolocation button and native zoom controls. It must be imported via
[`LocationPickerMapClient`](./LocationPickerMapClient.md) (the `ssr: false` dynamic
wrapper).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `latitude` | `number \| null` | yes | — | Current marker latitude (`null` = no marker yet). |
| `longitude` | `number \| null` | yes | — | Current marker longitude. |
| `onChange` | `(coords: { latitude, longitude }) => void` | yes | — | Called on map click or successful geolocation. |
| `mapTitle` | `string` | yes | — | Accessible name for the map region and marker. |

## Behaviour

Creates the map once (OpenFreeMap positron style, no API key), defaulting to the
Aranjuez town centre when there is no initial marker. External coordinate changes
(e.g. a form reset) re-sync the marker and ease the camera. Geolocation uses
`navigator.geolocation.getCurrentPosition` with high accuracy and surfaces
permission/timeout errors.

## Composition

- **Uses:** `maplibre-gl` (+ its CSS), `atoms/core/Icon`, `useTranslations()`.
- **Used by:** the form organisms via `LocationPickerMapClient`
  (`SecurityAlertForm`, `CreateSanctionForm`, place/space forms).

## Internationalisation

Reads the `a11y` dictionary slice for the map keyboard hint and geolocation states
(`mapKeyboardHint`, `useMyLocation`, `locating`, `locationError`, `locationDenied`).

## Accessibility

The map container is `role="application"` with `aria-label={mapTitle}`; a keyboard
hint accompanies it; the geolocation error uses `role="alert"`. (Satisfies SC
2.1.1 / 2.1.3 / 2.5.1 / 2.5.7 with a manual-coordinate alternative.)

## Styling & tokens

`h-[500px]`, `rounded-md`, `border-outline-variant`, teardrop marker in the
secondary colour.

## Usage

```jsx
import LocationPickerMapClient from '@modelcity/core/components/molecules/LocationPickerMapClient';
<LocationPickerMapClient latitude={lat} longitude={lng} onChange={setCoords} mapTitle={t.map.pick} />
```

## Related

`molecules/core/LocationPickerMapClient` (the wrapper), `molecules/leisure/PlaceMap`,
`molecules/engagement/AlertsMap`.
