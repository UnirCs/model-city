---
title: AddCarModal
sidebar_label: AddCarModal
sidebar_position: 1
---

# AddCarModal

`packages/mobility/components/molecules/AddCarModal.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

A modal hosting the "register a new car" form (license plate, nickname, brand,
model). It calls the `addUserCar` server action and, on success, refreshes the
surrounding page so the new car appears.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Visibility. |
| `onClose` | `() => void` | yes | — | Close handler. |
| `labels` | `object` | yes | — | Localized form copy (fields, validation, submit/cancel, errors). |

## Behaviour

Client-side validation of the fields; the plate uses `LicensePlateInput`; success and
errors are announced via `useAnnounce`, and success refreshes the page.

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/mobility/LicensePlateInput`,
  `atoms/core/FormField`/`Button`/`Icon`, `addUserCar` action, `useAnnounce`,
  `useRouter`.
- **Used by:** `organisms/mobility/CarsPanel`.

## Internationalisation

All copy via `labels`.

## Accessibility

The accessible `core/Modal`; the plate input carries its own a11y; results announced.

## Related

`atoms/mobility/LicensePlateInput`, `molecules/mobility/CarCard`,
`organisms/mobility/CarsPanel`.
