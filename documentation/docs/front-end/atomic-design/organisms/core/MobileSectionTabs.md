---
title: MobileSectionTabs
sidebar_label: MobileSectionTabs
sidebar_position: 8
---

# MobileSectionTabs

`packages/core/components/organisms/MobileSectionTabs.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component (async)

## Purpose

The server wrapper that resolves the role-gated nav sections on the server and
renders the client [`SectionTabsBar`](../../molecules/core/SectionTabsBar.md), which
shows sub-section tabs on mobile when the user is inside a multi-item section. It
sits between `TopNavBar` and the main content in `AppShell` so the tabs stick below
the top bar.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Active locale. |

## Behaviour

`await`s `getDictionary(lang)` and `auth0.getSession()` in parallel and passes
`buildNavSections({ session, n: dict.nav })` to `SectionTabsBar`.

## Composition

- **Uses:** `getDictionary`, `auth0`, `buildNavSections`,
  `molecules/core/SectionTabsBar`.
- **Used by:** `templates/core/AppShell`.

## Internationalisation

Reads `dict.nav` for the section labels.

## Accessibility

Delegates to `SectionTabsBar` (labelled `<nav>`, `aria-current` on the active pill).

## Related

`molecules/core/SectionTabsBar`, `organisms/core/MobileNav`,
`templates/core/AppShell`.
