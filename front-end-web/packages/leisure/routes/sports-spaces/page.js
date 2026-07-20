import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getPublicSpaces } from '@modelcity/leisure/lib/api/client';
import { canManagePublicSpaces } from '@modelcity/leisure/lib/auth/roles';
import PageHeader from '@modelcity/core/components/molecules/PageHeader';
import SpacesList from '@modelcity/leisure/components/organisms/SpacesList';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.bookingsFacilities');

// Always SSR — paginated data must stay fresh.
export const dynamic = 'force-dynamic';

/**
 * Sports spaces list page — `/{lang}/sports-spaces`
 *
 * Fetches a page of active public spaces from the leisure microservice and
 * delegates rendering to the {@link SpacesList} organism. Gated by the parent
 * `(app)` layout; administrators additionally see a "create space" CTA.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ page?: string }>,
 * }} props
 */
export default async function SportsSpacesPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.sportsSpaces;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;
  const canManage = canManagePublicSpaces(session);

  const data = await getPublicSpaces({ page }, accessToken);
  const fetchError = !data;
  const spaces = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  const pageHref = (n) => `/${lang}/sports-spaces?page=${n}`;

  return (
    <>
      <PageHeader icon="stadium" title={t.bannerTitle} subtitle={t.bannerSubtitle} className="mb-xl" />
      <SpacesList
        lang={lang}
        t={t}
        canManage={canManage}
        fetchError={fetchError}
        spaces={spaces}
        page={page}
        totalPages={totalPages}
        pageHref={pageHref}
      />
    </>
  );
}
