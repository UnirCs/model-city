import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Organism: LandingFooter
 *
 * Slim footer of the public landing page with the municipal address, the
 * "powered by" attribution, the legal/help links (including the glossary) and
 * the copyright line.
 *
 * @param {{ t: object, lang: string, brand: string }} props
 *   `t` is the landing dictionary; `brand` is `common.brand`.
 */
export default function LandingFooter({ t, lang, brand }) {
  const footerLinks = [t.footer.legal, t.footer.cookies, t.footer.accessibility, t.footer.contact];

  return (
    <footer className="bg-surface-container border-t border-outline-variant py-md">
      <div className="max-w-container-max mx-auto px-gutter">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-sm mb-sm pb-sm border-b border-outline-variant">
          <div>
            <p className="text-caption text-on-surface-variant">{t.footer.address}</p>
            <p className="text-caption text-on-surface-variant mt-xs flex items-center gap-xs flex-wrap">
              <Icon name="code" size={12} className="text-primary/40 shrink-0" />
              {t.footer.poweredBy}
              {' '}
              <a
                href="https://github.com/modelcity"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline"
              >
                {t.footer.poweredByLinkLabel}
              </a>
            </p>
          </div>

          <nav className="flex flex-wrap gap-md" aria-label={t.footer.footerNav}>
            {footerLinks.map((l) => (
              <Link
                key={l}
                href="#"
                className="text-caption text-on-surface-variant hover:text-primary transition-colors"
              >
                {l}
              </Link>
            ))}
            {/* Glossary — public route, also linked from Help page. */}
            <Link
              href={`/${lang}/help/glossary`}
              className="text-caption text-on-surface-variant hover:text-primary transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              {t.footer.glossary}
            </Link>
          </nav>
        </div>

        <p className="text-caption text-on-surface-variant">
          © {new Date().getFullYear()} {brand} &middot; {t.footer.seat}
        </p>
      </div>
    </footer>
  );
}
