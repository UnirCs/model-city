---
title: ProfileCard
sidebar_label: ProfileCard
sidebar_position: 10
---

# ProfileCard

`packages/core/components/organisms/ProfileCard.js` · **Tier:** organism · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The citizen's own profile surface with **inline-editable** fields. It shows an
avatar header and a personal-data list: name (editable text), email (read-only),
join date (read-only) and neighbourhood (editable select). If the backend profile is
unavailable it falls back to Auth0 data and shows a warning banner.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `authUser` | `{ name?, email?, picture? }` | yes | — | Auth0 user (fallback source). |
| `profile` | `{ name, email, createdAt, neighbourhood } \| null` | yes | — | Backend profile; `null` triggers the fallback + warning. |
| `dict` | `object` | yes | — | The `profile` dictionary (labels, save/cancel, errors). |
| `lang` | `string` | yes | — | Locale for the join-date formatting. |

## Behaviour

Uses internal `EditableField` / `EditableSelectField` sub-components: hover reveals
an edit affordance; Enter saves, Escape cancels. The neighbourhood select maps a
chosen slug back to its display name from `core/lib/config/neighbourhoods`.

## Composition

- **Uses:** `atoms/core/Avatar`, `atoms/core/Icon`, the neighbourhoods config;
  internal `EditableField` / `EditableSelectField`.
- **Used by:** the profile page (`core/routes/profile`).

## Internationalisation

All copy from the `profile` dictionary; the join date via `toLocaleDateString(lang)`.

## Accessibility

Fields use a `<dl>`; edit buttons carry an `aria-label`; the backend-unavailable
warning uses the error-container colours.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`,
`divide-outline-variant`, error warning banner, mobile logout button.

## Related

`atoms/core/Avatar`, `organisms/core/UserDetailView` (admin read-only variant),
`organisms/core/RegistrationForm`.
