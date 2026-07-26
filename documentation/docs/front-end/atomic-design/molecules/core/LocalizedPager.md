---
title: LocalizedPager
sidebar_label: LocalizedPager
sidebar_position: 12
---

# LocalizedPager

`packages/core/components/molecules/LocalizedPager.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A previous / page-indicator / next pager built on `LocalizedLink` (so the `lang`
prefix is added automatically and client-side navigation keeps the default scroll
behaviour). Targets default to `${basePath}?page=N`, or use a custom `hrefBuilder`
for pages with several paginated sections. Returns `null` when there is a single
page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `basePath` | `string` | no* | — | Lang-relative path, e.g. `/mobility/cars`. Used to build `?page=N` hrefs. |
| `hrefBuilder` | `(pageNumber: number) => string` | no* | — | Overrides `basePath` for multi-section pages. |
| `page` | `number` | yes | — | Zero-based current page. |
| `totalPages` | `number` | yes | — | Total page count (returns `null` if ≤ 1). |
| `labels` | `{ page, of, prev, next }` | yes | — | Localized pager copy. |
| `ariaLabel` | `string` | no | `'Pagination'` | `aria-label` of the `<nav>`. |
| `className` | `string` | no | `'mt-md'` | Extra `<nav>` classes. |

*Provide either `basePath` or `hrefBuilder`.

## Composition

- **Uses:** `LocalizedLink` (`core/lib/i18n/LocalizedLink`), `atoms/core/Icon`.
- **Used by:** list organisms that paginate lang-relative paths (e.g. mobility
  cars, admin lists).

## Internationalisation

Uses `LocalizedLink`, which prepends `/{lang}` automatically; copy comes from
`labels`.

## Accessibility

Same as `SectionPager`: `<nav aria-label>`, disabled ends as `<span>`s, prev/next
words hidden below `sm`.

## Styling & tokens

`grid grid-cols-3`, `border-outline-variant`, `rounded-md`.

## Usage

```jsx
<LocalizedPager basePath="/mobility/cars" page={page} totalPages={totalPages} labels={t.pager} />
```

## Related

`molecules/core/SectionPager` (plain-`Link` variant).
