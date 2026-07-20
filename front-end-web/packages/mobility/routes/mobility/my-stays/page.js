import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getUserStreetReservations } from '@modelcity/mobility/lib/api/client';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import LocalizedPager from '@modelcity/core/components/molecules/LocalizedPager';
import StaysPanel from '@modelcity/mobility/components/organisms/StaysPanel';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobilityMyStays');

export const dynamic = 'force-dynamic';

/**
 * Citizen page — `/{lang}/mobility/my-stays`
 *
 * Displays the user's active and recent (last 30 days) SER zone tickets.
 * Active tickets expose a renewal CTA driven by the {@link RenewStayModal}.
 *
 * @param {{ params: Promise<{ lang: string }>, searchParams: Promise<{ page?: string }> }} props
 */
export default async function MyStaysPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.stays;

  const session = await auth0.getSession();
  const sub = session?.user?.sub;
  const accessToken = session?.tokenSet?.accessToken;

  const data = sub ? await getUserStreetReservations(sub, { page }, accessToken) : null;
  const fetchError = data == null && sub != null;
  const safe = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  return (
    <div className="space-y-lg">
      <IconPageHeader icon="receipt_long" title={t.bannerTitle} subtitle={t.bannerSubtitle} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        {fetchError ? (
          <FetchErrorBanner message={t.loadError} bare />
        ) : (
          <>
            <StaysPanel reservations={safe} labels={t} renewLabels={dict.mobility.renewForm} lang={lang} />
            <LocalizedPager
              basePath="/mobility/my-stays"
              page={page}
              totalPages={totalPages}
              labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
              ariaLabel="stays pagination"
            />
          </>
        )}
      </div>
    </div>
  );
}
