---
title: ListSkeleton
sidebar_label: ListSkeleton
sidebar_position: 4
---

# ListSkeleton

`packages/core/components/molecules/skeletons/ListSkeleton.js` · **Tier:** molecule (skeleton) · **Module:** core · **Rendering:** Server Component

## Purpose

A responsive grid of [`CardSkeleton`](./CardSkeleton.md)s, optionally wrapped in a
surface panel (matching the boxed look of list/consultation pages) preceded by a
section-heading placeholder.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `count` | `number` | no | `6` | Number of card placeholders. |
| `withImage` | `boolean` | no | `true` | Forwarded to each `CardSkeleton`. |
| `className` | `string` | no | `''` | Extra classes on the grid. |
| `contained` | `boolean` | no | `false` | Wrap the grid in a surface panel with a heading placeholder. |

## Composition

- **Uses:** `atoms/core/Skeleton`, `molecules/core/skeletons/CardSkeleton`.
- **Used by:** list-page `loading.js` files across modules.

## Accessibility

All `Skeleton` blocks are `aria-hidden`.

## Related

`molecules/core/skeletons/CardSkeleton`, `atoms/core/Skeleton`.
