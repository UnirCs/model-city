---
title: LanguageSwitcher
sidebar_label: LanguageSwitcher
sidebar_position: 11
---

# LanguageSwitcher

`packages/core/components/molecules/LanguageSwitcher.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A dropdown that lets the user switch between supported languages. It writes the
chosen language to the `NEXT_LANG` cookie (so the proxy remembers the preference)
and re-localises the current page in place with `router.replace` — using `replace`
(not `push`) so no stale entry in the old language is left in history.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `variant` | `'default' \| 'landing'` | no | `'default'` | `landing` uses white/translucent styling for the banner header; `default` uses standard surface styling. |

## Behaviour

Iterates `SUPPORTED_LANGS` (`es`, `en`, `fr`). Selecting a language sets the cookie
(`max-age` one year, `SameSite=Lax`) and calls
`router.replace(relocalizePath(pathname, newLang))`. Closes on outside click.

## Composition

- **Uses:** `SUPPORTED_LANGS` + `relocalizePath` (`core/lib/i18n`),
  `atoms/core/Icon` (`language`), `usePathname`/`useRouter`, `useTranslations()`.
- **Used by:** `molecules/core/SettingsModal`, `organisms/core/LandingBanner`, the
  nav surfaces.

## Internationalisation

Central to i18n — see [Internationalisation](../../../architecture/i18n.md). Reads
`dict.lang.label` and `dict.lang[<code>]` for the option labels.

## Accessibility

Trigger with `aria-label` + `aria-expanded`; the panel is `role="listbox"` with
`role="option"` + `aria-selected` entries.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, `shadow-lg`;
landing variant uses `text-white/70` on translucency.

## Usage

```jsx
<LanguageSwitcher />
<LanguageSwitcher variant="landing" />
```

## Related

`molecules/core/SettingsModal`, [Internationalisation](../../../architecture/i18n.md).
