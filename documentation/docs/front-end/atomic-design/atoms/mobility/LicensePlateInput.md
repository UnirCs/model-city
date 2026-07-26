---
title: LicensePlateInput
sidebar_label: LicensePlateInput
sidebar_position: 1
---

# LicensePlateInput

`packages/mobility/components/atoms/LicensePlateInput.js` · **Tier:** atom · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

A visual recreation of an EU-style license-plate input. The left strip mimics the
blue EU band with the country code; the right portion is a free-text input that
automatically uppercases its value. Purely presentational — the `value`/`onChange`
API mirrors a native `<input>`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `string` | yes | — | Controlled value. |
| `onChange` | `(next: string) => void` | yes | — | Called with the uppercased value. |
| `placeholder` | `string` | no | `'1234 ABC'` | Input placeholder. |
| `countryCode` | `string` | no | `'E'` | Country letter in the EU band. |
| `maxLength` | `number` | no | `16` | Max characters. |
| `disabled` | `boolean` | no | `false` | Disables the input. |
| `error` | `boolean` | no | `false` | Applies the error ring + `aria-invalid`. |
| `id` | `string` | no | (auto) | Input id (falls back to `useId`). |
| `aria-label` | `string` | no | — | Accessible name for the input. |

## Composition

- **Uses:** native `<input>` (+ `useId`); an `<abbr title="European Union">` in the
  band.
- **Used by:** `molecules/mobility/AddCarModal`, `organisms/mobility/ReservationForm`,
  `organisms/mobility/CreateSanctionForm`.

## Internationalisation

None (label supplied by the caller).

## Accessibility

The value uppercases automatically (`autoCapitalize="characters"`); the wrapper paints
a high-contrast `focus-within` ring while the inner input uses `focus:outline-none`
(SC 2.4.7 / 2.4.11 / 2.4.13); `aria-invalid` is set on error.

## Styling & tokens

`h-12` plate, `bg-blue-700` EU band, `border-outline-variant` / `border-error`,
`text-h3 tracking-widest`.

## Related

`molecules/mobility/AddCarModal`, `atoms/core/FormField`.
