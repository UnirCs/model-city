/**
 * Mobility i18n namespace loaders.
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
    mobility: () => import('./locales/es/mobility.json').then((m) => m.default),
  },
  en: {
    mobility: () => import('./locales/en/mobility.json').then((m) => m.default),
  },
  fr: {
    mobility: () => import('./locales/fr/mobility.json').then((m) => m.default),
  },
};
