---
title: SpaceResourcesPanel
sidebar_label: SpaceResourcesPanel
sidebar_position: 17
---

# SpaceResourcesPanel

`packages/leisure/components/organisms/SpaceResourcesPanel.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

Renders the list of bookable resources of a public space. Read-only for citizens;
operators/admins additionally get add / edit / delete controls through a single shared
modal. Each resource links to its reservations page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `spaceId` | `string \| number` | yes | — | Owning space id. |
| `resources` | `Array<{ id, name, description?, resourceType }>` | yes | — | The bookable resources. |
| `canManage` | `boolean` | yes | — | Enables add/edit/delete. |
| `t` | `object` | yes | — | `leisure.resources` dictionary. |
| `tForm` | `object` | yes | — | `leisure.resourceForm` dictionary. |
| `tTypes` | `Record<string,string>` | yes | — | `leisure.resourceTypes` labels. |
| `lang` | `string` | yes | — | Locale. |

## Behaviour

A single shared modal handles create/edit; delete uses a confirmation. Each resource
row links to `/sports-spaces/{spaceId}/resources/{id}`.

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/FormField`, `atoms/core/Button`,
  `atoms/core/Icon`, the space-resource server actions.
- **Used by:** `organisms/leisure/SpaceDetailView`.

## Internationalisation

Copy from `leisure.resources` / `resourceForm` / `resourceTypes`.

## Accessibility

The create/edit modal is the accessible `core/Modal`; resource rows are labelled
links.

## Related

`organisms/leisure/ResourceReservationsPanel`, `organisms/leisure/SpaceDetailView`,
`molecules/core/Modal`.
