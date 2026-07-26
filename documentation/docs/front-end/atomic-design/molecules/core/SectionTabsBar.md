---
title: SectionTabsBar
sidebar_label: SectionTabsBar
sidebar_position: 24
---

# SectionTabsBar

`packages/core/components/molecules/SectionTabsBar.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A sticky horizontal pill sub-navigation shown on **mobile** when the current URL is
inside a nav section that has more than one navigable sub-item. On every path change
the active pill auto-scrolls to the centre of the rail. Hidden on `lg+` (desktop /
tablet use the sidebar accordion) and returns `null` for a section with zero or one
item.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `sections` | `NavSection[]` | yes | — | The role-gated nav sections (from `core/lib/nav/sections`); the active one is matched against the pathname. |

## Behaviour

Finds the active section by matching `pathname` against its `sectionPath` /
`sectionPaths`, keeps its items with a valid `href`, and scrolls the
`aria-current="page"` pill into view via `scrollIntoView({ inline: 'center' })`.

## Composition

- **Uses:** `LocalizedLink`, `useTranslations()` (`lang`), `usePathname`, the
  Material Symbols font (inline for the pill icons).
- **Used by:** `organisms/core/MobileSectionTabs` (its server wrapper), inside
  `templates/core/AppShell`.

## Internationalisation

Item labels come from the nav model; reads `lang` from context to build hrefs.

## Accessibility

`<nav aria-label>`; the active pill carries `aria-current="page"`. The scrollbar is
hidden via the `scrollbar-none` utility.

## Styling & tokens

`sticky top-16`, `rounded-full` pills, `bg-secondary-container/60` active,
`bg-surface-container-low`, `border-outline-variant`.

## Usage

```jsx
<SectionTabsBar sections={navSections} />
```

## Related

`organisms/core/MobileSectionTabs` (server wrapper),
`organisms/core/BottomNavBar`, [Sitemap & navigation](../../../architecture/sitemap.md).
