import Icon from '@modelcity/core/components/atoms/Icon';
import CatalogueCard from '@modelcity/core/components/molecules/CatalogueCard';
import CreateCard from '@modelcity/leisure/components/molecules/CreateCard';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';
import { formatDuration } from '@modelcity/leisure/lib/utils/format';

/**
 * Organism: RoutesList
 *
 * Card grid of city routes with pagination, plus fetch-error and empty states.
 * Staff see a "create route" card as the first item in the grid.
 *
 * @param {{
 *   lang: string,
 *   t: object,                              // tourism.routes dictionary
 *   units: object,                          // tourism.units dictionary
 *   canCreate: boolean,
 *   fetchError: boolean,
 *   routes: object[],
 *   page: number,
 *   totalPages: number,
 *   pageHref: (pageNumber: number) => string,
 * }} props
 */
export default function RoutesList({ lang, t, units, canCreate, fetchError, routes, page, totalPages, pageHref }) {
  if (fetchError) {
    return <FetchErrorBanner message={t.loadError} />;
  }

  if (routes.length === 0 && !canCreate) {
    return (
      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
          <Icon name="inbox" size={48} className="opacity-40" />
          <p className="text-body-lg">{t.noResults}</p>
        </div>
      </div>
    );
  }

  return (
    <section
      aria-labelledby="tourism-routes-heading"
      className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
    >
      <div className="flex items-center gap-sm mb-md">
        <Icon name="explore" fill size={20} className="text-secondary" />
        <h2 id="tourism-routes-heading" className="text-h3 text-primary">
          {t.sectionTitle}
        </h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
        {canCreate && (
          <CreateCard
            href={`/${lang}/tourism/routes/new`}
            label={t.createNew}
            hint={t.createNewHint}
          />
        )}
        {routes.map((route) => (
          <CatalogueCard
            key={route.id}
            href={`/${lang}/tourism/routes/${route.id}`}
            imageUrl={route.imageUrl}
            imageAlt={route.name}
            fallbackIcon="map"
            title={route.name}
            extras={[
              { icon: 'location_on', value: t.placeCount.replace('{count}', String(route.placeCount)) },
              { icon: 'schedule',    value: formatDuration(route.estimatedDurationMinutes, units) },
            ]}
            viewDetailsLabel={t.viewDetails}
          />
        ))}
      </div>

      <SectionPager
        currentPage={page}
        totalPages={totalPages}
        hrefBuilder={pageHref}
        labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
        ariaLabel="routes pagination"
      />
    </section>
  );
}
