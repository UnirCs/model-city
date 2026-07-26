---
title: EvidencePicker
sidebar_label: EvidencePicker
sidebar_position: 3
---

# EvidencePicker

`packages/mobility/components/molecules/EvidencePicker.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

Lets the operator attach a photo from their device — either by picking from disk or by
opening the rear camera (when supported) — and exposes the resulting base64 payload
through `onChange`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `string \| null` | yes | — | Current image (base64) or `null`. |
| `onChange` | `(next: { base64, mimeType, sizeBytes } \| null) => void` | yes | — | Emits the picked/captured image (or `null` when removed). |
| `labels` | `{ pick, capture, replace, remove, tooLarge }` | yes | — | Localized control copy. |
| `error` | `string \| null` | no | — | Inline error message. |

## Behaviour

Two hidden inputs (file + camera via `capture`) feed the reader; oversized files
surface `labels.tooLarge`. A preview shows the current image with replace/remove
actions.

## Composition

- **Uses:** `atoms/core/Icon`/`Button`, hidden `<input type="file">`, `useRef`.
- **Used by:** `organisms/mobility/CreateSanctionForm`.

## Internationalisation

All copy via `labels`.

## Accessibility

Labelled pick/capture/replace/remove buttons; the preview has descriptive alt/labels.

## Related

`organisms/mobility/CreateSanctionForm`, `molecules/mobility/SanctionDetailModal`.
