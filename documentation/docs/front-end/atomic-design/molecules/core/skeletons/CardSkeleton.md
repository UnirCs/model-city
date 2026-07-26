---
title: CardSkeleton
sidebar_label: CardSkeleton
sidebar_position: 1
---

# CardSkeleton

`packages/core/components/molecules/skeletons/CardSkeleton.js` · **Tier:** molecule (skeleton) · **Module:** core · **Rendering:** Server Component

## Purpose

A loading placeholder mirroring a generic content card (optional top image, title,
body lines and a footer row).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `withImage` | `boolean` | no | `true` | Show the top image placeholder. |
| `lines` | `number` | no | `2` | Number of body text lines. |
| `className` | `string` | no | `''` | Extra classes on the card wrapper. |

## Composition

- **Uses:** `atoms/core/Skeleton`.
- **Used by:** `molecules/core/skeletons/ListSkeleton`, list-page `loading.js` files.

## Accessibility

Composed entirely of `Skeleton` (each `aria-hidden`); conveys no information.

## Related

`molecules/core/skeletons/ListSkeleton`, `atoms/core/Skeleton`.
