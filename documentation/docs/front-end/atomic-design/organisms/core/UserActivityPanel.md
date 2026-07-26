---
title: UserActivityPanel
sidebar_label: UserActivityPanel
sidebar_position: 19
---

# UserActivityPanel

`packages/core/components/organisms/UserActivityPanel.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component (async)

## Purpose

The system-trail panel embedded in a citizen/worker detail view: a module selector
(one enabled module at a time), event-type and date-range filters, a full-width
event list and a paginator — all scoped to a fixed `responsibleUserId`. It
centralises module resolution, fetching and pager-href building so both detail pages
stay thin.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `userId` | `string` | yes | — | The fixed responsible user. |
| `basePath` | `string` | yes | — | Lang-relative base path for pager hrefs. |
| `sp` | `Record<string,string>` | yes | — | Resolved search params (`module`, `eventType`, `from`, `to`, `page`). |
| `accessToken` | `string` | yes | — | Bearer token for `listSystemTrails`. |
| `lang` | `string` | yes | — | Locale for row timestamps. |
| `sectionTitle` | `string` | no | — | Heading inside the list container. |
| `eventLabels` | `object` | yes | — | `admin.events` labels. |
| `pagerLabels` | `object` | yes | — | `admin.common` pager labels. |

## Behaviour

Resolves the module (falling back to `core`), snaps the date range to ISO bounds,
fetches `listSystemTrails(...)`, and composes `SystemTrailFilters` +
`SystemTrailList` + `LocalizedPager` with a shared href builder.

## Composition

- **Uses:** `molecules/core/SystemTrailFilters`, `organisms/core/SystemTrailList`,
  `molecules/core/LocalizedPager`, `listSystemTrails` + trail config, `dateToIso`.
- **Used by:** `organisms/core/UserDetailView` (as its events section).

## Internationalisation

Labels via `eventLabels` / `pagerLabels`.

## Accessibility

Delegates to its composed filter/list/pager molecules.

## Related

`organisms/core/UserDetailView`, `organisms/core/SystemTrailList`,
`molecules/core/SystemTrailFilters`.
