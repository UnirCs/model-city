/**
 * Citizen-engagement i18n namespace loaders.
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
    participation: () => import('./locales/es/participation.json').then((m) => m.default),
    security:      () => import('./locales/es/security.json').then((m) => m.default),
  },
  en: {
    participation: () => import('./locales/en/participation.json').then((m) => m.default),
    security:      () => import('./locales/en/security.json').then((m) => m.default),
  },
  fr: {
    participation: () => import('./locales/fr/participation.json').then((m) => m.default),
    security:      () => import('./locales/fr/security.json').then((m) => m.default),
  },
};
