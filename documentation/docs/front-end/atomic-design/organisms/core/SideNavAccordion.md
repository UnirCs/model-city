---
title: SideNavAccordion
sidebar_label: SideNavAccordion
sidebar_position: 13
---

# SideNavAccordion

`packages/core/components/organisms/SideNavAccordion.js` · **Tier:** organism · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A client wrapper that renders the sidebar `NavGroup`s with **mutual-exclusion**
accordion behaviour: opening one group collapses the others. It auto-expands the
group whose `sectionPath` (or any `sectionPaths`) matches the current URL and keeps
it open while navigation stays inside that section.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `groups` | `Array<{ sectionPath, sectionPaths?, icon, label, items }>` | yes | — | The nav sections to render as accordion groups. |

## Behaviour

Computes the active section from the pathname, holds a single `openSection` in
state, and passes controlled `isOpen`/`isActive`/`onToggle` to each `NavGroup`.

## Composition

- **Uses:** `molecules/core/NavGroup`, `usePathname`, `useTranslations()` (`lang`).
- **Used by:** `organisms/core/SideNavBar`.

## Internationalisation

Reads `lang` from context; group labels come from the nav model.

## Accessibility

Delegates the accordion semantics to `NavGroup` (`aria-expanded`, `aria-current`).

## Related

`molecules/core/NavGroup`, `organisms/core/SideNavBar`.
