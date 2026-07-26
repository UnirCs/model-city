import LoginButton from '@modelcity/core/components/molecules/LoginButton';
import Icon from '@modelcity/core/components/atoms/Icon';
import Abbr from '@modelcity/core/components/atoms/Abbr';
import modelCityConfig from '@modelcity/config';

/**
 * Renders a translated sentence, wrapping occurrences of known acronyms in
 * `<abbr>` elements (WCAG 2.2 SC 3.1.4). Expansions come from `abbr[term]`.
 */
function withAbbr(text, terms, abbr) {
  const pattern = new RegExp(`(${terms.join('|')})`, 'g');
  return text.split(pattern).map((chunk, i) =>
    terms.includes(chunk)
      ? <Abbr key={i} title={abbr[chunk]}>{chunk}</Abbr>
      : chunk,
  );
}

/**
 * Organism: LandingBanner
 *
 * Full-bleed three-photo diagonal banner of the public landing page with the
 * oversized title, subtitle, login CTA and the FNMT digital-certificate note.
 *
 * @param {{
 *   t: object,                 // landing dictionary
 *   lang: string,
 *   abbr: Record<string, string>,  // a11y.abbr dictionary (acronym expansions)
 * }} props
 */
export default function LandingBanner({ t, lang, abbr }) {
  // Three landing photos (base + two diagonal strips), configured per city.
  const [baseImage, midImage, farImage] = modelCityConfig.landingImages;
  return (
    <section
      className="relative overflow-hidden flex items-center"
      style={{ minHeight: '82vh' }}
      aria-labelledby="landing-title"
    >
      {/* ── Layer 1: full-bleed base ── */}
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img
        src={baseImage.url}
        alt={baseImage.alt}
        className="absolute inset-0 w-full h-full object-cover"
        style={{ objectPosition: baseImage.objectPosition }}
        aria-hidden="true"
      />

      {/* ── Layer 2: diagonal strip, middle-right ── */}
      <div
        className="absolute inset-y-0 right-0"
        style={{ width: '56%', clipPath: 'polygon(14% 0%, 100% 0%, 100% 100%, 0% 100%)' }}
        aria-hidden="true"
      >
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={midImage.url}
          alt={midImage.alt}
          className="w-full h-full object-cover"
          style={{ objectPosition: midImage.objectPosition }}
        />
        {/* Subtle dark tint so it recedes vs. the top strip */}
        <div className="absolute inset-0" style={{ background: 'rgba(4,10,20,0.18)' }} />
      </div>

      {/* ── Layer 3: narrow diagonal strip, far right ── */}
      <div
        className="absolute inset-y-0 right-0"
        style={{ width: '28%', clipPath: 'polygon(18% 0%, 100% 0%, 100% 100%, 0% 100%)' }}
        aria-hidden="true"
      >
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={farImage.url}
          alt={farImage.alt}
          className="w-full h-full object-cover"
          style={{ objectPosition: farImage.objectPosition }}
        />
      </div>

      {/* ── Thin luminous accent lines at strip boundaries ── */}
      <div
        className="absolute inset-y-0 pointer-events-none"
        style={{
          right: '56%',
          width: '1px',
          background: 'linear-gradient(to bottom, transparent 0%, rgba(255,255,255,0.22) 30%, rgba(255,255,255,0.22) 70%, transparent 100%)',
          transform: 'skewX(-7deg)',
        }}
        aria-hidden="true"
      />
      <div
        className="absolute inset-y-0 pointer-events-none"
        style={{
          right: '28%',
          width: '1px',
          background: 'linear-gradient(to bottom, transparent 0%, rgba(255,255,255,0.30) 30%, rgba(255,255,255,0.30) 70%, transparent 100%)',
          transform: 'skewX(-7deg)',
        }}
        aria-hidden="true"
      />

      {/* ── Dark→transparent diagonal gradient (text readability) ── */}
      <div
        className="absolute inset-0"
        style={{
          background: [
            'linear-gradient(102deg,',
            '  rgba(4,10,20,0.97)  0%,',
            '  rgba(4,10,20,0.92) 24%,',
            '  rgba(4,10,20,0.72) 40%,',
            '  rgba(4,10,20,0.28) 54%,',
            '  rgba(4,10,20,0.06) 68%,',
            '  transparent        82%)',
          ].join(' '),
        }}
        aria-hidden="true"
      />
      {/* Bottom vignette across all strips */}
      <div
        className="absolute bottom-0 left-0 right-0 pointer-events-none"
        style={{
          height: '35%',
          background: 'linear-gradient(to top, rgba(4,10,20,0.55) 0%, transparent 100%)',
        }}
        aria-hidden="true"
      />

      {/* ── Content — left-aligned, asymmetric ── */}
      <div className="relative z-10 w-full max-w-container-max mx-auto px-gutter pt-14">
        <div style={{ maxWidth: '560px' }}>
          {/* ── BIG title — the dominant visual element ── */}
          <h1
            id="landing-title"
            className="text-white font-black leading-none mb-lg break-words"
            style={{
              fontSize: 'clamp(2.5rem, 8vw, 5.75rem)',
              letterSpacing: '-0.035em',
              lineHeight: 1.05,
            }}
          >
            {t.banner.title}
          </h1>

          {/* Subtitle — two sentences */}
          <p
            className="text-white/65 mb-xl"
            style={{ fontSize: '1.05rem', lineHeight: 1.75 }}
          >
            {t.banner.subtitle}
          </p>

          {/* ── CTA — white button, large, prominent ── */}
          <div className="flex flex-col gap-md">
            <LoginButton
              label={t.banner.cta}
              iconName="login"
              returnTo={`/${lang}/home`}
              className="w-fit bg-white text-gray-900 px-xl py-md rounded-md text-label-lg font-bold shadow-[0_8px_32px_rgba(255,255,255,0.18)] hover:bg-white/90 hover:-translate-y-0.5 hover:shadow-[0_12px_40px_rgba(255,255,255,0.25)] transition-all duration-200"
            />

            {/* FNMT note — warm amber shield */}
            <p
              className="text-white/45 flex items-start gap-xs"
              style={{ fontSize: '0.82rem', lineHeight: 1.6, maxWidth: '46ch' }}
            >
              <Icon name="verified_user" size={15} className="text-amber-300/60 shrink-0 mt-0.5" />
              <span>{withAbbr(t.banner.fnmtNote, ['FNMT'], abbr)}</span>
            </p>
          </div>
        </div>
      </div>

      {/* Hairline bottom border */}
      <div className="absolute bottom-0 left-0 right-0 h-px bg-white/8" aria-hidden="true" />
    </section>
  );
}
