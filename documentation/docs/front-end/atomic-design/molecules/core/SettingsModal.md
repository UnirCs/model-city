---
title: SettingsModal
sidebar_label: SettingsModal
sidebar_position: 28
---

# SettingsModal

`packages/core/components/molecules/SettingsModal.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The app settings dialog. It groups three areas: **language preferences** (es/en/fr),
**FNMT certificate verification**, and a collapsible **accessibility preferences**
panel (high contrast, text size, large cursor, highlight links).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Visibility. |
| `onClose` | `() => void` | yes | — | Close handler. |
| `accessToken` | `string \| null` | yes | — | Passed to `verifyCertificate` for the browser mTLS check. |

## Behaviour

- **Language** — writes the `NEXT_LANG` cookie and `router.replace`s the
  re-localised path (see [Internationalisation](../../../architecture/i18n.md)).
- **Certificate** — calls `verifyCertificate(accessToken)` (browser mTLS →
  `certClient`), showing a `checking/valid/invalid` state for 3 s and announcing the
  result.
- **Accessibility** — reads/writes the `useAccessibility()` settings
  (`highContrast`, `textSize`, `cursorSize`, `highlightLinks`).

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/Icon`, `useTranslations()`,
  `SUPPORTED_LANGS` + `relocalizePath` (`core/lib/i18n`), `verifyCertificate`
  (`core/lib/api/certClient`), `useAccessibility` + `useAnnounce`
  (`core/lib/a11y`).
- **Used by:** `molecules/core/SettingsButton`.

## Internationalisation

Reads the `a11y` and `certificate` dictionary slices; language labels from
`dict.lang`.

## Accessibility

Toggle buttons carry `aria-pressed`; the accessibility panel toggle uses
`aria-expanded` + `aria-controls`; results are announced via the live-region
announcer.

## Styling & tokens

`bg-primary`/`text-on-primary` active toggles, `bg-success`/`bg-error` cert states,
`rounded-md`.

## Usage

```jsx
<SettingsModal open={open} onClose={close} accessToken={accessToken} />
```

## Related

`molecules/core/SettingsButton`, `molecules/core/LanguageSwitcher`,
`molecules/core/CertInvalidError`, [Local mTLS](../../../../how-to-start/mtls-local.md).
