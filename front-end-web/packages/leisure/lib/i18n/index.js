/**
 * Leisure i18n namespace loaders (events/sports spaces and tourism).
 *
 * Each feature module owns its translations and exposes a per-locale map of
 * lazy loaders. The core dictionary engine (`@modelcity/core/lib/i18n/dictionaries`)
 * composes these with the loaders of every other module, so a namespace lives
 * next to the code that uses it.
 *
 * @type {Record<string, Record<string, () => Promise<object>>>}
 */
export const loaders = {
  es: {
    leisure: () => import('./locales/es/leisure.json').then((m) => m.default),
    tourism: () => import('./locales/es/tourism.json').then((m) => m.default),
  },
  en: {
    leisure: () => import('./locales/en/leisure.json').then((m) => m.default),
    tourism: () => import('./locales/en/tourism.json').then((m) => m.default),
  },
  fr: {
    leisure: () => import('./locales/fr/leisure.json').then((m) => m.default),
    tourism: () => import('./locales/fr/tourism.json').then((m) => m.default),
  },
};
