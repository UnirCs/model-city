---
title: CityPlaceForm
sidebar_label: CityPlaceForm
sidebar_position: 1
---

# CityPlaceForm

`packages/leisure/components/organisms/CityPlaceForm.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The shared form for creating (POST) or updating (PUT) a city place. Two columns: the
left holds the textual/metadata fields (multilingual), the right holds the
interactive map picker that drives the latitude/longitude state (the raw coordinates
are not shown to the user).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `mode` | `'create' \| 'edit'` | yes | — | Whether the form creates or updates. |
| `place` | `object` | no | — | Existing place (edit mode): name, coords, description, address, photoUrls, accessInfo, accessibilityInfo, category, visitDurationMinutes. |
| `t` | `object` | yes | — | `leisure`/`tourism` place-form dictionary. |
| `lang` | `string` | yes | — | Locale. |

(Exact prop set follows the source JSDoc; multilingual fields carry `{ es, en, fr }`
values.)

## Composition

- **Uses:** `MultilingualFields` (`DefaultLangFields` / `TranslationSections`),
  `molecules/core/LocationPickerMapClient`, `atoms/core/FormField`,
  `atoms/core/Button`, the place server actions, `useLocalizedBack`.
- **Used by:** the create/edit place routes.

## Internationalisation

Translatable fields use `MultilingualFields` with AI translate (see
[AI translation](../../../architecture/ai-translation.md)); labels from the
dictionary.

## Accessibility

Fields via `FormField`; the map picker is keyboard-operable with a manual-coordinate
alternative.

## Related

`molecules/core/MultilingualFields`, `molecules/core/LocationPickerMapClient`,
`organisms/leisure/PlaceDetailView`.
