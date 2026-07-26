---
title: EmptyState
sidebar_label: EmptyState
sidebar_position: 7
---

# EmptyState

`packages/core/components/molecules/EmptyState.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A reusable full-screen empty/error state with a centred card layout and a single
call-to-action link. Used for not-found pages, unauthorized views and other empty
states. It renders its own `<main id="main">` landmark, so it stands alone as a page
body.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `title` | `string` | yes | — | Heading (`<h1>`). |
| `subtitle` | `string` | no | — | Secondary line under the title. |
| `body` | `string` | yes | — | Explanatory paragraph. |
| `actionLabel` | `string` | yes | — | CTA link text. |
| `actionHref` | `string` | yes | — | CTA destination. |
| `actionIcon` | `string` | no | `'home'` | Leading icon on the CTA. |

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`.
- **Used by:** not-found / unauthorized surfaces (e.g. `organisms/core/UnauthorizedView`
  patterns) and module empty pages.

## Internationalisation

None directly — all strings supplied (localized) by the caller.

## Accessibility

Owns a focusable `<main id="main" tabIndex={-1}>` landmark (the skip-link target).
The CTA is a primary-styled link.

## Styling & tokens

`min-h-screen`, `bg-surface`, `bg-surface-container-lowest` card,
`bg-primary`/`text-on-primary` CTA, `rounded-md`, `shadow-md`.

## Usage

```jsx
<EmptyState
  title={t.notFound.title}
  body={t.notFound.body}
  actionLabel={t.common.goHome}
  actionHref={`/${lang}/home`}
/>
```

## Related

`molecules/core/InlineStatus` (inline variant), `organisms/core/UnauthorizedView`.
