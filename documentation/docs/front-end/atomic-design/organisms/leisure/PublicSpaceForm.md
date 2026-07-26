---
title: PublicSpaceForm
sidebar_label: PublicSpaceForm
sidebar_position: 12
---

# PublicSpaceForm

`packages/leisure/components/organisms/PublicSpaceForm.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The shared form to create (POST) or fully update (PUT) a public (sports) space. Two
columns: multilingual textual fields on the left, the map picker on the right.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `mode` | `'create' \| 'edit'` | yes | — | Create or update. |
| `space` | `object` | no | — | Existing space (edit mode). |
| `t` | `object` | yes | — | `leisure.spaceForm` dictionary. |
| `lang` | `string` | yes | — | Locale. |

## Behaviour

Initialises multilingual `{ es, en, fr }` name/description from
`space.translations`; the map picker drives the coordinates; back navigation via
`useLocalizedBack`.

## Composition

- **Uses:** `MultilingualFields`, `molecules/core/LocationPickerMapClient`,
  `atoms/core/FormField`, `atoms/core/Button`, the public-space server actions.
- **Used by:** the create/edit sports-space routes.

## Internationalisation

Translatable fields via `MultilingualFields`; labels from `leisure.spaceForm`.

## Accessibility

Fields via `FormField`; the map picker is keyboard-operable.

## Related

`molecules/core/MultilingualFields`, `molecules/core/LocationPickerMapClient`,
`organisms/leisure/SpaceDetailView`.
