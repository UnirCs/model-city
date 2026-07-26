---
title: ThemeProvider
sidebar_label: ThemeProvider
sidebar_position: 1
---

# ThemeProvider

`packages/core/components/providers/ThemeProvider.js` · **Tier:** provider · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The global light/dark theme context. It resolves the initial theme (stored
preference, else the OS `prefers-color-scheme`), toggles the `.dark` class on
`<html>`, and persists the choice to both `localStorage` and a first-party cookie
(`MODEL-CITY-THEME`, one-year max-age). It exposes a `useTheme()` hook.

## Exports

| Export | Kind | Description |
| --- | --- | --- |
| `ThemeProvider` | component | Context provider; wrap the app (mounted in the root/public layout). |
| `useTheme` | hook | Returns `{ theme: 'light' \| 'dark', toggle: () => void }`. |

## Props

`ThemeProvider` takes `{ children }`.

## Behaviour

On mount it reads `localStorage` (falling back to the media query), sets the state
and applies the `.dark` class. `toggle()` flips the theme, persists it and updates
the class. Applying the class before first paint avoids a flash of the wrong theme
(FOUC).

## Composition

- **Uses:** React context + `useState`/`useEffect`; `localStorage` + cookie.
- **Used by:** `molecules/core/ThemeToggle` (via `useTheme`), mounted globally in the
  layout provider stack.

## Internationalisation

None.

## Accessibility

Supports the user's OS colour-scheme preference by default; the design tokens meet
the contrast targets in both themes (see
[Design system & tokens](../../architecture/design-system.md)).

## Usage

```jsx
// layout
<ThemeProvider>{children}</ThemeProvider>

// consumer
const { theme, toggle } = useTheme();
```

## Related

`molecules/core/ThemeToggle`, [Design system & tokens](../../architecture/design-system.md).
