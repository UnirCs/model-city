'use client';

import { createContext, useContext, useState, useEffect } from 'react';

const AccessibilityContext = createContext(null);

const DEFAULT_SETTINGS = {
  highContrast: false,
  textSize: 'normal',
  cursorSize: false,
  highlightLinks: false,
};

const STORAGE_KEY = 'accessibility-settings';

/**
 * Accessibility Context
 * 
 * Manages accessibility preferences:
 * - highContrast: boolean (default false)
 * - textSize: 'normal' | 'large' (default 'normal')
 * - cursorSize: boolean (default false)
 * - highlightLinks: boolean (default false)
 * 
 * Settings are persisted in localStorage.
 */
export function AccessibilityProvider({ children }) {
  const [settings, setSettings] = useState(DEFAULT_SETTINGS);
  const [isLoaded, setIsLoaded] = useState(false);

  // Load settings from localStorage on mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        setSettings({ ...DEFAULT_SETTINGS, ...parsed });
      }
    } catch (error) {
      console.error('Failed to load accessibility settings:', error);
    } finally {
      setIsLoaded(true);
    }
  }, []);

  // Save settings to localStorage whenever they change
  useEffect(() => {
    if (isLoaded) {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
      } catch (error) {
        console.error('Failed to save accessibility settings:', error);
      }
    }
  }, [settings, isLoaded]);

  // Apply CSS classes to document based on settings
  useEffect(() => {
    if (!isLoaded) return;

    const root = document.documentElement;

    // High contrast
    if (settings.highContrast) {
      root.classList.add('a11y-high-contrast');
    } else {
      root.classList.remove('a11y-high-contrast');
    }

    // Text size
    root.classList.remove('a11y-text-normal', 'a11y-text-large');
    root.classList.add(`a11y-text-${settings.textSize}`);

    // Cursor size
    if (settings.cursorSize) {
      root.classList.add('a11y-large-cursor');
    } else {
      root.classList.remove('a11y-large-cursor');
    }

    // Highlight links
    if (settings.highlightLinks) {
      root.classList.add('a11y-highlight-links');
    } else {
      root.classList.remove('a11y-highlight-links');
    }
  }, [settings, isLoaded]);

  const updateSetting = (key, value) => {
    setSettings(prev => ({ ...prev, [key]: value }));
  };

  const resetSettings = () => {
    setSettings(DEFAULT_SETTINGS);
  };

  return (
    <AccessibilityContext.Provider
      value={{
        settings,
        updateSetting,
        resetSettings,
        isLoaded,
      }}
    >
      {children}
    </AccessibilityContext.Provider>
  );
}

export function useAccessibility() {
  const context = useContext(AccessibilityContext);
  if (!context) {
    throw new Error('useAccessibility must be used within AccessibilityProvider');
  }
  return context;
}
