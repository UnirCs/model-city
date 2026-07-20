import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageEventTickets } from '@modelcity/leisure/lib/auth/roles';
import { getEvent, getEventTickets } from '@modelcity/leisure/lib/api/client';
import Icon from '@modelcity/core/components/atoms/Icon';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';
import EventTicketsTable from '@modelcity/leisure/components/organisms/EventTicketsTable';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.eventsTickets');

export const dynamic = 'force-dynamic';

/**
 * Backoffice: tickets sold for an event — `/{lang}/events/{id}/tickets`.
 *
 * @param {{
 *   params: Promise<{ lang: string, id: string }>,
 *   searchParams: Promise<{ page?: string }>,
 * }} props
 */
export default async function EventTicketsPage({ params, searchParams }) {
  const { lang, id } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.eventTickets;

  const session = await auth0.getSession();
  if (!canManageEventTickets(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const [evRes, data] = await Promise.all([
    getEvent(id, accessToken),
    getEventTickets(id, { page }, accessToken),
  ]);

  const eventName = 'data' in evRes ? evRes.data.name : '';
  const tickets = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  return (
    <div className="space-y-lg">
      <BackButton />

      <IconPageHeader icon="receipt_long" title={t.pageTitle} subtitle={eventName || undefined} />

      {!data ? (
        <div className="p-md bg-error-container text-on-error-container rounded-md flex items-center gap-sm">
          <Icon name="error_outline" size={20} />
          <span className="text-body-md">{t.loadError}</span>
        </div>
      ) : (
        <div className="pb-12">
          <EventTicketsTable
            eventId={id}
            tickets={tickets}
            t={t}
            statusLabels={dict.leisure.ticketStatuses}
            refundLabels={dict.leisure.refundDialog}
            lang={lang}
          />

          <SectionPager
            currentPage={page}
            totalPages={totalPages}
            hrefBuilder={(n) => `/${lang}/events/${id}/tickets?page=${n}`}
            labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
            ariaLabel="event tickets pagination"
            className=""
          />
        </div>
      )}
    </div>
  );
}
