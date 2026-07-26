---
title: CreateCard
sidebar_label: CreateCard
sidebar_position: 3
---

# CreateCard

`packages/leisure/components/molecules/CreateCard.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Server Component

## Purpose

A dashed "add new entity" card placed at the start of catalogue grids (and in empty
states) to let staff create a new entity.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `href` | `string` | yes | — | Create-page destination. |
| `label` | `string` | yes | — | Card label. |
| `hint` | `string` | no | — | Secondary hint line. |
| `className` | `string` | no | `''` | Extra classes. |

## Composition

- **Uses:** `atoms/core/Icon` (`add`), `next/link`.
- **Used by:** the leisure list organisms (`EventsList`, `SpacesList`, `RoutesList`,
  `LocationsList`) for staff.

## Internationalisation

`label`/`hint` supplied (localized) by the caller.

## Accessibility

A single link with a clear label; dashed border signals its "create" affordance.

## Styling & tokens

`border-2 border-dashed border-secondary/40`, `bg-surface-container-lowest`,
`rounded-md`, hover secondary tint.

## Related

`molecules/core/CatalogueCard`, `organisms/leisure/EventsList`.
