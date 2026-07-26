---
title: Override a global provider
sidebar_label: Override a provider
sidebar_position: 4
---

# Override a global provider

**Goal:** change global context behaviour — here make the city default to **dark**
theme and use a city-specific cookie — by overriding the
[`ThemeProvider`](../../atomic-design/providers/ThemeProvider.md).

- **Override file:** `overrides/core/components/providers/ThemeProvider.js`
- **Regen needed:** no.

Providers are plain client modules imported by the layout provider stack, so
resolution swaps them. Keep the **same named exports** (`ThemeProvider` **and**
`useTheme`) — many components (e.g. `ThemeToggle`) import `useTheme` from this path.

## Recipe

```js
// overrides/core/components/providers/ThemeProvider.js
'use client';

import { createContext, useContext, useEffect, useState } from 'react';

const COOKIE_NAME = 'ARANJUEZ-THEME';
const MAX_AGE = 60 * 60 * 24 * 365;
const ThemeContext = createContext({ theme: 'dark', toggle: () => {} });

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('dark'); // ← city default

  useEffect(() => {
    try {
      const stored = localStorage.getItem(COOKIE_NAME);
      const resolved = stored ?? 'dark';        // ← default dark, ignore OS pref
      setTheme(resolved);
      document.documentElement.classList.toggle('dark', resolved === 'dark');
    } catch {}
  }, []);

  const toggle = () => setTheme((prev) => {
    const next = prev === 'dark' ? 'light' : 'dark';
    try { localStorage.setItem(COOKIE_NAME, next); } catch {}
    document.cookie = `${COOKIE_NAME}=${next}; path=/; max-age=${MAX_AGE}; SameSite=Lax`;
    document.documentElement.classList.toggle('dark', next === 'dark');
    return next;
  });

  return <ThemeContext.Provider value={{ theme, toggle }}>{children}</ThemeContext.Provider>;
}

// Must keep this export — ThemeToggle and others consume it.
export function useTheme() {
  return useContext(ThemeContext);
}
```

## Notes

- The **named-export contract matters** here more than for a component: dropping
  `useTheme` would break every consumer. Match the upstream export set exactly.

## Verify

`npm run dev`; the app starts in dark mode and the theme toggle still flips it.
