---
title: SettingsButton
sidebar_label: SettingsButton
sidebar_position: 27
---

# SettingsButton

`packages/core/components/molecules/SettingsButton.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A top-bar gear button that opens the [`SettingsModal`](./SettingsModal.md). It owns
the open/close state and forwards the access token to the modal.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `accessToken` | `string \| null` | yes | — | Passed to `SettingsModal` (needed by the certificate/settings actions). |

## Composition

- **Uses:** `atoms/core/Icon` (`settings`), `molecules/core/SettingsModal`,
  `useState`, `useTranslations()`.
- **Used by:** `organisms/core/TopNavBar`.

## Internationalisation

Reads `dict.a11y.settingsMenuLabel` (fallback `'Open settings'`) for the button
`aria-label`.

## Accessibility

Icon-only button with an `aria-label` and a visible `focus-visible` ring.

## Styling & tokens

`text-on-surface-variant`, `hover:bg-surface-container`, `rounded-md`,
`focus-visible:outline-primary`.

## Usage

```jsx
<SettingsButton accessToken={accessToken} />
```

## Related

`molecules/core/SettingsModal`, `organisms/core/TopNavBar`.
