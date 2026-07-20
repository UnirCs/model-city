import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getCityRoutes } from '@modelcity/leisure/lib/api/client';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import PageHeader from '@modelcity/core/components/molecules/PageHeader';
import RoutesList from '@modelcity/leisure/components/organisms/RoutesList';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.tourismRoutes');

// Always SSR — paginated data must stay fresh
export const dynamic = 'force-dynamic';

/**
 * Tourism Routes list page — `/{lang}/tourism/routes`
 *
 * Fetches a page of city routes from the leisure microservice and delegates
 * rendering to the {@link RoutesList} organism. Accessible to every
 * authenticated user.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ page?: string }>,
 * }} props
 */
export default async function TourismRoutesPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'tourism');
  const t = dict.tourism.routes;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;
  const canCreate = canManageTourism(session);

  const data = await getCityRoutes({ page }, accessToken);
  const fetchError = !data;
  const routes = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  const pageHref = (n) => `/${lang}/tourism/routes?page=${n}`;

  return (
    <>
      <PageHeader icon="map" title={t.bannerTitle} subtitle={t.bannerSubtitle} />
      <RoutesList
        lang={lang}
        t={t}
        units={dict.tourism.units}
        canCreate={canCreate}
        fetchError={fetchError}
        routes={routes}
        page={page}
        totalPages={totalPages}
        pageHref={pageHref}
      />
    </>
  );
}
