---
title: SystemTrailList
sidebar_label: SystemTrailList
sidebar_position: 16
---

# SystemTrailList

`packages/core/components/organisms/SystemTrailList.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

Wraps the system-trail (audit) results in a single bordered container (used by the
Records page and the per-user activity panel). Events are stacked full width and
separated by dividers. It renders an error banner when the fetch failed and an empty
message when there are no matching events. An optional `title` is shown as the
container header (for the detail view, where "System events" is a sub-section).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `events` | `Array<object>` | yes | — | The trail events to render. |
| `lang` | `string` | yes | — | Locale for row timestamps. |
| `fetchError` | `boolean` | no | `false` | Renders the error banner instead of the list. |
| `title` | `string` | no | — | Optional container heading. |
| `titleIcon` | `string` | no | `'receipt_long'` | Icon for the heading. |
| `labels` | `object` | yes | — | Localized labels (forwarded to each `SystemTrailRow`, plus `loadError`/`empty`). |

## Composition

- **Uses:** `atoms/core/Icon`, `molecules/core/SystemTrailRow`.
- **Used by:** `organisms/core/UserActivityPanel`, the Records page.

## Internationalisation

All copy via `labels`.

## Accessibility

The error state uses `role="alert"`; the optional heading is an `<h2>`.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`,
`bg-error-container` error banner.

## Related

`molecules/core/SystemTrailRow`, `molecules/core/SystemTrailFilters`,
`organisms/core/UserActivityPanel`.
