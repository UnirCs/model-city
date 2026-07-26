---
title: AlertsMapClient
sidebar_label: AlertsMapClient
sidebar_position: 2
---

# AlertsMapClient

`packages/engagement/components/molecules/AlertsMapClient.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

The client-side entry point for [`AlertsMap`](./AlertsMap.md). `next/dynamic` with
`ssr: false` must live in a Client Component, so server pages import this thin wrapper
instead of `AlertsMap` (see the map wrapper pattern in
[Design system & tokens](../../../architecture/design-system.md)).

## Props

Forwards its props to the dynamically-loaded `AlertsMap`.

## Composition

- **Uses:** `next/dynamic` → `molecules/engagement/AlertsMap` with `{ ssr: false }`.
- **Used by:** `organisms/engagement/AlertCenterView`.

## Internationalisation

None (delegates to `AlertsMap`).

## Related

`molecules/engagement/AlertsMap`, `molecules/leisure/PlaceMapClient`.
