---
title: BottomNavBar
sidebar_label: BottomNavBar
sidebar_position: 2
---

# BottomNavBar

`packages/core/components/organisms/BottomNavBar.js` · **Tier:** organism · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The fixed bottom navigation for mobile (`<lg`): Home (left), a central FAB, and
Profile (right). The FAB toggles a floating **services sheet** listing the enabled
sections. The open/close animation is driven by a four-state machine
(`closed → opening → open → closing`) so both enter and exit transitions play via
CSS keyframes (more reliable than transition-class toggling on iOS Safari).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `sections` | `NavSection[]` | yes | — | The role-gated nav sections (from the server wrapper `MobileNav`). |
| `labels` | `object` | yes | — | Localized copy (`home`, `services`, `profile`, `servicesTitle`, `servicesClose`, `mobileNav`). |

## Behaviour

The sheet closes on navigation and on `Escape`. The FAB morphs to a red close
button while open (cross-fading `apps` ↔ `close` icons). Tapping a service pushes
its `rootHref` and closes the sheet.

## Composition

- **Uses:** `atoms/core/Icon`, `LocalizedLink`, `useTranslations()` (`lang`),
  `usePathname`/`useRouter`, `useId`.
- **Used by:** `organisms/core/MobileNav` (its server wrapper), inside `AppShell`.

## Internationalisation

All copy via `labels`; hrefs localized through `LocalizedLink` / `lang`.

## Accessibility

The services sheet is a `role="dialog" aria-modal` with a labelled title; the FAB
carries `aria-expanded`/`aria-haspopup="dialog"`/`aria-controls`; active tabs carry
`aria-current="page"`.

## Styling & tokens

`fixed bottom-0`, `backdrop-blur-md`, `bg-primary`/`bg-error` FAB, keyframe sheet
animations defined in `globals.css`.

## Related

`organisms/core/MobileNav`, `molecules/core/SectionTabsBar`,
`templates/core/AppShell`.
