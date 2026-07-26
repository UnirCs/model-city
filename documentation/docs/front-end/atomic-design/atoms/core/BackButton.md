---
title: BackButton
sidebar_label: BackButton
sidebar_position: 3
---

# BackButton

`packages/core/components/atoms/BackButton.js` · **Tier:** atom · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

Locale-aware "back" control. Instead of a blind `router.back()`, it resolves the
path the previous in-app navigation came from and re-localises it to the locale of
the page the user is currently on, so a back action never reverts an earlier
language switch — on `/es/events/1` it lands on `/es/events`, never `/en/events`.
The navigation logic lives in the shared `useLocalizedBack` hook (also used by the
form "cancel" controls). The label always comes from the shared `common.back`
translation, so call sites never pass it.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `className` | `string` | no | `''` | Extra classes appended last. |

## Composition

- **Uses:** `atoms/core/Icon` (`arrow_back`), `useTranslations()`
  (`core/lib/i18n/TranslationsProvider`), `useLocalizedBack()`
  (`core/lib/nav/useLocalizedBack`).
- **Used by:** detail pages and form organisms as the back/cancel affordance.

## Internationalisation

Reads `dict.common.back` from the translations context (falls back to `'Volver'`).
See [Internationalisation](../../../architecture/i18n.md) for `useLocalizedBack`.

## Accessibility

Native `<button type="button">`; the leading icon is `aria-hidden` (from `Icon`)
and the visible label provides the accessible name. Hover moves the icon and shifts
colour for affordance.

## Styling & tokens

`text-label-md`, `text-secondary` → `hover:text-primary`, `gap-xs`, colour-
transition utilities.

## Usage

```jsx
<BackButton />
```

## Related

`atoms/core/Icon`, and the `useLocalizedBack` hook documented in
[Internationalisation](../../../architecture/i18n.md).
