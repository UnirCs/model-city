---
title: SystemTrailFilters
sidebar_label: SystemTrailFilters
sidebar_position: 30
---

# SystemTrailFilters

`packages/core/components/molecules/SystemTrailFilters.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The URL-param filter bar for the system-trail (audit) viewer — the standalone
"Registros" page and the per-user activity panel. The admin picks **one module at a
time** (only enabled modules are listed); the event-type select is scoped to that
module and cleared whenever the module changes. It also offers an optional date
range and, in the standalone view only, a responsible-user input.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `labels` | `object` | yes | — | Localized copy (`filtersTitle`, `moduleLabel`, `modules`, `eventTypeLabel`/`Any`/`Labels`, `responsibleLabel`/`Placeholder`, `fromLabel`, `toLabel`, `apply`, `reset`). |
| `showResponsible` | `boolean` | no | `false` | Shows the responsible-user input (standalone Registros view). |
| `initial` | `{ module?, eventType?, responsibleUserId?, from?, to? }` | yes | — | Initial values (re-synced from the URL on back/forward). |

## Behaviour

Modules come from `availableTrailModules()`; event types from
`trailModuleById(module).eventTypes`. Changing the module resets the event type.
Date inputs hold `yyyy-mm-dd` (the server page snaps them to ISO bounds). Submitting
resets `page` to 0.

## Composition

- **Uses:** `atoms/core/Button`, `atoms/core/FormField`, `atoms/core/Icon`,
  `availableTrailModules`/`trailModuleById` (`core/lib/config/systemTrails`),
  `useRouter`/`useSearchParams`/`usePathname`.
- **Used by:** `organisms/core/SystemTrailList`, `organisms/core/UserActivityPanel`.

## Internationalisation

All copy via `labels`; module/event-type option text via `labels.modules` /
`labels.eventTypeLabels`.

## Accessibility

Each control is wrapped in `FormField`; selects use a custom chevron with
`appearance-none`.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, responsive
grid.

## Usage

```jsx
<SystemTrailFilters labels={t.systemTrails.filters} initial={initial} showResponsible />
```

## Related

`molecules/core/SystemTrailRow`, `organisms/core/SystemTrailList`,
`organisms/core/UserActivityPanel`.
