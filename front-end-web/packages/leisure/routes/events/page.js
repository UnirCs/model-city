import { Suspense } from 'react';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageEvents } from '@modelcity/leisure/lib/auth/roles';
import PageHeader from '@modelcity/core/components/molecules/PageHeader';
import { ListSkeleton } from '@modelcity/core/components/molecules/skeletons';
import EventFilters from '@modelcity/leisure/components/organisms/EventFilters';
import EventsList from '@modelcity/leisure/components/organisms/EventsList';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.eventsAgenda');

export const dynamic = 'force-dynamic';

const EVENT_TYPES = [
  'MUSIC', 'NIGHTLIFE', 'PERFORMING_ARTS', 'HOBBIES',
  'BUSINESS', 'FOOD_AND_DRINK', 'OTHER',
];

const VALID_TYPES = new Set(EVENT_TYPES);

/**
 * Events catalogue — `/{lang}/events`.
 *
 * Parses the type / paid filters and current page, then renders the header and
 * filter controls immediately while the {@link EventsList} organism streams in
 * under a `<Suspense>` boundary.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ page?: string, eventType?: string, paid?: string }>,
 * }} props
 */
export default async function EventsPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const eventType = sp.eventType && VALID_TYPES.has(sp.eventType) ? sp.eventType : null;
  const paid = sp.paid === 'true' ? true : sp.paid === 'false' ? false : null;

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.events;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;
  const canManage = canManageEvents(session);

  const baseQs = new URLSearchParams();
  if (eventType) baseQs.set('eventType', eventType);
  if (paid != null) baseQs.set('paid', String(paid));
  const pageHref = (n) => {
    const qs = new URLSearchParams(baseQs);
    qs.set('page', String(n));
    return `/${lang}/events?${qs.toString()}`;
  };

  return (
    <>
      <PageHeader icon="celebration" title={t.bannerTitle} subtitle={t.bannerSubtitle} />

      <EventFilters
        lang={lang}
        tFilters={dict.leisure.eventFilters}
        typeLabels={dict.leisure.eventTypes}
        eventTypes={EVENT_TYPES}
        eventType={eventType}
        paid={paid}
      />

      <Suspense key={`${eventType}-${paid}`} fallback={<ListSkeleton count={6} contained />}>
        <EventsList
          lang={lang}
          page={page}
          eventType={eventType}
          paid={paid}
          accessToken={accessToken}
          canManage={canManage}
          t={t}
          pageHref={pageHref}
        />
      </Suspense>
    </>
  );
}
