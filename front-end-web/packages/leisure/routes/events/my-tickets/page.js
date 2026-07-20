import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canBuyEventTickets } from '@modelcity/leisure/lib/auth/roles';
import { getUserTickets } from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import MyTicketsView from '@modelcity/leisure/components/organisms/MyTicketsView';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.eventsTickets');

export const dynamic = 'force-dynamic';

/** Backend `period` values, one per section */
const PERIOD = {
  thisWeek: 'week',
  future: 'future',
  past: 'past',
};

/**
 * Citizen's own tickets — `/{lang}/events/my-tickets`.
 *
 * Fetches three independently-paginated sections (this week / upcoming / past)
 * from the leisure microservice and delegates rendering to the
 * {@link MyTicketsView} organism.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ weekPage?: string, futurePage?: string, pastPage?: string }>,
 * }} props
 */
export default async function MyTicketsPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const weekPage = Math.max(0, parseInt(sp.weekPage ?? '0', 10) || 0);
  const futurePage = Math.max(0, parseInt(sp.futurePage ?? '0', 10) || 0);
  const pastPage = Math.max(0, parseInt(sp.pastPage ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.myTickets;

  const session = await auth0.getSession();
  if (!canBuyEventTickets(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const userId = session?.user?.sub;

  const [weekData, futureData, pastData] = userId
    ? await Promise.all([
        getUserTickets(userId, { page: weekPage, period: PERIOD.thisWeek }, accessToken),
        getUserTickets(userId, { page: futurePage, period: PERIOD.future }, accessToken),
        getUserTickets(userId, { page: pastPage, period: PERIOD.past }, accessToken),
      ])
    : [null, null, null];

  const fetchError = !weekData || !futureData || !pastData;

  const sections = {
    week: {
      tickets: weekData?.content ?? [],
      currentPage: weekPage,
      totalPages: weekData?.page?.totalPages ?? 1,
    },
    future: {
      tickets: futureData?.content ?? [],
      currentPage: futurePage,
      totalPages: futureData?.page?.totalPages ?? 1,
    },
    past: {
      tickets: pastData?.content ?? [],
      currentPage: pastPage,
      totalPages: pastData?.page?.totalPages ?? 1,
    },
  };

  const isEmpty =
    !fetchError &&
    sections.week.tickets.length === 0 &&
    sections.future.tickets.length === 0 &&
    sections.past.tickets.length === 0;

  return (
    <div className="space-y-lg">
      <BackButton />
      <MyTicketsView
        lang={lang}
        t={t}
        tBuy={dict.leisure.buyTicket}
        fetchError={fetchError}
        isEmpty={isEmpty}
        sections={sections}
      />
    </div>
  );
}
