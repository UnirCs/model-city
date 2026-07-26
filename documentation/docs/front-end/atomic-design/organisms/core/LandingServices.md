---
title: LandingServices
sidebar_label: LandingServices
sidebar_position: 6
---

# LandingServices

`packages/core/components/organisms/LandingServices.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

A theme-aware strip below the landing banner summarising the six citizen services
in a compact 3×2 grid (events, tourism map, participation, security, bookings,
mobility).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | The landing dictionary (reads `t.services.*`). |

## Composition

- **Uses:** `atoms/core/Icon`; builds a static six-item list internally.
- **Used by:** the public landing page.

## Internationalisation

Labels/descriptions from the `landing` dictionary's `services` slice.

## Accessibility

`<section aria-labelledby>`; each service tile pairs an icon with a label and
description.

## Styling & tokens

`bg-surface-container-low`, `border-outline-variant`, `bg-secondary-container/25`
icon badges, `rounded-md`.

## Related

`molecules/core/ServiceCard`, `organisms/core/ServiceGrid` (the authenticated home
grid), `organisms/core/LandingBanner`.
