import Link from 'next/link';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';
import Icon from '@modelcity/core/components/atoms/Icon';

export const generateMetadata = makeMetadata('a11y.glossary.title', { path: '/help/glossary' });

/**
 * Public glossary page — defines every acronym and technical term used
 * across the portal. Each term is exposed through a native `<dfn>`
 * element so assistive technologies announce it as a definition.
 *
 * Public route: lives in `routes-public/` so its shim is emitted at
 * `[lang]/help/glossary` (outside the `(app)` group), keeping it reachable for
 * unauthenticated visitors arriving from the landing-page footer
 * (WCAG 2.2 SC 3.1.3 / 3.1.4).
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function GlossaryPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang);
  const t = dict.a11y.glossary;

  return (
    <div className="min-h-screen bg-background flex flex-col">
      {/* ── Top bar with back-to-home link ─────────────────── */}
      <header className="border-b border-outline-variant bg-surface-container">
        <div className="max-w-container-max mx-auto px-gutter h-14 flex items-center">
          <Link
            href={`/${lang}`}
            className="inline-flex items-center gap-xs text-label-md text-primary hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
          >
            <Icon name="arrow_back" size={18} />
            {dict.common.brand}
          </Link>
        </div>
      </header>

      <main
        id="main"
        tabIndex={-1}
        className="flex-1 focus:outline-none px-gutter py-xl"
      >
        <div className="max-w-[72ch] mx-auto flex flex-col gap-xl">
          {/* ── Header ─────────────────────────────────────── */}
          <section>
            <h1 className="text-h1 text-primary mb-sm">{t.title}</h1>
            <p className="text-body-lg text-on-surface-variant">{t.subtitle}</p>
          </section>

          {/* ── Glossary entries ───────────────────────────── */}
          <section aria-labelledby="glossary-entries-heading">
            <h2 id="glossary-entries-heading" className="sr-only">
              {t.title}
            </h2>
            <dl className="flex flex-col gap-md">
              {t.entries.map((entry) => (
                <div
                  key={entry.term}
                  id={`term-${entry.term.toLowerCase()}`}
                  className="rounded-md bg-surface-container border border-outline-variant p-md"
                >
                  <dt className="text-h3 text-on-surface mb-xs">
                    <dfn className="not-italic font-bold text-primary">
                      {entry.term}
                    </dfn>
                  </dt>
                  <dd className="text-body-md text-on-surface-variant">
                    {entry.definition}
                  </dd>
                </div>
              ))}
            </dl>
          </section>
        </div>
      </main>
    </div>
  );
}
