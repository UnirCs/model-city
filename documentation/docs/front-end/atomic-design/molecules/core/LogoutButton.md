---
title: LogoutButton
sidebar_label: LogoutButton
sidebar_position: 16
---

# LogoutButton

`packages/core/components/molecules/LogoutButton.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A link that closes the Auth0 session (invalidating both the Auth0 session and the
local cookie). It redirects to `/auth/logout` → Auth0 → the app landing.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `label` | `string` | no | `'Cerrar Sesión'` | Link text. |

## Composition

- **Uses:** `atoms/core/Icon` (`logout`); native `<a href="/auth/logout">`.
- **Used by:** `molecules/core/UserMenu`, the nav surfaces.

## Internationalisation

No dictionary access; the caller passes a localized `label`.

## Accessibility

Native anchor with error-tone styling and a decorative leading icon.

## Styling & tokens

`text-error`, `hover:bg-error-container/40`, `rounded-md`, `text-body-md`.

## Usage

```jsx
<LogoutButton label={t.auth.logout} />
```

## Related

`molecules/core/LoginButton`, `molecules/core/UserMenu`.
