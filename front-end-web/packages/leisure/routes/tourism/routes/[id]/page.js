import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getCityRoute, getCityRouteCityPlaces } from '@modelcity/leisure/lib/api/client';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import RouteDetailView from '@modelcity/leisure/components/organisms/RouteDetailView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.tourismRoutes');

export const dynamic = 'force-dynamic';

/**
 * City route detail page — `/{lang}/tourism/routes/{id}`
 *
 * Fetches the route and its (paginated) points of interest from the leisure
 * microservice and delegates rendering to the {@link RouteDetailView} organism.
 *
 * @param {{ params: Promise<{ lang: string, id: string }>, searchParams: Promise<{ page?: string }> }} props
 */
export default async function RouteDetailPage({ params, searchParams }) {
  const { lang, id } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const dict = await getDictionary(lang, 'tourism');
  const t = dict.tourism.routeDetail;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;

  const [result, placesData] = await Promise.all([
    getCityRoute(id, accessToken),
    getCityRouteCityPlaces(id, { page }, accessToken),
  ]);

  if ('error' in result) {
    return <InlineStatus icon="error_outline" tone="error" message={t.loadError} />;
  }
  if ('notFound' in result) {
    return <InlineStatus icon="search_off" message={t.notFound} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />
      <RouteDetailView
        route={result.data}
        places={placesData?.content ?? []}
        routeId={id}
        page={page}
        totalPages={placesData?.page?.totalPages ?? 1}
        t={t}
        units={dict.tourism.units}
        tAdmin={dict.tourism.adminActions}
        categoryLabels={dict.tourism.placeDetail.categoryLabels}
        lang={lang}
        canManage={canManageTourism(session)}
      />
    </div>
  );
}
