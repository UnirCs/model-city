---
title: PlaceMapClient
sidebar_label: PlaceMapClient
sidebar_position: 11
---

# PlaceMapClient

`packages/leisure/components/molecules/PlaceMapClient.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The client-side entry point for [`PlaceMap`](./PlaceMap.md). `next/dynamic` with
`ssr: false` must live in a Client Component, so server pages import this thin
wrapper instead of `PlaceMap` (see the map wrapper pattern in
[Design system & tokens](../../../architecture/design-system.md)).

## Props

Forwards its props to the dynamically-loaded `PlaceMap` (`latitude`, `longitude`,
`name`, `mapTitle`).

## Composition

- **Uses:** `next/dynamic` → `molecules/leisure/PlaceMap` with `{ ssr: false }`.
- **Used by:** `molecules/leisure/LocationSection`, `organisms/leisure/PlaceDetailView`.

## Internationalisation

None (delegates to `PlaceMap`).

## Related

`molecules/leisure/PlaceMap`, `molecules/core/LocationPickerMapClient`.
