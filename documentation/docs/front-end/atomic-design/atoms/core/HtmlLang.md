---
title: HtmlLang
sidebar_label: HtmlLang
sidebar_position: 8
---

# HtmlLang

`packages/core/components/atoms/HtmlLang.js` · **Tier:** atom · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

Keeps `document.documentElement.lang` in sync with the `lang` prop. The root layout
initially sets `<html lang>` from a cookie, but the authoritative source is the
`[lang]` URL segment; this atom reconciles the two on the client (WCAG 2.2 SC
3.1.1, Language of Page). It renders nothing to the DOM.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | The active locale from the URL segment; written to `document.documentElement.lang`. |

## Composition

- **Uses:** `useEffect` only (side effect on `lang` change).
- **Used by:** the locale-level layout shell.

## Internationalisation

It **is** part of the i18n machinery — see
[Internationalisation](../../../architecture/i18n.md). It does not read the
dictionary.

## Accessibility

Ensures the document language matches the content language for assistive tech and
correct hyphenation/pronunciation (SC 3.1.1).

## Usage

```jsx
<HtmlLang lang={lang} />
```

## Related

`atoms/core/SkipLink`, and the [i18n architecture](../../../architecture/i18n.md).
