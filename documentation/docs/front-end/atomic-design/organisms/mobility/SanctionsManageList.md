---
title: SanctionsManageList
sidebar_label: SanctionsManageList
sidebar_position: 5
---

# SanctionsManageList

`packages/mobility/components/organisms/SanctionsManageList.js` · **Tier:** organism · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

The staff list of every registered sanction, rendered as a responsive card grid, that
opens the lazy-loading detail modal when a card is selected. Optionally shows a
"create sanction" affordance.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `sanctions` | `Array<object>` | yes | — | The sanctions. |
| `labels` | `object` | yes | — | List copy. |
| `detailLabels` | `object` | yes | — | Detail-modal copy. |
| `lang` | `string` | yes | — | Locale. |
| `canCreate` | `boolean` | no | — | Shows the create affordance. |
| `createButtonLabel` | `string` | no | — | Create button text. |
| `createHref` | `string` | no | — | Create destination. |

## Composition

- **Uses:** `molecules/mobility/SanctionDetailModal`, `atoms/core/Icon`, `useState`.
- **Used by:** the `/mobility/sanctions` page (staff).

## Internationalisation

Copy via `labels` / `detailLabels`.

## Accessibility

Cards are labelled buttons opening the accessible detail modal.

## Related

`molecules/mobility/SanctionDetailModal`, `organisms/mobility/MySanctionsList`,
`organisms/mobility/CreateSanctionForm`.
