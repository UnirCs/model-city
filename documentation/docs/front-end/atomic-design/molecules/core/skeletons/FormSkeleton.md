---
title: FormSkeleton
sidebar_label: FormSkeleton
sidebar_position: 3
---

# FormSkeleton

`packages/core/components/molecules/skeletons/FormSkeleton.js` · **Tier:** molecule (skeleton) · **Module:** core · **Rendering:** Server Component

## Purpose

A loading placeholder for forms: a title, N label+field pairs, and a submit/cancel
button row.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `fields` | `number` | no | `5` | Number of field placeholders. |
| `className` | `string` | no | `''` | Extra classes on the panel. |

## Composition

- **Uses:** `atoms/core/Skeleton`.
- **Used by:** create/edit route `loading.js` files.

## Accessibility

All `Skeleton` blocks are `aria-hidden`.

## Related

`atoms/core/Skeleton`, the form organisms.
