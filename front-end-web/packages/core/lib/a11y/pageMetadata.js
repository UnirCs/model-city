import { getDictionary, SUPPORTED_LANGS, DEFAULT_LANG } from '@modelcity/core/lib/i18n/dictionaries';
import { buildAlternates } from '@modelcity/core/lib/seo/routes';

/**
 * Shared `generateMetadata` factory for every app route.
 *
 * Usage in a `page.js`:
 *
 *   export { generateMetadata } from '@modelcity/core/lib/a11y/pageMetadata';
 *   // or, for a static title:
 *   export const generateMetadata = makeMetadata('nav.events');
 *   // or, for a dynamic title (e.g. `[id]` routes):
 *   export async function generateMetadata({ params }) {
 *     const { lang } = await params;
 *     const dict = await getDictionary(lang);
 *     return makePageMeta(lang, dict.events.detail.title + ' – ' + eventName);
 *   }
 *
 * WCAG 2.2 — 2.4.2 Page Titled (Level A).
 *
 * @param {string} dictPath  Dot-separated key path into the dictionary.
 *   e.g. `'nav.events'` → `dict.nav.events`
 * @param {{ path?: string }} [options]  When `path` is provided (the page's
 *   locale-less path, e.g. `''` for the landing or `'/help/glossary'`), the
 *   metadata includes hreflang `alternates`. Only public, indexable pages
 *   should set it; authenticated pages omit it.
 * @returns {(ctx: { params: Promise<{ lang: string }> }) => Promise<import('next').Metadata>}
 */
export function makeMetadata(dictPath, options = {}) {
  return async function generateMetadata({ params }) {
    const { lang } = await params;
    const safeLang = SUPPORTED_LANGS.includes(lang) ? lang : DEFAULT_LANG;
    const dict = await getDictionary(safeLang);
    const title = resolvePath(dict, dictPath) ?? '';
    return makePageMeta(safeLang, title, dict, options);
  };
}

/**
 * Resolve a dot-separated path like `'nav.events'` into a nested object.
 * @param {Record<string, unknown>} obj
 * @param {string} path
 * @returns {string | undefined}
 */
function resolvePath(obj, path) {
  return path.split('.').reduce((cur, key) => (cur && typeof cur === 'object' ? cur[key] : undefined), obj);
}

/**
 * Build a standard `Metadata` object for a page.
 * The root layout already declares `template: "%s"` so the title template
 * appends `" · Portal Ciudadano"` automatically.
 *
 * @param {string} lang
 * @param {string} title  Human-readable page title (without the site suffix).
 * @param {Record<string, unknown>} [dict]
 * @param {{ path?: string }} [options]  When `path` is set, adds hreflang
 *   `alternates` for that public path.
 * @returns {import('next').Metadata}
 */
export function makePageMeta(lang, title, dict, options = {}) {
  const meta = dict?.meta ?? {};
  /** @type {import('next').Metadata} */
  const metadata = {
    title,
    description: meta.description ?? undefined,
    openGraph: { locale: lang, title },
  };
  if (typeof options.path === 'string') {
    metadata.alternates = buildAlternates(lang, options.path);
  }
  return metadata;
}

