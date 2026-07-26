---
title: UserMenu
sidebar_label: UserMenu
sidebar_position: 33
---

# UserMenu

`packages/core/components/molecules/UserMenu.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

An avatar with a dropdown of user options (profile, sign out). It receives the
Auth0 `user` object as a prop from the parent Server Component
(`TopNavBar` → `auth0.getSession()`) and returns `null` when there is no user.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `user` | `{ name?, email?, picture?, given_name? } \| null` | yes | — | Auth0 user; `null` renders nothing. |

## Behaviour

Opens/closes on click and closes on outside click (a `mousedown` document
listener). Shows the name/email header, a **My profile** link (`LocalizedLink`) and
a **Sign out** anchor to `/auth/logout`.

## Composition

- **Uses:** `atoms/core/Avatar`, `atoms/core/Icon`, `LocalizedLink`,
  `useTranslations()`, `useState`/`useRef`/`useEffect`.
- **Used by:** `organisms/core/TopNavBar`.

## Internationalisation

Reads `dict.nav.userMenu`, `dict.auth.myProfile`, `dict.auth.logout`.

## Accessibility

Trigger has `aria-haspopup="menu"`, `aria-expanded` and an `aria-label`; the panel
is `role="menu"` with `role="menuitem"` entries.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, `shadow-lg`;
error-tone sign-out.

## Usage

```jsx
<UserMenu user={session?.user ?? null} />
```

## Related

`atoms/core/Avatar`, `molecules/core/LogoutButton`, `organisms/core/TopNavBar`.
