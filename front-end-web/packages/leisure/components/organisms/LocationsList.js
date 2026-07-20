import Icon from '@modelcity/core/components/atoms/Icon';
import CatalogueCard from '@modelcity/core/components/molecules/CatalogueCard';
import CreateCard from '@modelcity/leisure/components/molecules/CreateCard';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';
import { getCityPlaces } from '@modelcity/leisure/lib/api/client';
import { categoryIcon, formatDuration } from '@modelcity/leisure/lib/utils/format';

/**
 * Organism: LocationsList (async)
 *
 * Fetches one page of standalone city places (optionally filtered by category)
 * and renders the card grid with pagination, plus fetch-error and empty states.
 * Staff see a "create location" card in the grid and in the empty state.
 *
 * @param {{
 *   lang: string,
 *   page: number,
 *   activeCategory: string | null,
 *   accessToken: string | undefined,
 *   canCreate: boolean,
 *   t: object,                              // tourism.locations dictionary
 *   units: object,                          // tourism.units dictionary
 *   pageHref: (pageNumber: number) => string,
 * }} props
 */
export default async function LocationsList({ lang, page, activeCategory, accessToken, canCreate, t, units, pageHref }) {
  const data = await getCityPlaces({ page, category: activeCategory }, accessToken);
  const fetchError = !data;
  const places = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  if (fetchError) {
    return <FetchErrorBanner message={t.loadError} />;
  }

  if (places.length === 0) {
    return (
      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
          <Icon name="inbox" size={48} className="opacity-40" />
          <p className="text-body-lg">{t.noResults}</p>
          {canCreate && (
            <CreateCard
              href={`/${lang}/tourism/locations/new`}
              label={t.createNew}
              hint={t.createNewHint}
              className="w-80 max-w-full"
            />
          )}
        </div>
      </div>
    );
  }

  return (
    <section
      aria-labelledby="tourism-locations-heading"
      className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
    >
      <div className="flex items-center gap-sm mb-md">
        <Icon name="place" fill size={20} className="text-secondary" />
        <h2 id="tourism-locations-heading" className="text-h3 text-primary">
          {t.sectionTitle}
        </h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
        {canCreate && (
          <CreateCard
            href={`/${lang}/tourism/locations/new`}
            label={t.createNew}
            hint={t.createNewHint}
          />
        )}
        {places.map((place) => (
          <CatalogueCard
            key={place.id}
            href={`/${lang}/tourism/locations/${place.id}`}
            imageUrl={place.photoUrls?.[0]}
            imageAlt={place.name}
            fallbackIcon={categoryIcon(place.category)}
            title={place.name}
            extras={place.visitDurationMinutes != null ? [{ icon: 'schedule', value: formatDuration(place.visitDurationMinutes, units) }] : []}
            viewDetailsLabel={t.viewDetails}
          />
        ))}
      </div>

      <SectionPager
        currentPage={page}
        totalPages={totalPages}
        hrefBuilder={pageHref}
        labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
        ariaLabel="locations pagination"
      />
    </section>
  );
}
