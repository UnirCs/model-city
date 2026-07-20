import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Organism: UnauthorizedView
 * Full-page inline error surface shown when a user tries to access a route
 * that their role does not permit.
 *
 * The copy is themed around the city of Aranjuez and its gated royal
 * heritage precincts — the same way the palace gates require the right
 * credentials, so does this section of the portal.
 *
 * Receives the already-resolved `t` (dict.unauthorized) and the current
 * `lang` string so it can build the localised home link without needing
 * the TranslationsProvider (works in server components).
 *
 * @param {{
 *   t: {
 *     gateTitle: string,
 *     gateSubtitle: string,
 *     gateBody: string,
 *     gateHint: string,
 *     backHome: string,
 *   },
 *   lang: string,
 * }} props
 */
export default function UnauthorizedView({ t, lang }) {
  return (
    <div className="flex flex-col items-center justify-center py-2xl px-gutter text-center gap-lg max-w-3xl mx-auto">

      {/* Decorative city-gate illustration using Material Symbols */}
      <div className="relative flex items-center justify-center w-28 h-28">
        {/* Outer glow ring */}
        <span
          className="absolute inset-0 rounded-full bg-error-container/40 animate-pulse"
          aria-hidden="true"
        />
        {/* Padlock atop a city landmark backdrop */}
        <span className="relative flex flex-col items-center gap-0">
          <Icon
            name="domain"
            size={52}
            className="text-on-surface-variant/30"
          />
          <span className="absolute bottom-0 right-0 bg-surface rounded-full p-xs shadow-md border border-outline-variant">
            <Icon name="lock" fill size={26} className="text-error" />
          </span>
        </span>
      </div>

      {/* Main heading */}
      <div className="flex flex-col gap-sm">
        <h1 className="text-h2 font-semibold text-primary">{t.gateTitle}</h1>
        <p className="text-body-lg text-on-surface-variant font-medium">{t.gateSubtitle}</p>
      </div>

      {/* Body explanation */}
      <p className="text-body-md text-on-surface-variant leading-relaxed">{t.gateBody}</p>

      {/* Flavour hint — styled like a plaque */}
      <div className="w-full rounded-md bg-surface-container border border-outline-variant px-lg py-md flex items-start gap-sm">
        <Icon name="signpost" size={20} className="text-secondary shrink-0 mt-0.5" />
        <p className="text-caption text-on-surface-variant italic text-left">{t.gateHint}</p>
      </div>

      {/* Back to home link */}
      <Link
        href={`/${lang}/home`}
        className="inline-flex items-center gap-sm px-lg py-sm rounded-md bg-primary text-on-primary text-label-md font-semibold hover:opacity-90 transition-opacity"
      >
        <Icon name="home" size={18} className="text-on-primary" />
        {t.backHome}
      </Link>
    </div>
  );
}

