import { auth0 } from '@modelcity/core/lib/auth/auth0';
import ServiceGrid from '@modelcity/core/components/organisms/ServiceGrid';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getHomeServices } from '@modelcity/core/lib/config/services';
import { isPathEnabled } from '@modelcity/core/lib/config/modules';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.home');

// SSR: needs session data on every request
export const dynamic = 'force-dynamic';

/**
 * Home / Dashboard page.
 * Auth check and AppShell are handled by the parent (app)/layout.js.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function HomePage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'home');
  const t = dict.home;

  // Session is guaranteed by the layout; we call it again only to read user data.
  // auth0.getSession() is cached per-request so there is no extra overhead.
  const session = await auth0.getSession();
  const firstName = session?.user?.given_name
    ?? session?.user?.name?.split(' ')[0]
    ?? '…';

  // Keep only services whose feature module is enabled, then localize hrefs
  // and resolve their text from the dictionary (home.services[id]).
  const serviceText = t.services ?? {};
  const localizedServices = getHomeServices()
    .filter((service) => isPathEnabled(service.href))
    .map((service) => ({
      ...service,
      title: serviceText[service.id]?.title ?? service.id,
      description: serviceText[service.id]?.description ?? '',
      href: `/${lang}${service.href}`,
    }));

  return (
    <>
      {/* ── Welcome ─────────────────────────────── */}
      <section className="mb-xl">
        <h1 className="text-h1 text-primary mb-sm">
          {t.welcome}, {firstName}
        </h1>
        <p className="text-body-lg text-on-surface-variant">
          {t.subtitle}
        </p>
      </section>

      {/* ── Municipal services ──────────────────── */}
      <section className="mb-xl">
        <h2 className="text-h2 text-primary mb-md">{t.servicesTitle}</h2>
        <ServiceGrid services={localizedServices} />
      </section>

    </>
  );
}

