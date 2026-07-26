---
title: EditQuestionButton
sidebar_label: EditQuestionButton
sidebar_position: 4
---

# EditQuestionButton

`packages/engagement/components/molecules/EditQuestionButton.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

Renders the "Edit consultation" button and conditionally mounts the
[`EditQuestionModal`](./EditQuestionModal.md). Kept as a separate client component so
the parent page can stay a server component. It only renders for future consultations
(the parent controls the visibility condition).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `question` | `object` | yes | — | The consultation to edit. |
| `t` | `object` | yes | — | Localized copy (forwarded to the modal). |
| `lang` | `string` | no | — | Locale (per the JSDoc signature). |

## Composition

- **Uses:** `atoms/core/Icon`, `molecules/engagement/EditQuestionModal`, `useState`.
- **Used by:** `organisms/engagement/QuestionDetailView` (future consultations).

## Internationalisation

`t.buttonLabel` (and the modal copy) supplied by the caller.

## Accessibility

A labelled button that mounts the accessible `EditQuestionModal` on demand.

## Related

`molecules/engagement/EditQuestionModal`, `organisms/engagement/QuestionDetailView`.
