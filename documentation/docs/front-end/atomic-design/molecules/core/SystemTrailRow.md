---
title: SystemTrailRow
sidebar_label: SystemTrailRow
sidebar_position: 31
---

# SystemTrailRow

`packages/core/components/molecules/SystemTrailRow.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A collapsible card for the system-trail (audit) viewer. The always-visible header
shows the operation badge, the event-type name, the timestamp and a chevron toggle;
when expanded it shows the metadata (resource, responsible user, correlation id)
and the raw JSON payload when present. Height is animated with a `ResizeObserver`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `event` | `object` | yes | — | The trail event (`eventId`, `eventType`, `operationType`, `occurredAt`, `correlationId?`, `responsibleUserId?`, `resourceType?`, `resourceId?`, `payload?`). |
| `lang` | `string` | yes | — | Locale for `formatDateTime`. |
| `labels` | `object` | yes | — | Localized labels (`operationTypes`, `eventTypeLabels`, `colResource`, `colResponsible`, `colCorrelation`). |

## Behaviour

Maps `operationType` (`CREATE`/`UPDATE`/`DELETE`) to a badge colour, formats the
resource as `type #id`, and pretty-prints the payload with `JSON.stringify(…, 2)`.

## Composition

- **Uses:** `atoms/core/Icon`, `formatDateTime` (`core/lib/format`), `useState`/
  `useEffect`/`useRef`, `ResizeObserver`.
- **Used by:** `organisms/core/SystemTrailList`, `organisms/core/UserActivityPanel`.

## Internationalisation

Operation and event-type labels come from `labels`; the timestamp is localized via
`formatDateTime(event.occurredAt, lang)`.

## Accessibility

The header is a `<button aria-expanded>`; the metadata uses a `<dl>`; the payload is
a scrollable `<pre>`.

## Styling & tokens

`bg-surface-container`, `border-outline-variant`, `rounded-md`; operation badges use
secondary/tertiary/error container pairs; 300 ms height transition.

## Usage

```jsx
<SystemTrailRow event={event} lang={lang} labels={t.systemTrails} />
```

## Related

`organisms/core/SystemTrailList`, `molecules/core/SystemTrailFilters`.
