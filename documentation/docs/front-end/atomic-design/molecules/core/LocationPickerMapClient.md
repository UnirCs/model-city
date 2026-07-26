---
title: LocationPickerMapClient
sidebar_label: LocationPickerMapClient
sidebar_position: 14
---

# LocationPickerMapClient

`packages/core/components/molecules/LocationPickerMapClient.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The client-side entry point for [`LocationPickerMap`](./LocationPickerMap.md).
`next/dynamic` with `ssr: false` must live in a Client Component, so forms import
this thin wrapper instead of the map itself (see the map wrapper pattern in
[Design system & tokens](../../../architecture/design-system.md)).

## Props

None of its own — it forwards nothing explicitly; callers pass the map's props
through the dynamically-loaded `LocationPickerMap`.

## Composition

- **Uses:** `next/dynamic` → `molecules/core/LocationPickerMap` with `{ ssr: false }`.
- **Used by:** the create/edit form organisms that place a single-point picker
  (e.g. `SecurityAlertForm`, `CreateSanctionForm`).

## Internationalisation

None (delegates to `LocationPickerMap`).

## Accessibility

Inherits the underlying map's semantics.

## Usage

```jsx
import LocationPickerMapClient from '@modelcity/core/components/molecules/LocationPickerMapClient';
<LocationPickerMapClient value={coords} onChange={setCoords} />
```

## Related

`molecules/core/LocationPickerMap` (the actual map), and the map pattern in
[Design system & tokens](../../../architecture/design-system.md).
