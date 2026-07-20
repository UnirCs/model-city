import { SUPPORTED_LANGS, DEFAULT_LANG } from '@modelcity/core/lib/i18n/dictionaries';

/**
 * Public origin of the site, used to build absolute SEO URLs (hreflang
 * alternates, canonical, sitemap). Reuses the same `APP_BASE_URL` the Auth0
 * SDK relies on so there is a single source of truth for the deployment URL.
 */
export const SITE_URL = (process.env.APP_BASE_URL ?? 'http://localhost:3000').replace(/\/$/, '');

/**
 * Locale-less paths of the publicly indexable pages (no authentication
 * required). Everything under the `(app)` route group is behind the Auth0
 * session guard and is intentionally excluded. Keep this list in sync when a
 * new public page is added — it drives both the sitemap and its hreflang
 * alternates.
 *
 * @type {string[]}
 */
export const PUBLIC_ROUTES = ['', '/help/glossary'];

/**
 * Absolute URL for a locale-less path in a given language.
 *
 * @param {string} lang  Locale code (e.g. `'es'`).
 * @param {string} [path]  Path without locale, starting with `/` or empty.
 * @returns {string}
 */
export function localizedUrl(lang, path = '') {
  return `${SITE_URL}/${lang}${path}`;
}

/**
 * hreflang language map for a public path: one absolute URL per supported
 * locale plus an `x-default` pointing at the default language. Shared by
 * `generateMetadata` alternates and the sitemap.
 *
 * @param {string} [path]  Path without locale.
 * @returns {Record<string, string>}
 */
export function alternateLanguages(path = '') {
  const languages = Object.fromEntries(SUPPORTED_LANGS.map((l) => [l, localizedUrl(l, path)]));
  languages['x-default'] = localizedUrl(DEFAULT_LANG, path);
  return languages;
}

/**
 * Full `alternates` block for a page's metadata: the canonical URL of the
 * current locale plus the hreflang language map.
 *
 * @param {string} lang  Current locale.
 * @param {string} [path]  Path without locale.
 * @returns {{ canonical: string, languages: Record<string, string> }}
 */
export function buildAlternates(lang, path = '') {
  return { canonical: localizedUrl(lang, path), languages: alternateLanguages(path) };
}
