import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getUserCars } from '@modelcity/mobility/lib/api/client';
import Icon from '@modelcity/core/components/atoms/Icon';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import LocalizedPager from '@modelcity/core/components/molecules/LocalizedPager';
import CarsPanel from '@modelcity/mobility/components/organisms/CarsPanel';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobilityCars');

export const dynamic = 'force-dynamic';

/**
 * Citizen page — `/{lang}/mobility/cars`
 *
 * Lists the cars registered by the current user and exposes a CTA to add
 * new ones through the {@link AddCarModal}.
 *
 * @param {{ params: Promise<{ lang: string }>, searchParams: Promise<{ page?: string }> }} props
 */
export default async function MyCarsPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.cars;

  const session = await auth0.getSession();
  const sub = session?.user?.sub;
  const accessToken = session?.tokenSet?.accessToken;

  const data = sub ? await getUserCars(sub, { page }, accessToken) : null;
  const fetchError = data == null && sub != null;
  const safeCars = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  return (
    <div className="space-y-lg">
      <IconPageHeader icon="directions_car" title={t.bannerTitle} subtitle={t.bannerSubtitle} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="flex items-center gap-sm mb-md">
          <Icon name="directions_car" fill size={20} className="text-secondary" />
          <h2 className="text-h3 text-primary">{t.sectionTitle}</h2>
        </div>
        {fetchError ? (
          <FetchErrorBanner message={t.loadError} bare />
        ) : (
          <>
            <CarsPanel cars={safeCars} labels={t} lang={lang} />
            <LocalizedPager
              basePath="/mobility/cars"
              page={page}
              totalPages={totalPages}
              labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
              ariaLabel="cars pagination"
            />
          </>
        )}
      </div>
    </div>
  );
}
