---
title: CollapsibleHeader
sidebar_label: CollapsibleHeader
sidebar_position: 5
---

# CollapsibleHeader

`packages/core/components/molecules/CollapsibleHeader.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A wrapper-less collapsible section header with a `+`/`−` toggle. It renders just
the header row and the conditional content — no surrounding container. Height
changes are animated using an explicit pixel height kept in sync with the content
via a `ResizeObserver` (so it animates correctly even when children change, e.g.
after pagination).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `icon` | `string` | yes | — | Material icon (filled) beside the title. |
| `iconClassName` | `string` | no | `'text-on-surface-variant'` | Icon colour classes. |
| `title` | `string` | yes | — | Section heading (`<h2>`). |
| `defaultOpen` | `boolean` | no | `false` | Initial open state. |
| `children` | `ReactNode` | yes | — | Collapsible content. |

## Composition

- **Uses:** `atoms/core/Icon` (`add`/`remove`), `useState`, `useEffect`, `useRef`,
  `ResizeObserver`.
- **Used by:** detail views that group optional/secondary content into collapsible
  sections.

## Internationalisation

None directly — `title` supplied (localized) by the caller.

## Accessibility

The toggle button carries `aria-expanded`; the content height animates from/to `0`
with `overflow: hidden`.

## Styling & tokens

`text-h3`, `text-primary`, `border-outline-variant`, `rounded-full` toggle,
300 ms height transition.

## Usage

```jsx
<CollapsibleHeader icon="history" title={t.section.history} defaultOpen>
  {/* content */}
</CollapsibleHeader>
```

## Related

`molecules/core/AdminSection`, `molecules/core/PageHeader`.
