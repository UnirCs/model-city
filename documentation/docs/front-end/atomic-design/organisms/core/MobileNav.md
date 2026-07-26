---
title: MobileNav
sidebar_label: MobileNav
sidebar_position: 7
---

# MobileNav

`packages/core/components/organisms/MobileNav.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component (async)

## Purpose

The server wrapper that resolves the role-gated nav sections on the server and hands
them to the client [`BottomNavBar`](./BottomNavBar.md) (which owns the FAB services
sheet). Only rendered for the `<md` breakpoint by `AppShell`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Active locale (used to fetch the dictionary + session). |

## Behaviour

`await`s `getDictionary(lang)` and `auth0.getSession()` in parallel, builds
`buildNavSections({ session, n })`, and passes the sections plus a `labels` bundle
to `BottomNavBar`.

## Composition

- **Uses:** `getDictionary`, `auth0`, `buildNavSections`,
  `organisms/core/BottomNavBar`.
- **Used by:** `templates/core/AppShell`.

## Internationalisation

Reads `dict.nav`, `dict.auth`, `dict.a11y` to build the labels for the bottom nav.

## Accessibility

Delegates to `BottomNavBar` (labelled nav + FAB sheet).

## Related

`organisms/core/BottomNavBar`, `organisms/core/MobileSectionTabs`,
`templates/core/AppShell`.
