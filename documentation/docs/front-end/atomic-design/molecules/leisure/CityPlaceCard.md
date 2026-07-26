---
title: CityPlaceCard
sidebar_label: CityPlaceCard
sidebar_position: 2
---

# CityPlaceCard

`packages/leisure/components/molecules/CityPlaceCard.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Server Component

## Purpose

A compact card for a single point of interest inside a city-route detail. The whole
card links to the place detail page and carries a left secondary-coloured accent
stripe marking it as a child of the parent route, plus a 1-based ordinal badge.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `place` | `{ id, name, category, coverPhotoUrl? }` | yes | — | The point of interest. |
| `order` | `number` | yes | — | 1-based ordinal, displayed zero-padded (`01`, `02`, …). |
| `routeId` | `number \| string` | yes | — | Parent route id (for the detail href). |
| `t` | `object` | yes | — | `tourism.routeDetail` dictionary (`viewPlace`). |
| `categoryLabels` | `Record<string,string>` | yes | — | Localized category labels. |
| `lang` | `string` | yes | — | For the detail href. |

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`, `categoryIcon`/`categoryLabel`
  (`leisure/lib/utils/format`).
- **Used by:** `organisms/leisure/RouteDetailView`.

## Internationalisation

`t.viewPlace` + `categoryLabels` supplied by the caller.

## Accessibility

The whole card is a link; the accent stripe is `aria-hidden`; the cover thumbnail is
lazy-loaded with `alt`.

## Styling & tokens

`bg-surface-container-low`, `border-outline-variant`, `rounded-md`, `bg-secondary`
accent stripe, `bg-primary` ordinal badge.

## Related

`molecules/core/CatalogueCard`, `organisms/leisure/RouteDetailView`.
