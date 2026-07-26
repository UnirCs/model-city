---
title: FetchErrorBanner
sidebar_label: FetchErrorBanner
sidebar_position: 8
---

# FetchErrorBanner

`packages/core/components/molecules/FetchErrorBanner.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A card-framed error banner shown on list pages when a server fetch fails. Can also
render "bare" (just the coloured banner) for embedding inside an existing
card/section.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `message` | `string` | yes | — | User-facing error text. |
| `className` | `string` | no | `''` | Appended to the outer card wrapper for per-page spacing. |
| `bare` | `boolean` | no | `false` | When `true`, renders only the coloured banner without the surrounding card. |

## Variants & states

Two shapes: **card** (default — banner inside a `surface-container-lowest` card) and
**bare** (banner only).

## Composition

- **Uses:** `atoms/core/Icon` (`error_outline`).
- **Used by:** list/browse organisms when their server fetch returns an error
  sentinel (see the [Data & API](../../../architecture/data-and-api.md) error
  pattern).

## Internationalisation

None directly — `message` is supplied (localized) by the caller.

## Accessibility

Error-container colour pair for contrast; icon is decorative (`aria-hidden` from
`Icon`), the message text carries the meaning.

## Styling & tokens

`bg-error-container`, `text-on-error-container`, `bg-surface-container-lowest`,
`border-outline-variant`, `rounded-md`.

## Usage

```jsx
<FetchErrorBanner message={t.errors.loadFailed} className="mb-lg" />
<FetchErrorBanner message={t.errors.loadFailed} bare />
```

## Related

`molecules/core/EmptyState`, `molecules/core/InlineStatus`.
