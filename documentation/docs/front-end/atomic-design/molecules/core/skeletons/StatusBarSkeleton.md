---
title: StatusBarSkeleton
sidebar_label: StatusBarSkeleton
sidebar_position: 7
---

# StatusBarSkeleton

`packages/core/components/molecules/skeletons/StatusBarSkeleton.js` · **Tier:** molecule (skeleton) · **Module:** core · **Rendering:** Server Component

## Purpose

A wide horizontal bar placeholder for filter bars, status banners and similar
in-page strips, with a configurable number of inline chip placeholders.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `chips` | `number` | no | `4` | Number of inline chip placeholders. |

## Composition

- **Uses:** `atoms/core/Skeleton`.
- **Used by:** pages with a filter/status bar (`loading.js`).

## Accessibility

All `Skeleton` blocks are `aria-hidden`.

## Related

`molecules/core/AdminUserFilters`, `molecules/mobility/MobilityFilters`,
`atoms/core/Skeleton`.
