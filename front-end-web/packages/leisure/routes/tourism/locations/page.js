import { Suspense } from 'react';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import PageHeader from '@modelcity/core/components/molecules/PageHeader';
import { ListSkeleton } from '@modelcity/core/components/molecules/skeletons';
import LocationFilters from '@modelcity/leisure/components/organisms/LocationFilters';
import LocationsList from '@modelcity/leisure/components/organisms/LocationsList';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.tourismLocations');

// Always SSR — paginated + filtered data must stay fresh
export const dynamic = 'force-dynamic';

/**
 * Tourism Locations list page — `/{lang}/tourism/locations`
 *
 * Parses the category filter and current page, renders the header and filter
 * immediately, and streams the {@link LocationsList} organism under a
 * `<Suspense>` boundary. Accessible to every authenticated user.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ page?: string, category?: string }>,
 * }} props
 */
export default async function TourismLocationsPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const activeCategory = sp.category ?? null;

  const dict  = await getDictionary(lang, 'tourism');
  const t     = dict.tourism.locations;
  const units = dict.tourism.units;
  const categoryLabels = dict.tourism.placeDetail.categoryLabels;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;
  const canCreate = canManageTourism(session);

  const base = `/${lang}/tourism/locations`;
  const pageHref = (n) => {
    const qs = new URLSearchParams();
    if (activeCategory) qs.set('category', activeCategory);
    qs.set('page', String(n));
    return `${base}?${qs.toString()}`;
  };

  return (
    <>
      <PageHeader icon="explore" title={t.bannerTitle} subtitle={t.bannerSubtitle} />

      <LocationFilters
        lang={lang}
        t={t}
        categoryLabels={categoryLabels}
        activeCategory={activeCategory}
      />

      <Suspense key={activeCategory} fallback={<ListSkeleton count={6} contained />}>
        <LocationsList
          lang={lang}
          page={page}
          activeCategory={activeCategory}
          accessToken={accessToken}
          canCreate={canCreate}
          t={t}
          units={units}
          pageHref={pageHref}
        />
      </Suspense>
    </>
  );
}
