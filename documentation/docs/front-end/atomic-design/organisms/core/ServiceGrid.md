---
title: ServiceGrid
sidebar_label: ServiceGrid
sidebar_position: 12
---

# ServiceGrid

`packages/core/components/organisms/ServiceGrid.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

A responsive grid of municipal service cards for the authenticated home page. On
mobile it uses two columns with horizontal padding to keep cards off the screen
edges.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `services` | `Array<{ id, title, description, icon, href }>` | no | `[]` | The service cards to render (already filtered for enabled modules). |

## Composition

- **Uses:** `molecules/core/ServiceCard`.
- **Used by:** the home page (`core/routes/home`).

## Internationalisation

None directly — service titles/descriptions are supplied by the caller.

## Accessibility

`<section aria-labelledby="services-heading">`.

## Styling & tokens

`grid grid-cols-2 xl:grid-cols-3`, `gap-sm md:gap-md`.

## Related

`molecules/core/ServiceCard`, `organisms/core/LandingServices`,
[Modularity](../../../architecture/modularity.md) (home-grid module filtering).
