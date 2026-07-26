---
title: ServiceCard
sidebar_label: ServiceCard
sidebar_position: 25
---

# ServiceCard

`packages/core/components/molecules/ServiceCard.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A municipal-service card with an icon, title, description and link. On mobile it
uses smaller typography and tighter spacing. It is the building block of the home
service grid.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `id` | `string` | — | — | Service id (part of the service catalogue shape; not rendered directly). |
| `title` | `string` | yes | — | Card title. |
| `description` | `string` | yes | — | Card body text. |
| `icon` | `string` | yes | — | Material icon (filled). |
| `href` | `string` | yes | — | Destination (wrapped in a `next/link`). |

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`.
- **Used by:** `organisms/core/ServiceGrid` / `organisms/core/LandingServices`.

## Internationalisation

None directly — `title`/`description` supplied (localized) by the caller.

## Accessibility

The whole card is a link; the icon is decorative. Hover elevates the shadow and
tints the icon badge.

## Styling & tokens

`bg-surface-container-lowest`, responsive `text-body-md md:text-h3`,
`text-primary`, `bg-secondary-container/30`, `rounded-md`, colour-mix shadows.

## Usage

```jsx
<ServiceCard icon="event" title={t.services.events} description={t.services.eventsDesc} href="/events" />
```

## Related

`organisms/core/ServiceGrid`, `molecules/core/CatalogueCard`.
