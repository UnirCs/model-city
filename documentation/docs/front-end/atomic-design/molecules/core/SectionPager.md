---
title: SectionPager
sidebar_label: SectionPager
sidebar_position: 23
---

# SectionPager

`packages/core/components/molecules/SectionPager.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A server-rendered previous / page-indicator / next pager for SSR list pages.
Navigation is a plain `<Link scroll={false}>` whose target is produced by
`hrefBuilder(pageNumber)`, so the same component serves single-section pages and
multi-section pages (one pager per section, each with its own builder). Returns
`null` when there is a single page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `currentPage` | `number` | yes | — | Zero-based current page index. |
| `totalPages` | `number` | yes | — | Total page count (returns `null` if ≤ 1). |
| `hrefBuilder` | `(pageNumber: number) => string` | yes | — | Builds the href for a target page. |
| `labels` | `{ page, of, prev, next }` | yes | — | Localized pager copy. |
| `ariaLabel` | `string` | no | `'Pagination'` | `aria-label` of the `<nav>`. |
| `className` | `string` | no | `'mt-md'` | Extra `<nav>` classes (e.g. top spacing). |

## Composition

- **Uses:** `atoms/core/Icon` (`chevron_left`/`chevron_right`), `next/link`.
- **Used by:** SSR list organisms that paginate via URL params.

## Internationalisation

Copy is passed via `labels`; the plain `next/link` does not localize the href, so
callers pass locale-prefixed hrefs (unlike `LocalizedPager`).

## Accessibility

`<nav aria-label>`; disabled ends render as non-interactive `<span>`s with reduced
opacity; the prev/next words hide below the `sm` breakpoint.

## Styling & tokens

`grid grid-cols-3`, `border-outline-variant`, `text-on-surface-variant`, `rounded-md`.

## Usage

```jsx
<SectionPager
  currentPage={page}
  totalPages={totalPages}
  hrefBuilder={(n) => `/${lang}/events?page=${n}`}
  labels={t.pager}
/>
```

## Related

`molecules/core/LocalizedPager` (LocalizedLink variant),
`molecules/mobility/PaginationBar`.
