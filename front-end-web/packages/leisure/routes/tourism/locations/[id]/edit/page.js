import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import { getStandaloneCityPlace } from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import CityPlaceForm from '@modelcity/leisure/components/organisms/CityPlaceForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.tourismCreateLocation');

export const dynamic = 'force-dynamic';

/**
 * Edit City Place page — `/{lang}/tourism/locations/{id}/edit`
 *
 * Accessible only to backoffice staff and platform administrators. Loads the
 * existing place and renders the shared {@link CityPlaceForm} in edit mode.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function EditLocationPage({ params }) {
  const { lang, id } = await params;
  const dict = await getDictionary(lang, 'tourism');
  const t        = dict.tourism.placeForm;
  const tDetail  = dict.tourism.placeDetail;

  const session = await auth0.getSession();
  if (!canManageTourism(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await getStandaloneCityPlace(id, accessToken, true);

  if ('error' in result) {
    return <InlineStatus icon="error_outline" tone="error" message={tDetail.loadError} />;
  }

  if ('notFound' in result) {
    return <InlineStatus icon="search_off" message={tDetail.notFound} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      <IconPageHeader icon="edit_location" title={t.pageTitleEdit} subtitle={t.pageSubtitleEdit} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <CityPlaceForm
          mode="edit"
          place={result.data}
          t={t}
          tCategories={dict.tourism.placeDetail.categoryLabels}
          lang={lang}
        />
      </div>
    </div>
  );
}

