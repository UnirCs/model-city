---
title: RouteMapClient
sidebar_label: RouteMapClient
sidebar_position: 13
---

# RouteMapClient

`packages/leisure/components/molecules/RouteMapClient.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The client-side entry point for [`RouteMap`](./RouteMap.md). `next/dynamic` with
`ssr: false` must live in a Client Component, so server pages import this thin
wrapper instead of `RouteMap` (see the map wrapper pattern in
[Design system & tokens](../../../architecture/design-system.md)).

## Props

Forwards its props to the dynamically-loaded `RouteMap`.

## Composition

- **Uses:** `next/dynamic` → `molecules/leisure/RouteMap` with `{ ssr: false }`.
- **Used by:** `organisms/leisure/RouteDetailView`.

## Internationalisation

None (delegates to `RouteMap`).

## Related

`molecules/leisure/RouteMap`, `molecules/leisure/PlaceMapClient`.
