---
title: InlineStatus
sidebar_label: InlineStatus
sidebar_position: 10
---

# InlineStatus

`packages/core/components/molecules/InlineStatus.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A centred icon + message block used for in-page load-error and not-found states on
detail/sub pages (as opposed to the full-screen [`EmptyState`](./EmptyState.md)).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `icon` | `string` | yes | — | Material icon name (decorative). |
| `message` | `string` | yes | — | User-facing status text. |
| `tone` | `'error' \| 'muted'` | no | `'muted'` | Icon colour: `error` → `text-error`; `muted` → `text-outline`. |

## Composition

- **Uses:** `atoms/core/Icon`.
- **Used by:** detail/sub views for inline empty/error states.

## Internationalisation

None directly — `message` is supplied (localized) by the caller.

## Accessibility

The icon renders at 48px and is decorative (`aria-hidden`); the message paragraph
carries the meaning.

## Styling & tokens

`text-body-lg`, `text-on-surface-variant`, `text-error` / `text-outline` with
reduced opacity.

## Usage

```jsx
<InlineStatus icon="search_off" message={t.errors.notFound} />
<InlineStatus icon="error" message={t.errors.loadFailed} tone="error" />
```

## Related

`molecules/core/EmptyState` (full-screen variant), `molecules/core/FetchErrorBanner`.
