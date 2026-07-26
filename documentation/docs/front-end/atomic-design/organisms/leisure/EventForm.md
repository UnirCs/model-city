---
title: EventForm
sidebar_label: EventForm
sidebar_position: 5
---

# EventForm

`packages/leisure/components/organisms/EventForm.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The shared form to create (POST) or fully update (PUT) a leisure event. It receives
the catalogue of available city places so the user can bind the event to one (events
are always anchored to an existing place). Text fields are multilingual.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `mode` | `'create' \| 'edit'` | yes | — | Create or update. |
| `event` | `object` | no | — | Existing event (edit mode). |
| `places` | `Array<{ id, name }>` | yes | — | Catalogue of places to anchor the event to. |
| `t` | `object` | yes | — | `leisure.eventForm` dictionary. |
| `typeLabels` | `Record<string,string>` | yes | — | `leisure.eventTypes` labels. |
| `lang` | `string` | yes | — | Locale. |

## Behaviour

Initialises multilingual `{ es, en, fr }` name/description from `event.translations`;
maps the form to the create/update event actions; navigation back uses
`useLocalizedBack`.

## Composition

- **Uses:** `MultilingualFields`, `atoms/core/FormField`, `atoms/core/Button`,
  `atoms/core/Icon`, the event server actions, `useLocalizedBack`.
- **Used by:** the create/edit event routes.

## Internationalisation

Translatable fields via `MultilingualFields` with AI translate; type labels via
`typeLabels`.

## Accessibility

Fields via `FormField`; the place binding is a labelled select.

## Related

`molecules/core/MultilingualFields`, `organisms/leisure/EventDetailView`.
