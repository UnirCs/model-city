---
title: CreateAgentModal
sidebar_label: CreateAgentModal
sidebar_position: 6
---

# CreateAgentModal

`packages/core/components/molecules/CreateAgentModal.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A modal form for inviting a new municipal agent. The admin picks a role, enters a
name and email, and submits; the component calls the `onSubmit` server action with
`{ role, name, email }` and then shows a success or error banner. Role options are
Administrative (`BACKOFFICE`), Field (`OPERATOR`) and Mobility (`MOBILITY_AGENT`).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Visibility. |
| `onSubmit` | `(payload) => Promise<{ ok: true } \| { error, status?, detail? }>` | yes | — | Invitation server action. |
| `onClose` | `() => void` | yes | — | Close handler (also resets the form). |
| `labels` | `object` | yes | — | Localized copy (title, role labels/descriptions, field labels/placeholders, submit/cancel/close, validation and per-status error messages). |

## Behaviour

Client-side validation checks the role, non-empty name, and an email regex. On
submit it disables the form, and maps the HTTP status of a failure to an i18n key
(`errorNetwork`/`errorBadRequest`/`errorForbidden`/`errorConflict`/`errorServer`).
Success and error results are announced via `useAnnounce` (`polite` / `assertive`).

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/FormField`, `atoms/core/Button`,
  `atoms/core/Icon`, `useAnnounce` (`core/lib/a11y/AnnouncerProvider`).
- **Used by:** `organisms/core/AddWorkerButton`.

## Internationalisation

All copy via `labels`; live-region announcements use the same strings.

## Accessibility

The role picker is a `role="radiogroup"` of `role="radio"` buttons with
`aria-checked`; results are announced through the announcer; error text uses
`role="alert"`.

## Styling & tokens

`bg-primary`/`bg-secondary`/`bg-tertiary` active role tiles, `bg-error-container`
error banner, green success banner, `rounded-md`.

## Usage

```jsx
<CreateAgentModal open={open} onSubmit={inviteAgent} onClose={close} labels={t.admin.createAgent} />
```

## Related

`organisms/core/AddWorkerButton`, `molecules/core/Modal`.
