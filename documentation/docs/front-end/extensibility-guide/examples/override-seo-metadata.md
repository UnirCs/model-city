---
title: Override public SEO & metadata
sidebar_label: Override SEO metadata
sidebar_position: 18
---

# Override public SEO & metadata

**Goal:** change the public site's SEO surface — the indexable route list, the
canonical/hreflang alternates and robots — for the city.

- **Override file:** `overrides/core/lib/seo/routes.js`
- **Regen needed:** no (it's a `lib` module). If you also change the root layout or
  the `robots`/`sitemap` route files, those are route sources — run
  `npm run gen:modules`.

`core/lib/seo/routes.js` is the single source of truth for public URLs (see the
multilingual-SEO section of [Internationalisation](../../architecture/i18n.md)):
`SITE_URL`, `PUBLIC_ROUTES`, `localizedUrl`, `alternateLanguages`, `buildAlternates`.
`app/sitemap.js` and `app/robots.js` read from it.

## Recipe — expose a new public page

Keep the **same exports**; add your city-only public path to `PUBLIC_ROUTES` so it
gets hreflang alternates and a sitemap entry:

```js
// overrides/core/lib/seo/routes.js  (copy upstream, then edit)
export const SITE_URL = process.env.APP_BASE_URL ?? 'https://aranjuez.example';

export const PUBLIC_ROUTES = [
  '',                 // landing
  '/help/glossary',
  '/festivals',       // ← new city-only public page
];

// …keep localizedUrl / alternateLanguages / buildAlternates unchanged…
```

Then pass the same `{ path }` to `makeMetadata` from the page so it emits the
canonical + `languages` alternates:

```js
// in overrides/leisure/routes/festivals/page.js
import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';
export const generateMetadata = makeMetadata('nav.festivals', { path: '/festivals' });
```

## Notes

- Only the **public** surface is indexable; everything under the `(app)` gate exports
  `robots: { index: false, follow: false }` and is excluded — do not add gated paths to
  `PUBLIC_ROUTES`.
- `metadataBase` is declared once in the root `[lang]` layout; to change it, override
  `overrides/core/routes-public/layout.js` (a route source → run the codegen).

## Verify

`npm run build` then inspect `/sitemap.xml` and the page `<head>` — the new public
route appears with `hreflang` alternates and a canonical URL.
