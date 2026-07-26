---
title: AppShell
sidebar_label: AppShell
sidebar_position: 1
---

# AppShell

`packages/core/components/templates/AppShell.js` · **Tier:** template · **Module:** core · **Rendering:** Server Component

## Purpose

The base layout for authenticated pages. It composes the responsive navigation
chrome around the page content and renders the `<main id="main">` skip-link
landmark, a decorative page backdrop, and the session-expired modal. It is the only
template in the design system.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `children` | `ReactNode` | yes | — | The page content. |
| `lang` | `string` | yes | — | Active locale, forwarded to the nav organisms and the session modal. |
| `sessionExpired` | `boolean` | no | `false` | Shows the `SessionExpiredModal` when the token has expired. |

## Responsive composition

| Breakpoint | Chrome |
| --- | --- |
| Mobile (`<md`) | `TopNavBar` + `MobileSectionTabs` (inside multi-item sections) + content (`pb-20` to clear the bottom nav) + `MobileNav` (FAB) |
| Tablet (`md–lg`) | `TopNavBar` + `SideNavRail` (icon rail) + content |
| Desktop (`≥lg`) | `TopNavBar` + `SideNavBar` (full sidebar) + content |

## Composition

- **Uses:** `organisms/core/TopNavBar`, `SideNavBar`, `SideNavRail`, `MobileNav`,
  `MobileSectionTabs`; `atoms/core/ScrollToTop`;
  `molecules/core/SessionExpiredModal`.
- **Used by:** the `(app)` gate layout (`core/routes-app/layout.js`) — see
  [Auth & roles](../../../architecture/auth-and-roles.md).

## Internationalisation

Forwards `lang` to its children; the backdrop image comes from
`modelcity.config.js` (`backgroundImageUrl`, imported via `@modelcity/config`).

## Accessibility

Renders the focusable `<main id="main" tabIndex={-1}>` landmark that `SkipLink`
targets (SC 2.4.1). The backdrop is `aria-hidden`.

## Styling & tokens

`min-h-screen` flex column, `pt-16 md:pt-20` to clear the top bar,
`max-w-container-max` content column, low-opacity backdrop.

## Related

`organisms/core/TopNavBar`, `organisms/core/SideNavBar`,
`molecules/core/SessionExpiredModal`, [Sitemap & navigation](../../../architecture/sitemap.md).
