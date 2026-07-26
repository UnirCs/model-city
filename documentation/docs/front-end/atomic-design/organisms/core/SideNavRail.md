---
title: SideNavRail
sidebar_label: SideNavRail
sidebar_position: 15
---

# SideNavRail

`packages/core/components/organisms/SideNavRail.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component (async)

## Purpose

A narrow icon-only sidebar shown on tablet (`md–lg`). Each icon links directly to
the section root (`rootHref` from the nav model) and exposes the section label as a
native tooltip (`title`) plus `aria-label`. No accordion, no sub-items — each
section's landing page provides the rest of the navigation. Hidden on mobile
(replaced by the bottom nav) and desktop (replaced by the full `SideNavBar`).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Active locale. |

## Behaviour

`await`s `getDictionary(lang)` + `auth0.getSession()`, builds the sections, renders
a Home icon, one icon per section root, and a logout icon pinned to the bottom.

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`, `getDictionary`, `auth0`,
  `buildNavSections`.
- **Used by:** `templates/core/AppShell` (tablet).

## Internationalisation

Reads `dict.nav` + `dict.auth.logout`; each icon's `aria-label` is the section
label.

## Accessibility

`<aside aria-label>`; every icon link carries an `aria-label`.

## Related

`organisms/core/SideNavBar` (desktop), `organisms/core/BottomNavBar` (mobile),
`templates/core/AppShell`.
