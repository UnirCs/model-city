---
title: EventsList
sidebar_label: EventsList
sidebar_position: 7
---

# EventsList

`packages/leisure/components/organisms/EventsList.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component (async)

## Purpose

Fetches one page of events from the leisure microservice and renders the card grid
(with an optional staff "create" card), pagination, and the fetch-error / empty
states. Designed to be wrapped in `<Suspense>` so the filter controls render
immediately.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `page` | `number` | yes | — | Zero-based page index. |
| `eventType` | `string \| null` | yes | — | Type filter. |
| `paid` | `boolean \| null` | yes | — | Paid filter. |
| `accessToken` | `string \| undefined` | yes | — | Bearer token for the fetch. |
| `canManage` | `boolean` | yes | — | Shows the staff "create event" card. |
| `t` | `object` | yes | — | `leisure.events` dictionary. |
| `pageHref` | `(pageNumber) => string` | yes | — | Pager href builder. |

## Behaviour

`await getEvents({ page, eventType, paid }, accessToken)`; renders
`FetchErrorBanner` on failure and an empty state otherwise. Prices are formatted via
`formatPrice`; dates via `formatEventDateTime`.

## Composition

- **Uses:** `molecules/core/CatalogueCard`, `molecules/core/FetchErrorBanner`,
  `molecules/core/SectionPager`, `molecules/leisure/CreateCard`, `atoms/core/Icon`,
  `getEvents` + format utils (`leisure/lib`).
- **Used by:** the events list page.

## Internationalisation

All copy from the `leisure.events` dictionary.

## Accessibility

`<section aria-labelledby>` with an `<h2>`; empty/error states are clearly labelled.

## Related

`organisms/leisure/EventFilters`, `organisms/leisure/EventDetailView`,
`molecules/core/CatalogueCard`.
