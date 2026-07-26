---
title: TopNavBar
sidebar_label: TopNavBar
sidebar_position: 17
---

# TopNavBar

`packages/core/components/organisms/TopNavBar.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component (async)

## Purpose

The authenticated top bar. It fetches the Auth0 session on the server and lays out
the brand (coat of arms + name), a Help link, the settings button, the theme toggle
and the user menu (desktop only — on mobile/tablet the profile lives in the bottom
FAB nav).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Active locale. |

## Behaviour

`await`s `auth0.getSession()` + `getDictionary(lang)`, deriving `user` and
`accessToken` (the latter passed to `SettingsButton` for the certificate check).

## Composition

- **Uses:** `molecules/core/ThemeToggle`, `molecules/core/UserMenu`,
  `molecules/core/SettingsButton`, `atoms/core/Icon`, `next/link` + `next/image`,
  `auth0`, `getDictionary`.
- **Used by:** `templates/core/AppShell` (all breakpoints).

## Internationalisation

Reads `dict.common.brand`, `dict.nav.help`.

## Accessibility

`<header>` landmark; the Help link and each action carry labels; the user menu is
hidden below `lg` (profile is reachable via the bottom nav).

## Styling & tokens

`fixed top-0`, `h-16 md:h-20`, `bg-surface/50 backdrop-blur-md`,
`border-outline-variant`.

## Related

`molecules/core/UserMenu`, `molecules/core/SettingsButton`,
`molecules/core/ThemeToggle`, `templates/core/AppShell`.
