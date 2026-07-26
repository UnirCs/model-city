---
title: MapSkeleton
sidebar_label: MapSkeleton
sidebar_position: 5
---

# MapSkeleton

`packages/core/components/molecules/skeletons/MapSkeleton.js` · **Tier:** molecule (skeleton) · **Module:** core · **Rendering:** Server Component

## Purpose

A large rectangular placeholder for embedded maps, optionally with a side legend
column of list-item placeholders.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `withLegend` | `boolean` | no | `false` | Render a side legend column beside the map. |

## Composition

- **Uses:** `atoms/core/Skeleton`.
- **Used by:** map-page `loading.js` files (alerts centre, place/route maps).

## Accessibility

All `Skeleton` blocks are `aria-hidden`.

## Related

`atoms/core/Skeleton`, the map molecules.
