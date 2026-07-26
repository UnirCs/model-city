---
title: NavGroup
sidebar_label: NavGroup
sidebar_position: 19
---

# NavGroup

`packages/core/components/molecules/NavGroup.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

An accordion-style sidebar navigation group for a service section. It works in two
modes: **uncontrolled** (manages its own open state, auto-expanding when the current
path is inside the section) or **controlled** (the parent owns `isOpen` + `onToggle`,
e.g. `SideNavAccordion` for mutual-exclusion behaviour).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `sectionPath` | `string` | yes | — | The section's primary path (used for active detection). |
| `icon` | `string` | yes | — | Section icon. |
| `label` | `string` | yes | — | Section label (the toggle button). |
| `items` | `Array<{ href: string \| null, label, icon? }>` | no | `[]` | Sub-items; `href: null` renders a disabled item. |
| `isOpen` | `boolean` | no | — | Controlled open state (with `onToggle`). |
| `isActive` | `boolean` | no | — | Parent-computed active flag (accounts for extra `sectionPaths`). |
| `onToggle` | `() => void` | no | — | Controlled toggle callback. |

## Behaviour

Highlights the header when active; auto-expands (uncontrolled) when the path is
inside the section. The sub-item with the **longest** matching href wins the active
state, so a shorter sibling isn't also highlighted. Uses a CSS `grid-template-rows`
`0fr → 1fr` trick for smooth expand.

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`, `usePathname`, `useTranslations()`.
- **Used by:** `organisms/core/SideNavBar`, `organisms/core/SideNavAccordion`,
  `organisms/core/MobileNav`.

## Internationalisation

Reads `lang` from context to build localized hrefs; labels come from the nav model.

## Accessibility

Toggle carries `aria-expanded`; the active sub-item carries `aria-current="page"`;
disabled items are non-interactive `<span>`s.

## Styling & tokens

`bg-secondary-container/40` active, `border-secondary-container` accent rail,
`rounded-md`, rotating chevron.

## Usage

```jsx
<NavGroup sectionPath="/tourism" icon="tour" label={t.nav.tourism} items={items} />
```

## Related

`molecules/core/NavItem`, `organisms/core/SideNavAccordion`,
`organisms/core/SideNavBar`.
