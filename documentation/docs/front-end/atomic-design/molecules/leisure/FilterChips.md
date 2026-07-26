---
title: FilterChips
sidebar_label: FilterChips
sidebar_position: 6
---

# FilterChips

`packages/leisure/components/molecules/FilterChips.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Server Component

## Purpose

A generic chip-based filter. It renders either as **links** (with `hrefBuilder`) or
as **buttons** (with `onSelect`), highlighting the active value. Used across event
and location filters.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `options` | `Array<{ value: string \| null, label, icon? }>` | yes | — | The chip options. |
| `activeValue` | `string \| null` | yes | — | The currently selected value. |
| `onSelect` | `(value) => void` | no | — | Button-mode click handler. |
| `hrefBuilder` | `(value) => string` | no | — | Link-mode href builder (takes precedence, making chips links). |
| `ariaLabel` | `string` | no | — | Group label. |

## Variants & states

**link** (SSR filter navigation, `scroll={false}`) vs **button** (client callback);
active chip uses the primary colour pair.

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`.
- **Used by:** `organisms/leisure/EventFilters`, `organisms/leisure/LocationFilters`.

## Internationalisation

Option labels supplied (localized) by the caller.

## Accessibility

`role="group"` with `aria-pressed` on each chip.

## Styling & tokens

`bg-primary`/`text-on-primary` active, `bg-surface-container-low` idle, `rounded-md`.

## Related

`organisms/leisure/EventFilters`, `organisms/leisure/LocationFilters`,
`molecules/leisure/FilterSection`.
