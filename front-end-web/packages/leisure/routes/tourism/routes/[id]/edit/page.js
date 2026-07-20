import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import {
  getAllCityPlaces,
  getCityRoute,
} from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import CityRouteForm from '@modelcity/leisure/components/organisms/CityRouteForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.tourismCreateRoute');

export const dynamic = 'force-dynamic';

/**
 * Edit City Route page — `/{lang}/tourism/routes/{id}/edit`
 *
 * Accessible only to backoffice staff and platform administrators. Loads the
 * existing route together with the catalogue of available places and renders
 * the shared {@link CityRouteForm} in edit mode.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function EditRoutePage({ params }) {
  const { lang, id } = await params;
  const dict = await getDictionary(lang, 'tourism');
  const t        = dict.tourism.routeForm;
  const tDetail  = dict.tourism.routeDetail;

  const session = await auth0.getSession();
  if (!canManageTourism(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const [routeResult, availablePlaces] = await Promise.all([
    getCityRoute(id, accessToken, true),
    getAllCityPlaces(accessToken),
  ]);

  if ('error' in routeResult) {
    return <InlineStatus icon="error_outline" tone="error" message={tDetail.loadError} />;
  }

  if ('notFound' in routeResult) {
    return <InlineStatus icon="search_off" message={tDetail.notFound} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      <IconPageHeader icon="edit_road" title={t.pageTitleEdit} subtitle={t.pageSubtitleEdit} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <CityRouteForm
          mode="edit"
          route={routeResult.data}
          availablePlaces={availablePlaces}
          t={t}
          tCategories={dict.tourism.placeDetail.categoryLabels}
          lang={lang}
        />
      </div>
    </div>
  );
}

