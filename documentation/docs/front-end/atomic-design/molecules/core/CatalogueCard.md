---
title: CatalogueCard
sidebar_label: CatalogueCard
sidebar_position: 3
---

# CatalogueCard

`packages/core/components/molecules/CatalogueCard.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

The generic listing card used across all catalogue sections (events, sports spaces,
tourist routes, locations, consultations). It is entirely service-agnostic: the
caller composes the metadata it needs via `extras`. Layout: cover image (with
optional fallback icon), a 2-line-clamped title, an extras row of `{icon, value}`
items joined by mid-dots, and a fused "view details" button. An optional secondary
action (e.g. QR) renders a second fused button.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `href` | `string` | yes | — | Detail link. |
| `imageUrl` | `string \| null` | no | — | Cover image; falls back to `fallbackIcon`. |
| `imageAlt` | `string` | yes | — | Alt text for the cover. |
| `fallbackIcon` | `string` | no | `'category'` | Icon shown when there is no image. |
| `title` | `string` | yes | — | Card title (2-line clamp). |
| `extras` | `Array<{ icon, value }>` | no | `[]` | Metadata chips rendered as one caption line. |
| `viewDetailsLabel` | `string` | yes | — | Primary button label. |
| `grayscale` | `boolean` | no | `false` | Renders the cover in grayscale. |
| `secondaryLabel` | `string` | no | — | Enables a second fused action when paired with `onSecondaryAction`. |
| `onSecondaryAction` | `() => void` | no | — | Secondary action handler (renders a QR icon button). |

## Variants & states

Two layouts: **single action** (whole card is a link) and **dual action** (link
body + primary link + secondary button, e.g. "view" + "QR").

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`.
- **Used by:** the list organisms across leisure/engagement (events, spaces, routes,
  locations, consultations).

## Internationalisation

All text via props (`title`, `viewDetailsLabel`, `secondaryLabel`, `extras[].value`).

## Accessibility

Cover images are lazy-loaded with `alt`; separators are `aria-hidden`.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, hover
elevation/translate; `bg-primary`/`text-on-primary` primary action.

## Usage

```jsx
<CatalogueCard href={`/${lang}/events/${e.id}`} imageUrl={e.image} imageAlt={e.title}
  title={e.title} extras={[{ icon: 'event', value: date }]} viewDetailsLabel={t.common.view} />
```

## Related

`molecules/core/ServiceCard`, `molecules/core/UserMiniCard`,
`molecules/leisure/CreateCard`.
