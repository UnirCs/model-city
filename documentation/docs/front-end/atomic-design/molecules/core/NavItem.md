---
title: NavItem
sidebar_label: NavItem
sidebar_position: 20
---

# NavItem

`packages/core/components/molecules/NavItem.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A single sidebar navigation item with an icon and label. It supports an **active**
state (detected via the current pathname) and a **danger** variant (e.g. logout),
and automatically prefixes the `href` with the current language.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `href` | `string` | yes | — | Locale-less path; prefixed with `/{lang}` internally. |
| `icon` | `string` | yes | — | Material icon (filled when active). |
| `label` | `string` | yes | — | Item text. |
| `danger` | `boolean` | no | `false` | Applies the error-tone (destructive) styling. |

## Variants & states

- **active** — when `pathname === /{lang}{href}` or starts with it: secondary-
  container background, semibold, filled icon, `aria-current="page"`.
- **danger** — error text + error-container hover.
- default — muted text, surface hover.

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`, `usePathname`, `useTranslations()` (for
  `lang`).
- **Used by:** `molecules/core/NavGroup`, the sidebar nav organisms.

## Internationalisation

Reads `lang` from the translations context to build the localized href. The `label`
is supplied by the caller.

## Accessibility

Sets `aria-current="page"` on the active item; the icon is decorative.

## Styling & tokens

`bg-secondary-container/40`, `text-secondary`, `text-error`, `rounded-md`,
`text-body-md`.

## Usage

```jsx
<NavItem href="/events" icon="event" label={t.nav.events} />
```

## Related

`molecules/core/NavGroup`, `organisms/core/SideNavBar`.
