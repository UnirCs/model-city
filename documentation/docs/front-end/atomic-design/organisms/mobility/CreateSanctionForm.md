---
title: CreateSanctionForm
sidebar_label: CreateSanctionForm
sidebar_position: 2
---

# CreateSanctionForm

`packages/mobility/components/organisms/CreateSanctionForm.js` · **Tier:** organism · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

The two-column form to issue a new sanction: license plate + evidence photo on the
left, map picker on the right. The operator can be deep-linked from the tickets table
with the plate / coordinates pre-filled.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `labels` | `object` | yes | — | Form copy. |
| `lang` | `string` | yes | — | Locale. |
| `initial` | `{ licensePlate?, latitude?, longitude? }` | no | `{}` | Pre-filled values (deep-link from the tickets table). |

## Composition

- **Uses:** `atoms/mobility/LicensePlateInput`, `molecules/mobility/EvidencePicker`,
  `molecules/core/LocationPickerMapClient`, `atoms/core/FormField`/`Button`, the
  sanction action, `useLocalizedBack`.
- **Used by:** the `/mobility/sanctions/new` page.

## Internationalisation

All copy via `labels`.

## Accessibility

Fields via `FormField`; plate + evidence + map each carry their own a11y.

## Related

`molecules/mobility/EvidencePicker`, `atoms/mobility/LicensePlateInput`,
`organisms/mobility/TicketsTable`.
