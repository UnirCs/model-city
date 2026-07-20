import { SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';
import { PUBLIC_ROUTES, localizedUrl, alternateLanguages } from '@modelcity/core/lib/seo/routes';

/**
 * Multilingual sitemap for the publicly indexable pages.
 *
 * One entry per (public route × locale), each carrying the full hreflang
 * `languages` map (incl. `x-default`) so search engines treat the localised
 * URLs as alternates of a single page. Authenticated routes under `(app)` are
 * deliberately excluded — they live behind the Auth0 session guard.
 *
 * Served at `/sitemap.xml` (already excluded from the proxy matcher).
 *
 * @returns {import('next').MetadataRoute.Sitemap}
 */
export default function sitemap() {
  const lastModified = new Date();

  return PUBLIC_ROUTES.flatMap((path) => {
    const languages = alternateLanguages(path);
    return SUPPORTED_LANGS.map((lang) => ({
      url: localizedUrl(lang, path),
      lastModified,
      changeFrequency: 'monthly',
      priority: path === '' ? 1 : 0.6,
      alternates: { languages },
    }));
  });
}
