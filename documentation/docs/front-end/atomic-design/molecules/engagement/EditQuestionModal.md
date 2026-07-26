---
title: EditQuestionModal
sidebar_label: EditQuestionModal
sidebar_position: 5
---

# EditQuestionModal

`packages/engagement/components/molecules/EditQuestionModal.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

A modal form for admin/backoffice users to edit a **future** civic consultation. It
submits via the `updateQuestion` server action. Text fields (title, description,
objectives) are multilingual, and the location is chosen by zone → neighbourhood.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `question` | `object` | yes | — | The consultation being edited (with `translations`, `objectives`, `neighbourhoodId`). |
| `t` | `object` | yes | — | Localized form copy. |
| `onClose` | `() => void` | yes | — | Close handler. |

## Behaviour

Initialises multilingual `{ es, en, fr }` fields from `question.translations`, sorts
objectives by `sortOrder`, and resolves the initial zone from the neighbourhoods
config. Submits `updateQuestion` and refreshes.

## Composition

- **Uses:** `molecules/core/Modal`, `MultilingualFields`, `atoms/core/FormField`,
  `atoms/core/Button`, the neighbourhoods config, `updateQuestion` action.
- **Used by:** `molecules/engagement/EditQuestionButton`.

## Internationalisation

Translatable fields via `MultilingualFields`; labels via `t`.

## Accessibility

The accessible `core/Modal`; fields via `FormField`.

## Related

`molecules/engagement/EditQuestionButton`, `organisms/engagement/CreateQuestionForm`,
`molecules/core/MultilingualFields`.
