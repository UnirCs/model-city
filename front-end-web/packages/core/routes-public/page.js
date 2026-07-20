import { redirect } from 'next/navigation';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import ThemeToggle from '@modelcity/core/components/molecules/ThemeToggle';
import LanguageSwitcher from '@modelcity/core/components/molecules/LanguageSwitcher';
import LandingBanner from '@modelcity/core/components/organisms/LandingBanner';
import LandingServices from '@modelcity/core/components/organisms/LandingServices';
import LandingEasyRead from '@modelcity/core/components/organisms/LandingEasyRead';
import LandingFooter from '@modelcity/core/components/organisms/LandingFooter';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('landing.banner.title', { path: '' });

// SSR to detect the Auth0 session
export const dynamic = 'force-dynamic';

/**
 * Landing page — shown to unauthenticated visitors. Authenticated users are
 * redirected to the dashboard. Composes the floating header with the banner,
 * services strip, easy-read summary and footer organisms.
 *
 * Public route: lives in `routes-public/`, so the codegen emits its shim at the
 * bare `[lang]/page.js` (outside the `(app)` session gate).
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function LandingPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'landing');
  const t = dict.landing;

  // If the user already has an active session → redirect to dashboard
  const session = await auth0.getSession();
  if (session) redirect(`/${lang}/home`);

  return (
    <div className="flex flex-col min-h-screen bg-background">
      {/* ── Header — floating over the photo ── */}
      <header className="fixed top-0 w-full z-50 bg-black/20 backdrop-blur-md border-b border-white/10">
        <div className="max-w-container-max mx-auto px-gutter flex justify-between items-center h-14">
          <div className="flex items-center gap-sm ml-auto">
            <LanguageSwitcher variant="landing" />
            <ThemeToggle className="text-white/70 hover:text-white hover:bg-white/10" />
          </div>
        </div>
      </header>

      <main id="main" tabIndex={-1} className="flex-1 flex flex-col focus:outline-none">
        <LandingBanner t={t} lang={lang} abbr={dict.a11y.abbr} />
        <LandingServices t={t} />
        <LandingEasyRead t={t} />
      </main>

      <LandingFooter t={t} lang={lang} brand={dict.common.brand} />
    </div>
  );
}
