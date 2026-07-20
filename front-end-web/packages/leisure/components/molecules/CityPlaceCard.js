import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import { categoryIcon, categoryLabel } from '@modelcity/leisure/lib/utils/format';

/**
 * Molecule: CityPlaceCard
 * Compact card for a single point of interest inside a city-route detail.
 *
 * The whole card links to the place detail page. Follows the
 * citizen-services-rules: a thin secondary-coloured stripe on the left
 * highlights the card as a child of the parent route.
 *
 * @param {{
 *   place: {
 *     id: number,
 *     name: string,
 *     category: string,
 *     coverPhotoUrl?: string,
 *   },
 *   order: number,                  // 1-based ordinal to display "01", "02", …
 *   routeId: number | string,
 *   t: object,                      // tourism.routeDetail dictionary
 *   categoryLabels: Record<string, string>,
 *   lang: string,
 * }} props
 */
export default function CityPlaceCard({
  place,
  order,
  routeId,
  t,
  categoryLabels,
  lang,
}) {
  const href = `/${lang}/tourism/routes/${routeId}/places/${place.id}`;
  const ordinal = String(order).padStart(2, '0');

  return (
    <Link href={href} className="group block">
      <article className="relative overflow-hidden rounded-md border border-outline-variant bg-surface-container-low hover:shadow-md transition-all duration-200 hover:-translate-y-0.5">
        {/* Left accent stripe per citizen-services-rules */}
        <div className="absolute top-0 left-0 w-1 h-full bg-secondary" aria-hidden="true" />

        <div className="flex items-stretch gap-sm p-sm pl-md">
          {/* Ordinal badge */}
          <span className="shrink-0 w-10 h-10 bg-primary text-on-primary flex items-center justify-center rounded-md text-label-md font-bold">
            {ordinal}
          </span>

          {/* Body */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-xs text-secondary mb-xs">
              <Icon name={categoryIcon(place.category)} size={16} />
              <span className="text-caption font-semibold uppercase tracking-wide">
                {categoryLabel(place.category, categoryLabels)}
              </span>
            </div>
            <h4 className="text-body-md text-primary mb-xs truncate font-semibold">{place.name}</h4>
            <span className="inline-flex items-center gap-xs text-label-md text-secondary group-hover:text-primary transition-colors">
              {t.viewPlace}
              <Icon name="arrow_forward" size={16} />
            </span>
          </div>

          {/* Cover thumbnail (optional) */}
          {place.coverPhotoUrl && (
            <div className="hidden sm:block shrink-0 w-16 h-16 rounded-md overflow-hidden bg-surface-container-high">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={place.coverPhotoUrl}
                alt={place.name}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                loading="lazy"
              />
            </div>
          )}
        </div>
      </article>
    </Link>
  );
}

