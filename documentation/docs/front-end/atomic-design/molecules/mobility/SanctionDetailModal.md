---
title: SanctionDetailModal
sidebar_label: SanctionDetailModal
sidebar_position: 7
---

# SanctionDetailModal

`packages/mobility/components/molecules/SanctionDetailModal.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

A modal that displays the full detail of a sanction, including its base64 evidence
image. The image payload is requested **lazily** through `fetchDetail` so the listing
endpoint can stay lightweight.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Visibility. |
| `sanctionId` | `number \| string \| null` | yes | — | The sanction to load. |
| `fetchDetail` | `(id) => Promise<{ data } \| { error: true }>` | yes | — | Lazy detail loader (returns the sanction incl. `imageBase64`). |
| `labels` | `object` | yes | — | Modal copy. |
| `lang` | `string` | yes | — | Locale for date formatting. |
| `onClose` | `() => void` | yes | — | Close handler. |

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/Icon`, the sanction-detail loader.
- **Used by:** `organisms/mobility/MySanctionsList`,
  `organisms/mobility/SanctionsManageList`.

## Internationalisation

All copy via `labels`.

## Accessibility

The accessible `core/Modal`; loading/error states are clearly shown; the evidence
image has descriptive alt.

## Related

`organisms/mobility/MySanctionsList`, `organisms/mobility/SanctionsManageList`,
`molecules/mobility/EvidencePicker`.
