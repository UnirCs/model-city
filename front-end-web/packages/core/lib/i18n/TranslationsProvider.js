'use client';

import { createContext, useContext } from 'react';

/**
 * @typedef {{ lang: string, dict: object }} TranslationsContextValue
 * @type {React.Context<TranslationsContextValue>}
 */
const TranslationsContext = createContext({ lang: 'es', dict: {} });

/**
 * Provides the current language and its dictionary to all client children.
 * Must be rendered inside a Server Component that has already loaded the dictionary.
 *
 * @param {{ lang: string, dict: object, children: React.ReactNode }} props
 */
export function TranslationsProvider({ lang, dict, children }) {
  return (
    <TranslationsContext.Provider value={{ lang, dict }}>
      {children}
    </TranslationsContext.Provider>
  );
}

/**
 * Returns the current language code and dictionary.
 * Must be called from a Client Component inside a TranslationsProvider.
 *
 * @returns {TranslationsContextValue}
 */
export function useTranslations() {
  return useContext(TranslationsContext);
}

