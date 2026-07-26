---
title: CarCard
sidebar_label: CarCard
sidebar_position: 2
---

# CarCard

`packages/mobility/components/molecules/CarCard.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Server Component

## Purpose

A compact, purely presentational card showing one of the user's registered cars: the
license plate, an optional nickname, a brand/model subtitle, and an optional
"added at" footer.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `car` | `{ id, licensePlate, nickname?, brand?, model?, createdAt? }` | yes | — | The registered car. |
| `addedAtLabel` | `string` | yes | — | Label for the added-at footer. |
| `formattedAddedAt` | `string` | no | — | Preformatted added-at date; when present, renders the footer. |

## Composition

- **Uses:** `atoms/core/Icon` (`directions_car`, `event_available`).
- **Used by:** `organisms/mobility/CarsPanel`.

## Internationalisation

`addedAtLabel` + preformatted date supplied (localized) by the caller.

## Accessibility

Presentational `<article>` with truncated secondary fields.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, `bg-primary/10`
icon badge.

## Related

`organisms/mobility/CarsPanel`, `molecules/mobility/AddCarModal`.
