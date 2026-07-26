---
title: ThemeToggle
sidebar_label: ThemeToggle
sidebar_position: 32
---

# ThemeToggle

`packages/core/components/molecules/ThemeToggle.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A button that toggles between light and dark mode, using the current dictionary for
accessible labels. It reads and flips the theme through the
[`ThemeProvider`](../../providers/ThemeProvider.md) context.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `className` | `string` | no | `''` | Extra classes appended last. |

## Composition

- **Uses:** `useTheme()` (`providers/ThemeProvider`), `useTranslations()`,
  `atoms/core/Icon` (`light_mode` / `dark_mode`).
- **Used by:** the settings/top-bar surfaces.

## Internationalisation

Reads `dict.theme.activateLight` / `dict.theme.activateDark` for the button
`aria-label` (which one depends on the current theme).

## Accessibility

Icon-only button with a theme-dependent `aria-label`.

## Styling & tokens

`text-on-surface-variant`, `hover:bg-surface-container`, `rounded-md`.

## Usage

```jsx
<ThemeToggle />
```

## Related

`providers/ThemeProvider` (the `useTheme` source), `molecules/core/SettingsModal`.
