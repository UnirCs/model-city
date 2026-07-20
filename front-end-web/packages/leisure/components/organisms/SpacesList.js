import Icon from '@modelcity/core/components/atoms/Icon';
import CatalogueCard from '@modelcity/core/components/molecules/CatalogueCard';
import CreateCard from '@modelcity/leisure/components/molecules/CreateCard';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';

/**
 * Organism: SpacesList
 *
 * Card grid of public (sports) spaces with pagination, plus fetch-error and
 * empty states. Administrators see a "create space" card in the grid and in
 * the empty state.
 *
 * @param {{
 *   lang: string,
 *   t: object,                              // leisure.sportsSpaces dictionary
 *   canManage: boolean,
 *   fetchError: boolean,
 *   spaces: object[],
 *   page: number,
 *   totalPages: number,
 *   pageHref: (pageNumber: number) => string,
 * }} props
 */
export default function SpacesList({ lang, t, canManage, fetchError, spaces, page, totalPages, pageHref }) {
  const base = `/${lang}/sports-spaces`;

  if (fetchError) {
    return <FetchErrorBanner message={t.loadError} />;
  }

  if (spaces.length === 0) {
    return (
      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
          <Icon name="inbox" size={48} className="opacity-40" />
          <p className="text-body-lg">{t.noResults}</p>
          {canManage && (
            <CreateCard href={`${base}/new`} label={t.createSpace} className="w-full max-w-xs" />
          )}
        </div>
      </div>
    );
  }

  return (
    <section
      aria-labelledby="sports-spaces-heading"
      className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
    >
      <div className="flex items-center gap-sm mb-md">
        <Icon name="place" fill size={20} className="text-secondary" />
        <h2 id="sports-spaces-heading" className="text-h3 text-primary">
          {t.sectionTitle}
        </h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
        {canManage && (
          <CreateCard href={`${base}/new`} label={t.createSpace} />
        )}
        {spaces.map((space) => (
          <CatalogueCard
            key={space.id}
            href={`/${lang}/sports-spaces/${space.id}`}
            imageUrl={space.photoUrl}
            imageAlt={space.name}
            fallbackIcon="stadium"
            title={space.name}
            extras={space.address ? [{ icon: 'location_on', value: space.address }] : []}
            viewDetailsLabel={t.viewDetails}
          />
        ))}
      </div>

      <SectionPager
        currentPage={page}
        totalPages={totalPages}
        hrefBuilder={pageHref}
        labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
        ariaLabel="sports spaces pagination"
      />
    </section>
  );
}
