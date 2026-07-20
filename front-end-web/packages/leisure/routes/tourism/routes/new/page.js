import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import { getAllCityPlaces } from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import Icon from '@modelcity/core/components/atoms/Icon';
import CityRouteForm from '@modelcity/leisure/components/organisms/CityRouteForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.tourismCreateRoute');

export const dynamic = 'force-dynamic';

/**
 * Create City Route page — `/{lang}/tourism/routes/new`
 *
 * Accessible only to backoffice staff and platform administrators. Renders
 * the shared {@link CityRouteForm} in create mode and pre-fetches every city
 * place from the leisure microservice to populate the picker.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function CreateRoutePage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'tourism');
  const t = dict.tourism.routeForm;

  const session = await auth0.getSession();
  if (!canManageTourism(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const availablePlaces = await getAllCityPlaces(accessToken);

  return (
    <div className="space-y-lg">
      <BackButton />

      <div className="flex items-center gap-sm">
        <div className="w-10 h-10 rounded-md bg-primary/10 flex items-center justify-center shrink-0">
          <Icon name="add_road" fill size={24} className="text-primary" />
        </div>
        <div>
          <h1 className="text-h2 text-primary font-semibold">{t.pageTitleCreate}</h1>
          <p className="text-body-sm text-on-surface-variant">{t.pageSubtitleCreate}</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <CityRouteForm
          mode="create"
          availablePlaces={availablePlaces}
          t={t}
          tCategories={dict.tourism.placeDetail.categoryLabels}
          lang={lang}
        />
      </div>
    </div>
  );
}

