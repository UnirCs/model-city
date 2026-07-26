---
title: FilterSection
sidebar_label: FilterSection
sidebar_position: 7
---

# FilterSection

`packages/leisure/components/molecules/FilterSection.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Server Component

## Purpose

A reusable visual container for filter sections: a rounded, bordered panel with an
icon + title header and a content slot for chips/controls.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `title` | `string` | yes | — | Section heading. |
| `icon` | `string` | no | `'filter_list'` | Header icon. |
| `children` | `ReactNode` | yes | — | The filter controls (e.g. `FilterChips`). |
| `ariaLabel` | `string` | no | `'filter'` | `aria-label` of the `<nav>`. |
| `className` | `string` | no | `''` | Extra classes. |

## Composition

- **Uses:** `atoms/core/Icon`.
- **Used by:** `organisms/leisure/EventFilters`, `organisms/leisure/LocationFilters`.

## Internationalisation

`title` supplied (localized) by the caller.

## Accessibility

Renders a labelled `<nav>` with an `<h2>` heading.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, `shadow-sm`.

## Related

`molecules/leisure/FilterChips`, `molecules/core/AdminUserFilters`.
