import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import {
  getPublicSpace,
  getPublicSpaceResources,
  getResourceReservations,
} from '@modelcity/leisure/lib/api/client';
import { canBookReservation, canManageSpaceResources } from '@modelcity/leisure/lib/auth/roles';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import ResourceReservationsPanel from '@modelcity/leisure/components/organisms/ResourceReservationsPanel';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.bookingsFacilities');

export const dynamic = 'force-dynamic';

/** Returns tomorrow's date as ISO `YYYY-MM-DD`. */
function tomorrowIso() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/** Validates an ISO `YYYY-MM-DD` string. */
function isValidIsoDate(value) {
  return typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value);
}

/**
 * Resource reservations page —
 * `/{lang}/sports-spaces/{id}/resources/{rid}`
 *
 * Shows the reservations of the resource on the selected date. Citizens see
 * the slot booking form on top; operators/admins see citizen info and can
 * delete reservations. The date is controlled via the `?date=` query string.
 *
 * @param {{
 *   params: Promise<{ lang: string, id: string, rid: string }>,
 *   searchParams: Promise<{ date?: string }>,
 * }} props
 */
export default async function ResourceReservationsPage({ params, searchParams }) {
  const { lang, id, rid } = await params;
  const sp = await searchParams;
  // Default to tomorrow so the citizen sees a valid bookable date on first load.
  const date = isValidIsoDate(sp.date) ? sp.date : tomorrowIso();

  const dict = await getDictionary(lang, 'leisure');
  const t      = dict.leisure.reservations;
  const tForm  = dict.leisure.reservationForm;
  const tSpace = dict.leisure.spaceDetail;
  const tTypes = dict.leisure.resourceTypes;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;
  const canManage = canManageSpaceResources(session);
  const canBook   = canBookReservation(session);

  // Parallel fetches: space metadata, resource lookup and reservations.
  const [spaceResult, rawResources, rawReservations] = await Promise.all([
    getPublicSpace(id, accessToken),
    getPublicSpaceResources(id, accessToken),
    getResourceReservations(id, rid, date, accessToken),
  ]);
  const resources = rawResources?.content ?? rawResources ?? [];
  const reservations = rawReservations?.content ?? rawReservations ?? [];

  if ('error' in spaceResult || 'notFound' in spaceResult) {
    return <InlineStatus icon="search_off" message={tSpace.notFound} />;
  }

  const resource = (resources ?? []).find((r) => String(r.id) === String(rid));
  if (!resource) {
    return <InlineStatus icon="search_off" message={tSpace.notFound} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      <ResourceReservationsPanel
        spaceId={id}
        resourceId={rid}
        date={date}
        reservations={reservations}
        canManage={canManage}
        canBook={canBook}
        t={t}
        tForm={tForm}
        spaceName={spaceResult.data.name}
        resourceName={resource.name}
        resourceType={resource.resourceType}
        resourceDescription={resource.description}
        tTypes={tTypes}
      />
    </div>
  );
}







