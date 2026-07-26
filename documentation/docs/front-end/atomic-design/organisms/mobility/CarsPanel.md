---
title: CarsPanel
sidebar_label: CarsPanel
sidebar_position: 1
---

# CarsPanel

`packages/mobility/components/organisms/CarsPanel.js` · **Tier:** organism · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

Renders the user's car collection with a CTA to register a new one, owning the
open/close state of the [`AddCarModal`](../../molecules/mobility/AddCarModal.md). Pure
client component; the parent page fetches the cars server-side and passes them down.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `cars` | `Array<object>` | yes | — | The user's registered cars. |
| `labels` | `object` | yes | — | Panel + modal copy. |
| `lang` | `string` | yes | — | Locale. |

## Composition

- **Uses:** `molecules/mobility/CarCard`, `molecules/mobility/AddCarModal`,
  `atoms/core/Icon`/`Button`, `useState`.
- **Used by:** the `/mobility/cars` page.

## Internationalisation

All copy via `labels`.

## Accessibility

Empty state and add CTA are labelled; the modal is accessible.

## Related

`molecules/mobility/CarCard`, `molecules/mobility/AddCarModal`.
