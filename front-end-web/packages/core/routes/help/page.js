import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';
import Icon from '@modelcity/core/components/atoms/Icon';
import Link from 'next/link';

export const generateMetadata = makeMetadata('nav.help');

/**
 * Help page — persistent help mechanism (WCAG 2.2 SC 3.2.6).
 *
 * Provides:
 *  - Contact details (email, phone, schedule)
 *  - Frequently asked questions
 *
 * Linked from TopNavBar on every authenticated page.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function HelpPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'help');
  const t = dict.help;
  const a11yLabels = dict.a11y;

  return (
    <div className="max-w-[72ch] mx-auto flex flex-col gap-xl">
      {/* ── Header ─────────────────────────────────────────── */}
      <section>
        <h1 className="text-h1 text-primary mb-sm">{t.pageTitle}</h1>
        <p className="text-body-lg text-on-surface-variant">{t.pageSubtitle}</p>
      </section>

      {/* ── Easy-read summary ──────────────────────────────── */}
      <section
        aria-labelledby="help-easy-read-heading"
        className="rounded-md bg-secondary-container/20 border border-secondary/30 p-md"
      >
        <h2
          id="help-easy-read-heading"
          className="text-h3 text-on-surface mb-sm"
        >
          {t.easyRead.title}
        </h2>
        <p className="text-body-md text-on-surface-variant mb-sm">
          {t.easyRead.intro}
        </p>
        <ul className="flex flex-col gap-xs list-none p-0 m-0">
          {[t.easyRead.p1, t.easyRead.p2, t.easyRead.p3, t.easyRead.p4].map((line, i) => (
            <li key={i} className="flex items-start gap-sm text-body-md text-on-surface">
              <Icon name="check_circle" size={16} className="text-secondary shrink-0 mt-0.5" />
              <span>{line}</span>
            </li>
          ))}
        </ul>
      </section>

      {/* ── Contact ────────────────────────────────────────── */}
      <section aria-labelledby="help-contact-heading">
        <h2 id="help-contact-heading" className="text-h2 text-on-surface mb-md">
          {t.contactTitle}
        </h2>
        <p className="text-body-md text-on-surface-variant mb-lg">{t.contactBody}</p>

        <ul className="flex flex-col gap-md list-none p-0 m-0">
          <li className="flex items-start gap-md p-md rounded-md bg-surface-container border border-outline-variant">
            <Icon name="mail" size={24} className="text-secondary shrink-0 mt-0.5" />
            <div>
              <p className="text-label-md font-semibold text-on-surface">{t.emailLabel}</p>
              <a
                href={`mailto:${t.emailValue}`}
                className="text-body-md text-secondary hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
              >
                {t.emailValue}
              </a>
            </div>
          </li>

          <li className="flex items-start gap-md p-md rounded-md bg-surface-container border border-outline-variant">
            <Icon name="call" size={24} className="text-secondary shrink-0 mt-0.5" />
            <div>
              <p className="text-label-md font-semibold text-on-surface">{t.phoneLabel}</p>
              <a
                href={`tel:${t.phoneValue.replace(/\s/g, '')}`}
                className="text-body-md text-secondary hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
              >
                {t.phoneValue}
              </a>
            </div>
          </li>

          <li className="flex items-start gap-md p-md rounded-md bg-surface-container border border-outline-variant">
            <Icon name="schedule" size={24} className="text-secondary shrink-0 mt-0.5" />
            <div>
              <p className="text-label-md font-semibold text-on-surface">{t.scheduleLabel}</p>
              <p className="text-body-md text-on-surface-variant">{t.scheduleValue}</p>
            </div>
          </li>
        </ul>
      </section>

      {/* ── FAQ ────────────────────────────────────────────── */}
      <section aria-labelledby="help-faq-heading">
        <h2 id="help-faq-heading" className="text-h2 text-on-surface mb-md">
          {t.faqTitle}
        </h2>
        <dl className="flex flex-col gap-md">
          {t.faq.map((item, i) => (
            <div
              key={i}
              className="rounded-md bg-surface-container border border-outline-variant p-md"
            >
              <dt className="text-label-md font-semibold text-on-surface mb-xs flex items-start gap-sm">
                <Icon name="help_outline" size={18} className="text-secondary shrink-0 mt-0.5" />
                {item.q}
              </dt>
              <dd className="text-body-md text-on-surface-variant ml-6.5">
                {item.a}
              </dd>
            </div>
          ))}
        </dl>
      </section>

      {/* ── Glossary link ──────────────────────────────────── */}
      <section>
        <Link
          href={`/${lang}/help/glossary`}
          className="inline-flex items-center gap-sm text-label-md text-secondary hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
        >
          <Icon name="menu_book" size={18} />
          {t.glossaryLinkLabel}
        </Link>
      </section>
    </div>
  );
}


