---
title: QuestionStatusActions
sidebar_label: QuestionStatusActions
sidebar_position: 6
---

# QuestionStatusActions

`packages/engagement/components/molecules/QuestionStatusActions.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

Admin/backoffice action buttons that change a consultation's status:
**Start survey** (future consultations only → `PATCH openDate=today`) and **Close
survey** (active consultations only → `PATCH closeDate=today`). Returns `null` when
neither action applies.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `questionId` | `string \| number` | yes | — | The consultation. |
| `status` | `'active' \| 'future' \| 'past'` | yes | — | Determines which action (if any) is shown. |
| `t` | `object` | yes | — | `participation.questionDetail.adminActions` dictionary. |

## Behaviour

Calls `openQuestion` / `closeQuestion` (server actions), showing a spinner while
pending and refreshing on success; errors are surfaced inline and announced.

## Composition

- **Uses:** `atoms/core/Icon`, `openQuestion`/`closeQuestion`
  (`engagement/lib/actions/questions`), `useAnnounce`, `useRouter`, `useState`.
- **Used by:** `organisms/engagement/QuestionDetailView`.

## Internationalisation

Copy via `t` (`startSurvey`, `closeSurvey`, `actionError`).

## Accessibility

Buttons carry a disabled/loading state; errors announced (`assertive`).

## Related

`organisms/engagement/QuestionDetailView`, `molecules/engagement/EditQuestionButton`.
