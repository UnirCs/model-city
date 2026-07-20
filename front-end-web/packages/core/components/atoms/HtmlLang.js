'use client';

import { useEffect } from 'react';

/**
 * Atom: HtmlLang
 *
 * Keeps `document.documentElement.lang` in sync with the `lang` prop.
 * Needed because the root layout (src/app/layout.js) sets lang from a
 * cookie, but the authoritative source is the [lang] URL segment —
 * WCAG 2.2 SC 3.1.1.
 *
 * Renders nothing to the DOM.
 *
 * @param {{ lang: string }} props
 */
export default function HtmlLang({ lang }) {
  useEffect(() => {
    document.documentElement.lang = lang;
  }, [lang]);

  return null;
}
