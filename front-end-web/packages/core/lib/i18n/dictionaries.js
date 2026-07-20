/** Supported language codes. */
export const SUPPORTED_LANGS = ['es', 'en', 'fr'];

/** Default language when no preference is detected. */
export const DEFAULT_LANG = 'es';

/**
 * Dynamic loaders for the shared/common dictionary — one per locale.
 * Contains keys used across the whole app: navigation, header, auth,
 * theme, language switcher, status, OTP, certificate, unauthorized…
 */
const commonLoaders = {
  es: () => import('./locales/es/common.json').then((m) => m.default),
  en: () => import('./locales/en/common.json').then((m) => m.default),
  fr: () => import('./locales/fr/common.json').then((m) => m.default),
};

/**
 * Per-locale loaders for the always-on core service namespaces (landing, home,
 * profile, registration, administration and help).
 */
const coreServiceLoaders = {
  es: {
    landing:      () => import('./locales/es/landing.json').then((m) => m.default),
    home:         () => import('./locales/es/home.json').then((m) => m.default),
    profile:      () => import('./locales/es/profile.json').then((m) => m.default),
    registration: () => import('./locales/es/registration.json').then((m) => m.default),
    admin:        () => import('./locales/es/admin.json').then((m) => m.default),
    help:         () => import('./locales/es/help.json').then((m) => m.default),
  },
  en: {
    landing:      () => import('./locales/en/landing.json').then((m) => m.default),
    home:         () => import('./locales/en/home.json').then((m) => m.default),
    profile:      () => import('./locales/en/profile.json').then((m) => m.default),
    registration: () => import('./locales/en/registration.json').then((m) => m.default),
    admin:        () => import('./locales/en/admin.json').then((m) => m.default),
    help:         () => import('./locales/en/help.json').then((m) => m.default),
  },
  fr: {
    landing:      () => import('./locales/fr/landing.json').then((m) => m.default),
    home:         () => import('./locales/fr/home.json').then((m) => m.default),
    profile:      () => import('./locales/fr/profile.json').then((m) => m.default),
    registration: () => import('./locales/fr/registration.json').then((m) => m.default),
    admin:        () => import('./locales/fr/admin.json').then((m) => m.default),
    help:         () => import('./locales/fr/help.json').then((m) => m.default),
  },
};

/**
 * Per-service dictionary loaders — `[lang][service]`.
 *
 * Seeded with the always-on core namespaces and extended at startup by each
 * feature module through {@link registerServiceLoaders}. Keeping the engine
 * module-agnostic (it no longer imports the feature modules) is what lets the
 * orchestrator — not `core` — own composition; see
 * `src/lib/composition/registerModules.js` and
 * `docs/architecture/modular-frontend-packages.md`.
 */
const serviceLoaders = {
  es: { ...coreServiceLoaders.es },
  en: { ...coreServiceLoaders.en },
  fr: { ...coreServiceLoaders.fr },
};

/**
 * Registers a feature module's per-locale service loaders into the engine.
 * Called once at startup by the composition root for every active module. Later
 * registrations win on key collisions, mirroring an object spread.
 *
 * @param {Record<string, Record<string, () => Promise<object>>>} byLang
 *   `{ es: { service: loader, … }, en: {…}, fr: {…} }`.
 */
export function registerServiceLoaders(byLang) {
  for (const lang of SUPPORTED_LANGS) {
    Object.assign(serviceLoaders[lang], byLang?.[lang]);
  }
}

/**
 * Returns the dictionary for the given language.
 *
 * - `getDictionary(lang)` → returns only the common dictionary
 *   (navigation, header, auth, theme, status, OTP, certificate…).
 * - `getDictionary(lang, service)` → returns the common dictionary
 *   merged with the requested service namespace, so call sites can
 *   keep accessing `dict.common.*`, `dict.nav.*` and the service's
 *   own section (e.g. `dict.tourism.*`).
 *
 * Falls back to the default language for unsupported locales and
 * silently ignores unknown service names.
 *
 * @param {string} lang
 * @param {string} [service]
 * @returns {Promise<object>}
 */
export async function getDictionary(lang, service) {
  const safeLang = SUPPORTED_LANGS.includes(lang) ? lang : DEFAULT_LANG;
  const common = await commonLoaders[safeLang]();

  if (!service) return common;

  const loader = serviceLoaders[safeLang]?.[service];
  if (!loader) return common;

  const serviceDict = await loader();
  return { ...common, ...serviceDict };
}
