---
title: PageHeader
sidebar_label: PageHeader
sidebar_position: 22
---

# PageHeader

`packages/core/components/molecules/PageHeader.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

The standard list-page header: a filled section icon, the page `<h1>` and an
optional subtitle paragraph.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `icon` | `string` | yes | — | Material Symbols name (filled). |
| `title` | `string` | yes | — | Page `<h1>`. |
| `subtitle` | `string` | no | — | Secondary line under the title. |
| `iconClassName` | `string` | no | `'text-secondary'` | Icon colour classes. |
| `className` | `string` | no | `'mb-lg'` | Wrapper classes (controls bottom spacing). |

## Composition

- **Uses:** `atoms/core/Icon`.
- **Used by:** the list/browse organisms (events, spaces, routes, alerts,
  administration) as the top header.

## Internationalisation

None directly — `title`/`subtitle` supplied (localized) by the caller.

## Accessibility

Renders the single page `<h1>` inside a `<header>` landmark; the icon is decorative.

## Styling & tokens

`text-h2`, `text-primary`, `text-body-md`, `text-on-surface-variant`.

## Usage

```jsx
<PageHeader icon="event" title={t.events.title} subtitle={t.events.subtitle} />
```

## Related

`molecules/core/IconPageHeader` (compact detail-page variant).
