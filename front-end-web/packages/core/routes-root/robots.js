import { SITE_URL } from '@modelcity/core/lib/seo/routes';

/**
 * robots.txt for the site.
 *
 * Crawling is allowed for public pages and points at the multilingual sitemap.
 * `/auth/*` (Auth0 endpoints) and `/api/*` are disallowed. Pages under the
 * `(app)` route group are additionally marked `noindex` via that group's
 * layout metadata, since they redirect to login and must never be indexed.
 *
 * Served at `/robots.txt` (already excluded from the proxy matcher).
 *
 * @returns {import('next').MetadataRoute.Robots}
 */
export default function robots() {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: ['/auth/', '/api/'],
    },
    sitemap: `${SITE_URL}/sitemap.xml`,
    host: SITE_URL,
  };
}
