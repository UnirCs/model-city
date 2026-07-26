---
title: SessionExpiredModal
sidebar_label: SessionExpiredModal
sidebar_position: 26
---

# SessionExpiredModal

`packages/core/components/molecules/SessionExpiredModal.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The modal shown when the user's session token has expired. It displays a message
and redirects to `/auth/logout` when the user confirms.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Whether the modal is visible. |
| `lang` | `string` | yes | — | Active locale (passed through; the modal reads copy from the translations context). |

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/Button`, `useTranslations()`.
- **Used by:** `templates/core/AppShell` — rendered when the `(app)` gate detects an
  expired token (see [Auth & roles](../../../architecture/auth-and-roles.md)).

## Internationalisation

Reads the `auth` dictionary slice (`sessionExpiredTitle`,
`sessionExpiredDescription`, `sessionExpiredButton`) with English fallbacks.

## Accessibility

Inherits the accessible dialog semantics of `molecules/core/Modal`. Confirming
navigates to logout via `window.location.href`.

## Usage

```jsx
<SessionExpiredModal open={tokenExpired} lang={lang} />
```

## Related

`molecules/core/Modal`, `templates/core/AppShell`,
[Auth & roles](../../../architecture/auth-and-roles.md).
