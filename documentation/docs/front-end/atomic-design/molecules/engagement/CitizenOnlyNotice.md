---
title: CitizenOnlyNotice
sidebar_label: CitizenOnlyNotice
sidebar_position: 3
---

# CitizenOnlyNotice

`packages/engagement/components/molecules/CitizenOnlyNotice.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Server Component

## Purpose

Shown to staff users (admin, backoffice, operator) in place of actions that are
exclusively available to citizens (e.g. voting) — a friendly notice explaining why
the action is unavailable.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `{ title, message }` | yes | — | Localized notice copy. |

## Composition

- **Uses:** `atoms/core/Icon` (`badge`).
- **Used by:** `molecules/engagement/VotingZone` / the consultation detail view when a
  staff user views a citizen-only action.

## Internationalisation

Copy via `t` (supplied by the caller).

## Accessibility

Presentational card with an icon and clear message; no interactive elements.

## Styling & tokens

`bg-surface-container`, `bg-tertiary-container` icon badge, `border-outline-variant`,
`rounded-md`.

## Related

`molecules/engagement/VotingZone`, `organisms/engagement/QuestionDetailView`.
