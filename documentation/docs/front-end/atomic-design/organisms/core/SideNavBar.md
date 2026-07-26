---
title: SideNavBar
sidebar_label: SideNavBar
sidebar_position: 14
---

# SideNavBar

`packages/core/components/organisms/SideNavBar.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component (async)

## Purpose

The full-width sidebar navigation (desktop only, `≥lg`) — a 288 px sticky column
with a Home link plus the grouped-section accordion. Hidden on mobile (replaced by
the bottom nav) and on tablet (replaced by the narrow `SideNavRail`). It reads the
Auth0 session server-side so role-based gating happens once.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Active locale. |

## Behaviour

`await`s `getDictionary(lang)` + `auth0.getSession()`, builds
`buildNavSections({ session, n })`, renders a direct Home `NavItem` and passes the
sections to `SideNavAccordion`.

## Composition

- **Uses:** `molecules/core/NavItem`, `organisms/core/SideNavAccordion`,
  `getDictionary`, `auth0`, `buildNavSections`.
- **Used by:** `templates/core/AppShell` (desktop).

## Internationalisation

Reads `dict.nav` for labels; sections carry localized labels from the nav model.

## Accessibility

`<aside aria-label>` containing a `<nav aria-label>`.

## Styling & tokens

`hidden lg:flex`, `w-72`, `sticky top-20`, `bg-surface-container-low/50`,
`border-outline-variant`.

## Related

`organisms/core/SideNavRail` (tablet), `organisms/core/SideNavAccordion`,
`molecules/core/NavItem`, `templates/core/AppShell`.
