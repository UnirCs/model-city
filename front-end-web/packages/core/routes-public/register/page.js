import { redirect } from 'next/navigation';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import RegistrationForm from '@modelcity/core/components/organisms/RegistrationForm';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('registration.heading');

// Mandatory SSR: session required on every request
export const dynamic = 'force-dynamic';

/**
 * Registration page — shown to newly authenticated users who have no profile yet.
 *
 * Public route by necessity: it lives in `routes-public/` so its shim is emitted
 * outside the `(app)` group. The `(app)` gate redirects profile-less users *to*
 * `/register`, so this page must NOT sit behind that gate (it would loop). It
 * performs its own lightweight session check instead.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function RegisterPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'registration');
  const t = dict.registration;

  const session = await auth0.getSession();

  // No active session → users should not reach this page directly
  if (!session) redirect(`/${lang}`);

  const { email, name } = session.user;

  return (
    <div className="min-h-screen bg-surface flex flex-col items-center justify-center px-md py-xl">
      {/* Minimal header with brand */}
      <header className="mb-xl text-center">
        <p className="text-label-md text-on-surface-variant tracking-widest uppercase mb-xs">
          {t.brand}
        </p>
        <h1 className="text-h1 text-primary">{t.heading}</h1>
        <p className="text-body-md text-on-surface-variant mt-sm" style={{ maxWidth: '52ch' }}>
          {t.subtitle}
        </p>
      </header>

      {/* Main landmark — SkipLink target (WCAG 2.4.1). */}
      <main
        id="main"
        tabIndex={-1}
        className="w-full bg-surface-container-lowest rounded-md shadow-md p-md focus:outline-none"
        style={{ maxWidth: '480px' }}
      >
        <RegistrationForm
          email={email ?? ''}
          defaultName={name ?? ''}
          dict={t}
          lang={lang}
        />
      </main>

      {/* Footer with privacy policy */}
      <footer className="mt-lg text-caption text-on-surface-variant text-center" style={{ maxWidth: '52ch' }}>
        {t.privacy}{' '}
        <a href="#" className="underline hover:text-primary transition-colors">
          {t.gdpr}
        </a>
        {' '}{t.privacyEnd}
      </footer>
    </div>
  );
}
