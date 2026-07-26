---
title: PaginationBar
sidebar_label: PaginationBar
sidebar_position: 5
---

# PaginationBar

`packages/mobility/components/molecules/PaginationBar.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

A generic prev/next pager that mutates the `page` query parameter in the current URL
without touching the rest of the params. Hidden when there is a single page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | `number` | yes | — | Zero-based current page. |
| `totalPages` | `number` | yes | — | Total pages (returns `null` if ≤ 1). |
| `labels` | `{ page, of, prev, next }` | yes | — | Localized pager copy. |

## Behaviour

`goTo` clones the current `URLSearchParams`, sets/deletes `page`, and `router.push`es
the same pathname — preserving any active filters.

## Composition

- **Uses:** `atoms/core/Icon`, `useRouter`/`useSearchParams`/`usePathname`.
- **Used by:** the staff mobility tables/lists (`TicketsTable`, `SanctionsManageList`).

## Internationalisation

Copy via `labels`.

## Accessibility

`<nav aria-label="Pagination">`; disabled ends use `disabled` + reduced opacity;
prev/next words hidden below `sm`.

## Related

`molecules/core/SectionPager`, `molecules/core/LocalizedPager`,
`molecules/mobility/MobilityFilters`.
