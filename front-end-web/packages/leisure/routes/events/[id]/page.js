import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import {
  getEvent,
  getStandaloneCityPlace,
} from '@modelcity/leisure/lib/api/client';
import { canManageEvents, canDeleteEvents, canManageEventTickets, canBuyEventTickets } from '@modelcity/leisure/lib/auth/roles';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import EventDetailView from '@modelcity/leisure/components/organisms/EventDetailView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.events');

export const dynamic = 'force-dynamic';

/**
 * Event detail page — `/{lang}/events/{id}`.
 *
 * Fetches the event (and its bound city place, soft-failing) from the leisure
 * microservice and delegates rendering to the {@link EventDetailView} organism.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function EventDetailPage({ params }) {
  const { lang, id } = await params;

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.eventDetail;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;

  const result = await getEvent(id, accessToken);

  if ('error' in result) {
    return <InlineStatus icon="error_outline" tone="error" message={t.loadError} />;
  }
  if ('notFound' in result) {
    return <InlineStatus icon="search_off" message={t.notFound} />;
  }

  const event = result.data;
  // Resolve the bound place to show its name + "view more" link. Soft-fail
  // because the event detail must still render even if the place call fails.
  const placeRes = await getStandaloneCityPlace(event.placeId, accessToken);
  const place = 'data' in placeRes ? placeRes.data : null;

  return (
    <div className="space-y-lg">
      <BackButton />
      <EventDetailView
        event={event}
        place={place}
        t={t}
        tAdmin={dict.leisure.eventAdmin}
        tBuy={dict.leisure.buyTicket}
        typeLabels={dict.leisure.eventTypes}
        lang={lang}
        canEdit={canManageEvents(session)}
        canDelete={canDeleteEvents(session)}
        canViewTickets={canManageEventTickets(session)}
        canBuy={canBuyEventTickets(session)}
      />
    </div>
  );
}
