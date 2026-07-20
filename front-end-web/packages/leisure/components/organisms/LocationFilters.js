import FilterSection from '@modelcity/leisure/components/molecules/FilterSection';
import FilterChips from '@modelcity/leisure/components/molecules/FilterChips';
import { categoryIcon } from '@modelcity/leisure/lib/utils/format';

/**
 * Organism: LocationFilters
 *
 * Single-select city-place category filter. Each chip links to the locations
 * list with the matching `?category=` query (selecting the active one clears it).
 *
 * @param {{
 *   lang: string,
 *   t: object,                  // tourism.locations dictionary
 *   categoryLabels: object,     // tourism.placeDetail.categoryLabels dictionary
 *   activeCategory: string | null,
 * }} props
 */
export default function LocationFilters({ lang, t, categoryLabels, activeCategory }) {
  const base = `/${lang}/tourism/locations`;
  const filterHref = (cat) => {
    const qs = new URLSearchParams();
    if (cat) qs.set('category', cat);
    const str = qs.toString();
    return str ? `${base}?${str}` : base;
  };
  const categoryCodes = Object.keys(categoryLabels);

  return (
    <FilterSection title={t.filterTitle} ariaLabel="locations category filter" className="mb-lg">
      <div>
        <p className="text-caption text-on-surface-variant mb-sm">{t.typeLabel}</p>
        <FilterChips
          options={[
            { value: null, label: t.filterAll, icon: null },
            ...categoryCodes.map((code) => ({
              value: code,
              label: categoryLabels[code],
              icon: categoryIcon(code),
            })),
          ]}
          activeValue={activeCategory}
          hrefBuilder={filterHref}
          ariaLabel="location categories"
        />
      </div>
    </FilterSection>
  );
}
