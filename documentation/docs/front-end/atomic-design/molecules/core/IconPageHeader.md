---
title: IconPageHeader
sidebar_label: IconPageHeader
sidebar_position: 9
---

# IconPageHeader

`packages/core/components/molecules/IconPageHeader.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A compact page header with a rounded icon badge, an `<h1>` title and an optional
subtitle. Used on detail/sub pages (e.g. my-tickets, event tickets) where a back
button precedes the header.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `icon` | `string` | yes | — | Material Symbols name (filled). |
| `title` | `string` | yes | — | Page `<h1>`. |
| `subtitle` | `string` | no | — | Secondary line under the title. |
| `iconWrapClassName` | `string` | no | `'bg-primary/10'` | Badge background classes. |
| `iconClassName` | `string` | no | `'text-primary'` | Icon colour classes. |

## Composition

- **Uses:** `atoms/core/Icon`.
- **Used by:** detail/sub pages that already render a `BackButton` above the header.

## Internationalisation

None directly — `title`/`subtitle` supplied (localized) by the caller.

## Accessibility

Renders a single page `<h1>`; the badge icon is decorative.

## Styling & tokens

`rounded-md` badge, `text-h2`, `text-primary`, `text-body-sm`,
`text-on-surface-variant`.

## Usage

```jsx
<IconPageHeader icon="confirmation_number" title={t.tickets.myTickets} />
```

## Related

`molecules/core/PageHeader` (list-page variant).
