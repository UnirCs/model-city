---
title: EventFilters
sidebar_label: EventFilters
sidebar_position: 4
---

# EventFilters

`packages/leisure/components/organisms/EventFilters.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

The event type + paid/free filter controls for the events list. Each chip links to
the events list with the corresponding query string, preserving the other active
filter.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | For building hrefs. |
| `tFilters` | `object` | yes | — | `leisure.eventFilters` dictionary. |
| `typeLabels` | `object` | yes | — | `leisure.eventTypes` labels. |
| `eventTypes` | `string[]` | yes | — | Available event-type codes. |
| `eventType` | `string \| null` | yes | — | Active type filter. |
| `paid` | `boolean \| null` | yes | — | Active paid filter. |

## Composition

- **Uses:** `molecules/leisure/FilterSection`, `molecules/leisure/FilterChips`.
- **Used by:** the events list page (alongside `EventsList`).

## Internationalisation

Copy from `tFilters`; type labels from `typeLabels`.

## Accessibility

Delegates to `FilterSection` (labelled `<nav>`) and `FilterChips` (`aria-pressed`).

## Related

`organisms/leisure/LocationFilters`, `organisms/leisure/EventsList`,
`molecules/leisure/FilterChips`.
