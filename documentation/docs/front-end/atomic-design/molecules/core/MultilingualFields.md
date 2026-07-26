---
title: MultilingualFields
sidebar_label: MultilingualFields
sidebar_position: 18
---

# MultilingualFields

`packages/core/components/molecules/MultilingualFields.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The translatable-field primitives shared by every create/edit form. A form declares
its translatable fields **once** as a descriptor array and renders them in two
tiers: the default-language (Spanish) controls inline with the common data, and one
collapsible section per secondary language, each field carrying an **"AI translate"**
button that fills it in from the Spanish source via the `translateText` server
action (see [AI translation](../../../architecture/ai-translation.md)).

## Exports

This module has **no default export**; it exports named building blocks:

| Export | Kind | Purpose |
| --- | --- | --- |
| `DefaultLangFields` | component | Tier 1 — default-language controls, inline. |
| `TranslationSections` | component | Tier 2 — collapsible per-language sections with AI translate. |
| `AiTranslateButton` | component | Self-contained translate button (owns idle/loading/error state); reusable for ad-hoc translatable lists. |

### `TranslatableField` descriptor

`{ key, label, hint?, placeholder?, error?, as?: 'input'|'textarea', rows?, value: { es, en, fr }, onChange: (lang, value) => void }`

### Key props

- **`DefaultLangFields`** — `{ fields: TranslatableField[] }`.
- **`TranslationSections`** — `{ fields, defaultOpen?, renderExtra?, onTranslate? }`
  (`onTranslate` defaults to the `translateText` action).
- **`AiTranslateButton`** — `{ source, targetLang, onResult, ariaLabel?, onTranslate? }`.

## Behaviour

`AiTranslateButton` is disabled when the Spanish source is empty or a request is in
flight; on success it calls `onResult(text)`, otherwise it shows an error icon +
tooltip and leaves the field untouched. Secondary languages are
`SUPPORTED_LANGS` minus `DEFAULT_LANG`, delimited by a flag glyph.

## Composition

- **Uses:** `atoms/core/Icon`, `useTranslations()`, `SUPPORTED_LANGS`/`DEFAULT_LANG`,
  `translateText` (`core/lib/actions/translation`).
- **Used by:** every create/edit form organism (`EventForm`, `CityPlaceForm`,
  `CityRouteForm`, `PublicSpaceForm`, `CreateQuestionForm`, `SecurityAlertForm`, …).

## Internationalisation

Reads `dict.aiTranslate.*` (button/hint/error/section copy) and `dict.lang` (language
labels). See [AI translation](../../../architecture/ai-translation.md) for the flow.

## Accessibility

Each translate button carries a contextual `title`/`aria-label`; the per-language
sections use `aria-expanded` toggles.

## Styling & tokens

`bg-surface-container-low` sections, `border-outline-variant`, `rounded-md`,
`grid-template-rows` `0fr → 1fr` expand animation.

## Usage

```jsx
import { DefaultLangFields, TranslationSections } from '@modelcity/core/components/molecules/MultilingualFields';

<DefaultLangFields fields={fields} />
<TranslationSections fields={fields} />
```

## Related

[AI translation](../../../architecture/ai-translation.md),
[Internationalisation](../../../architecture/i18n.md), the form organisms.
