---
title: SpaceDetailView
sidebar_label: SpaceDetailView
sidebar_position: 16
---

# SpaceDetailView

`packages/leisure/components/organisms/SpaceDetailView.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

Renders the body of a public (sports) space detail page: banner (or title fallback),
description, photo gallery, the reservable-resources panel, the location aside, and
the staff administration controls. The back button is owned by the page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `space` | `object` | yes | — | Public space payload. |
| `resources` | `object[]` | yes | — | Reservable resources of the space. |
| `t` | `object` | yes | — | `leisure.spaceDetail` dictionary. |
| `tAdmin` | `object` | yes | — | `leisure.adminActions` dictionary. |
| `tRes` | `object` | yes | — | `leisure.resources` dictionary. |
| `tResForm` | `object` | yes | — | `leisure.resourceForm` dictionary. |
| `tTypes` | `object` | yes | — | `leisure.resourceTypes` dictionary. |
| `lang` | `string` | yes | — | Locale. |
| `canManageSpace` | `boolean` | yes | — | Shows the space admin controls. |
| `canManageResources` | `boolean` | yes | — | Enables resource management. |

## Composition

- **Uses:** `molecules/leisure/PhotoGallery`, `molecules/leisure/LocationSection`,
  `organisms/leisure/SpaceResourcesPanel`, `molecules/core/AdminSection`,
  `molecules/leisure/DeleteEntityButton`, `atoms/core/Icon`, `next/link`.
- **Used by:** the sports-space detail page.

## Internationalisation

Copy from `leisure.spaceDetail` and related resource dictionaries.

## Accessibility

Structured sections with headings; admin/resource controls are capability-gated.

## Related

`organisms/leisure/SpaceResourcesPanel`, `organisms/leisure/PublicSpaceForm`,
`molecules/leisure/LocationSection`.
