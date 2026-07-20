import Link from 'next/link';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getCityPlace } from '@modelcity/leisure/lib/api/client';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import Icon from '@modelcity/core/components/atoms/Icon';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import PlaceDetailView from '@modelcity/leisure/components/organisms/PlaceDetailView';
import DeleteEntityButton from '@modelcity/leisure/components/molecules/DeleteEntityButton';
import AdminSection from '@modelcity/core/components/molecules/AdminSection';
import { removeCityPlace } from '@modelcity/leisure/lib/actions/cityPlaces';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.tourismRoutes');

export const dynamic = 'force-dynamic';

/**
 * City place detail page —
 *   `/{lang}/tourism/routes/{id}/places/{placeId}`
 *
 * Thin wrapper that fetches the route-scoped city-place payload and delegates
 * the rendering to the shared `PlaceDetailView` organism (same payload shape
 * as the standalone `/tourism/locations/{id}` endpoint).
 *
 * @param {{ params: Promise<{ lang: string, id: string, placeId: string }> }} props
 */
export default async function PlaceDetailPage({ params }) {
  const { lang, id, placeId } = await params;
  const dict = await getDictionary(lang, 'tourism');
  const t       = dict.tourism.placeDetail;
  const units   = dict.tourism.units;
  const tAdmin  = dict.tourism.adminActions;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;
  const canManage   = canManageTourism(session);
  const result = await getCityPlace(id, placeId, accessToken);

  if ('error' in result) {
    return <InlineStatus icon="error_outline" tone="error" message={t.loadError} />;
  }

  if ('notFound' in result) {
    return <InlineStatus icon="search_off" message={t.notFound} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      <PlaceDetailView
        place={result.data}
        t={t}
        units={units}
        adminSlot={canManage ? (
          <AdminSection title={tAdmin.sectionTitle}>
            <Link
              href={`/${lang}/tourism/locations/${result.data.id}/edit`}
              className="inline-flex items-center justify-center gap-sm px-md py-sm rounded-md border border-primary text-primary bg-surface text-label-md font-semibold hover:bg-primary/5 transition-all active:scale-95 w-full"
            >
              <Icon name="edit" size={18} />
              {tAdmin.editPlace}
            </Link>
            <DeleteEntityButton
              id={result.data.id}
              action={removeCityPlace}
              onSuccessHref={`/${lang}/tourism/routes/${id}`}
              className="w-full"
              labels={{
                button:       tAdmin.deletePlace,
                confirmTitle: tAdmin.deletePlaceConfirmTitle,
                confirmBody:  tAdmin.deletePlaceConfirmBody,
                confirm:      tAdmin.confirm,
                cancel:       tAdmin.cancel,
                deleting:     tAdmin.deleting,
                error:        tAdmin.deleteError,
              }}
            />
          </AdminSection>
        ) : null}
      />
    </div>
  );
}
