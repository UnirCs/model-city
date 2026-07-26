---
title: LoginButton
sidebar_label: LoginButton
sidebar_position: 15
---

# LoginButton

`packages/core/components/molecules/LoginButton.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A link that initiates the OAuth 2.0 flow with Auth0. It navigates to
`/auth/login?returnTo=…` → Auth0 Universal Login. It is intentionally **not** a
Client Component — no loading state is needed because Auth0 controls the whole OAuth
navigation.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `className` | `string` | no | `''` | Extra classes appended last. |
| `label` | `string` | no | `'Acceder'` | Link text. |
| `iconName` | `string` | no | `'login'` | Leading Material icon. |
| `returnTo` | `string` | no | `'/home'` | Post-login destination (URL-encoded into the `returnTo` query param). |

## Composition

- **Uses:** `atoms/core/Icon`; native `<a href="/auth/login?returnTo=…">`.
- **Used by:** `organisms/core/LandingBanner` and the public landing surfaces.

## Internationalisation

No dictionary access; the caller passes a localized `label`. The Auth0 routes are
handled by the proxy (see [Auth & roles](../../../architecture/auth-and-roles.md)).

## Accessibility

Native anchor with a decorative leading icon and a visible label.

## Usage

```jsx
<LoginButton label={t.auth.login} returnTo="/home" className="button button--primary" />
```

## Related

`molecules/core/LogoutButton`, [Auth & roles](../../../architecture/auth-and-roles.md).
