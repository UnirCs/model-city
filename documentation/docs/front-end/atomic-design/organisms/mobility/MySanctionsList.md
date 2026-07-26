---
title: MySanctionsList
sidebar_label: MySanctionsList
sidebar_position: 3
---

# MySanctionsList

`packages/mobility/components/organisms/MySanctionsList.js` · **Tier:** organism · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

The citizen-facing list of received sanctions. Tapping a row opens a modal that
lazily fetches the base64 evidence image through a server action.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `sanctions` | `Array<{ id, licensePlate, latitude, longitude, agentSub?, createdAt }>` | yes | — | The user's sanctions. |
| `labels` | `object` | yes | — | List copy. |
| `detailLabels` | `object` | yes | — | Detail-modal copy. |
| `lang` | `string` | yes | — | Locale. |

## Composition

- **Uses:** `molecules/mobility/SanctionDetailModal`, `atoms/core/Icon`, `useState`.
- **Used by:** the `/mobility/my-sanctions` page.

## Internationalisation

Copy via `labels` / `detailLabels`.

## Accessibility

Rows are labelled buttons opening the accessible detail modal.

## Related

`molecules/mobility/SanctionDetailModal`, `organisms/mobility/SanctionsManageList`.
