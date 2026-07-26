---
title: CreateQuestionForm
sidebar_label: CreateQuestionForm
sidebar_position: 2
---

# CreateQuestionForm

`packages/engagement/components/organisms/CreateQuestionForm.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

The full form for platform administrators to create a new civic consultation
(multilingual title/description/objectives, image, open/close dates, and zone →
neighbourhood location). On success it navigates to the new question's detail page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | Form dictionary. |
| `lang` | `string` | yes | — | Locale. |

## Behaviour

Holds multilingual `{ es, en, fr }` state for the title, description and a dynamic
list of objectives; the location comes from the neighbourhoods config. Submits the
create-question action and redirects to the detail page; back navigation uses
`useLocalizedBack`.

## Composition

- **Uses:** `MultilingualFields`, `atoms/core/FormField`, `atoms/core/Button`,
  `atoms/core/Icon`, the neighbourhoods config, the create-question action.
- **Used by:** the `/participation/questions/new` page.

## Internationalisation

Translatable fields via `MultilingualFields` with AI translate; labels via `t`.

## Accessibility

Fields via `FormField`; dynamic objective rows have add/remove controls.

## Related

`molecules/engagement/EditQuestionModal`, `organisms/engagement/QuestionDetailView`,
`molecules/core/MultilingualFields`.
