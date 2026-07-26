---
title: TableSkeleton
sidebar_label: TableSkeleton
sidebar_position: 8
---

# TableSkeleton

`packages/core/components/molecules/skeletons/TableSkeleton.js` · **Tier:** molecule (skeleton) · **Module:** core · **Rendering:** Server Component

## Purpose

A placeholder for tabular admin views: a toolbar, a header row and N data rows of M
columns.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `rows` | `number` | no | `8` | Number of data-row placeholders. |
| `columns` | `number` | no | `5` | Number of columns (drives the CSS grid). |

## Composition

- **Uses:** `atoms/core/Skeleton`.
- **Used by:** staff table `loading.js` files (`TicketsTable`, `EventTicketsTable`,
  `SanctionsManageList` pages).

## Accessibility

All `Skeleton` blocks are `aria-hidden`.

## Related

`organisms/mobility/TicketsTable`, `organisms/leisure/EventTicketsTable`,
`atoms/core/Skeleton`.
