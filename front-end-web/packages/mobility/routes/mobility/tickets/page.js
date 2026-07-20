import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canViewMobilityOps } from '@modelcity/mobility/lib/auth/roles';
import { listStreetReservations } from '@modelcity/mobility/lib/api/client';
import { dateToIso } from '@modelcity/mobility/lib/utils/format';
import Icon from '@modelcity/core/components/atoms/Icon';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import MobilityFilters from '@modelcity/mobility/components/molecules/MobilityFilters';
import PaginationBar from '@modelcity/mobility/components/molecules/PaginationBar';
import TicketsTable from '@modelcity/mobility/components/organisms/TicketsTable';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobilityTickets');

export const dynamic = 'force-dynamic';

/**
 * Staff page — `/{lang}/mobility/tickets`
 *
 * Operational table of street reservations (active stays). Supports filtering
 * by license plate, date range and active/expired status. Restricted to
 * field operators and platform administrators.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{
 *     licensePlate?: string,
 *     fromDate?: string,
 *     toDate?: string,
 *     active?: string,
 *     page?: string,
 *   }>,
 * }} props
 */
export default async function ActiveStaysPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = (await searchParams) ?? {};
  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.tickets;

  const session = await auth0.getSession();
  if (!canViewMobilityOps(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;

  const page = Number(sp.page ?? 0) || 0;
  const activeParam =
    sp.active === 'true' ? true : sp.active === 'false' ? false : undefined;

  const result = await listStreetReservations(
    {
      licensePlate: sp.licensePlate?.trim() || undefined,
      from: dateToIso(sp.fromDate, 'start') ?? undefined,
      to:   dateToIso(sp.toDate,   'end')   ?? undefined,
      active: activeParam,
      page,
    },
    accessToken,
  );

  const fetchError = result == null;
  const content    = result?.content ?? [];
  const totalPages = result?.page?.totalPages ?? 0;

  return (
    <div className="space-y-lg">
      <IconPageHeader icon="receipt_long" title={t.bannerTitle} subtitle={t.bannerSubtitle} />

      <MobilityFilters
        labels={t}
        showActive
        initial={{
          licensePlate: sp.licensePlate ?? '',
          fromDate: sp.fromDate ?? '',
          toDate: sp.toDate ?? '',
          active: sp.active ?? '',
        }}
      />

      {fetchError && <FetchErrorBanner message={t.loadError} />}

      {!fetchError && (
        <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
          <div className="flex items-center gap-sm mb-md">
            <Icon name="local_parking" fill size={20} className="text-secondary" />
            <h2 className="text-h3 text-primary">{t.sectionTitle}</h2>
          </div>
          <TicketsTable reservations={content} labels={t} lang={lang} />
          <PaginationBar page={page} totalPages={totalPages} labels={t} />
        </div>
      )}
    </div>
  );
}

