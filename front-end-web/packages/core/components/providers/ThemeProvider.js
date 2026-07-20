'use client';

import { createContext, useContext, useEffect, useState } from 'react';

const COOKIE_NAME  = 'MODEL-CITY-THEME';
const STORAGE_KEY  = 'MODEL-CITY-THEME';
const MAX_AGE      = 60 * 60 * 24 * 365; // 1 year

/** Writes the theme value to both localStorage and a first-party cookie. */
function persistTheme(value) {
  try {
    localStorage.setItem(STORAGE_KEY, value);
  } catch {}
  document.cookie = `${COOKIE_NAME}=${value}; path=/; max-age=${MAX_AGE}; SameSite=Lax`;
}

const ThemeContext = createContext({ theme: 'light', toggle: () => {} });

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  useEffect(() => {
    try {
      const stored      = localStorage.getItem(STORAGE_KEY);
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      const resolved    = stored ?? (prefersDark ? 'dark' : 'light');
      setTheme(resolved);
      document.documentElement.classList.toggle('dark', resolved === 'dark');
    } catch {}
  }, []);

  const toggle = () => {
    setTheme((prev) => {
      const next = prev === 'dark' ? 'light' : 'dark';
      persistTheme(next);
      document.documentElement.classList.toggle('dark', next === 'dark');
      return next;
    });
  };

  return (
    <ThemeContext.Provider value={{ theme, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
