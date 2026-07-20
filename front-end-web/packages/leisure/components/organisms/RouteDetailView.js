import Link from 'next/link';
import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import Icon from '@modelcity/core/components/atoms/Icon';
import CityPlaceCard from '@modelcity/leisure/components/molecules/CityPlaceCard';
import RouteMapClient from '@modelcity/leisure/components/molecules/RouteMapClient';
import DeleteEntityButton from '@modelcity/leisure/components/molecules/DeleteEntityButton';
import AdminSection from '@modelcity/core/components/molecules/AdminSection';
import { removeCityRoute } from '@modelcity/leisure/lib/actions/cityRoutes';
import { formatDuration } from '@modelcity/leisure/lib/utils/format';

/**
 * Organism: RouteDetailView
 *
 * Renders the body of a city-route detail page: banner with the route name, the
 * "about" panel (description, practical details and map), the paginated list of
 * points of interest and the staff administration controls.
 *
 * The back button is intentionally NOT rendered here so the page owns it.
 *
 * @param {{
 *   route: object,                 // city route payload from the leisure service
 *   places: object[],              // current page of route points of interest
 *   routeId: string,               // route id from the route params (for pagination)
 *   page: number,
 *   totalPages: number,
 *   t: object,                     // tourism.routeDetail dictionary
 *   units: object,                 // tourism.units dictionary
 *   tAdmin: object,                // tourism.adminActions dictionary
 *   categoryLabels: object,        // tourism.placeDetail.categoryLabels dictionary
 *   lang: string,
 *   canManage: boolean,
 * }} props
 */
export default function RouteDetailView({
  route,
  places,
  routeId,
  page,
  totalPages,
  t,
  units,
  tAdmin,
  categoryLabels,
  lang,
  canManage,
}) {
  const duration = formatDuration(route.estimatedDurationMinutes, units);

  return (
    <>
      {/* Banner with title overlay */}
      <section className="relative rounded-md overflow-hidden shadow-md h-64 md:h-80">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={route.imageUrl}
          alt={route.name}
          className="w-full h-full object-cover"
        />
        <div
          className="absolute inset-0 bg-linear-to-t from-primary/85 to-transparent"
          aria-hidden="true"
        />
        <div className="absolute bottom-md left-md right-md">
          <h1 className="text-h2 text-white leading-tight">{route.name}</h1>
        </div>
      </section>

      {/* Two columns: about (60%, left) | points of interest + administration (40%, right) */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-lg items-start">
        {/* LEFT — About this route: description, practical details and map */}
        <section
          aria-labelledby="route-intro-heading"
          className="lg:col-span-3 bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
        >
          <h2
            id="route-intro-heading"
            className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant"
          >
            {t.introTitle}
          </h2>
          <p className="text-body-md text-on-surface-variant whitespace-pre-line">
            {route.description}
          </p>

          {/* Practical details */}
          <div className="mt-md flex items-center gap-lg flex-wrap">
            <div className="flex flex-col gap-xs">
              <p className="text-caption uppercase tracking-widest font-bold text-secondary">
                {t.duration}
              </p>
              <div className="flex items-center gap-xs text-on-surface">
                <Icon name="schedule" size={16} className="text-secondary shrink-0" />
                <p className="text-body-md">{duration}</p>
              </div>
            </div>
            <div className="flex flex-col gap-xs">
              <p className="text-caption uppercase tracking-widest font-bold text-secondary">
                {t.placeCount}
              </p>
              <div className="flex items-center gap-xs text-on-surface">
                <Icon name="location_on" size={16} className="text-secondary shrink-0" />
                <p className="text-body-md">{route.placeCount}</p>
              </div>
            </div>
          </div>

          {/* Route map */}
          {places.length > 0 && (
            <div className="mt-md">
              <RouteMapClient places={places} mapTitle={t.mapTitle} />
            </div>
          )}
        </section>

        {/* RIGHT — points of interest + administration */}
        <div className="lg:col-span-2 space-y-lg">
          <section
            aria-labelledby="route-places-heading"
            className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
          >
            <h2
              id="route-places-heading"
              className="text-h3 text-primary mb-md pb-xs border-b border-outline-variant flex items-center gap-sm"
            >
              <Icon name="explore" fill size={22} className="text-secondary" />
              {t.placesTitle}
            </h2>

            {places.length === 0 ? (
              <p className="text-body-md text-on-surface-variant">{t.noPlaces}</p>
            ) : (
              <>
                <div className="grid grid-cols-1 gap-md">
                  {places.map((place, idx) => (
                    <CityPlaceCard
                      key={place.id}
                      place={place}
                      order={page * 10 + idx + 1}
                      routeId={route.id}
                      t={t}
                      categoryLabels={categoryLabels}
                      lang={lang}
                    />
                  ))}
                </div>

                {/* ── Pagination ─────────────────────────────────────── */}
                {totalPages > 1 && (
                  <nav
                    className="grid grid-cols-3 items-center gap-sm mt-md"
                    aria-label="route places pagination"
                  >
                    <div className="flex justify-start">
                      {page === 0 ? (
                        <span className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-outline opacity-50 text-label-md cursor-not-allowed select-none">
                          <Icon name="chevron_left" size={18} />
                          <span className="hidden sm:inline">{t.prev}</span>
                        </span>
                      ) : (
                        <LocalizedLink
                          href={`/tourism/routes/${routeId}?page=${page - 1}`}
                          className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md"
                        >
                          <Icon name="chevron_left" size={18} />
                          <span className="hidden sm:inline">{t.prev}</span>
                        </LocalizedLink>
                      )}
                    </div>
                    <span className="text-body-md text-on-surface-variant text-center">
                      {t.page} {page + 1} {t.of} {totalPages}
                    </span>
                    <div className="flex justify-end">
                      {page >= totalPages - 1 ? (
                        <span className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-outline opacity-50 text-label-md cursor-not-allowed select-none">
                          <span className="hidden sm:inline">{t.next}</span>
                          <Icon name="chevron_right" size={18} />
                        </span>
                      ) : (
                        <LocalizedLink
                          href={`/tourism/routes/${routeId}?page=${page + 1}`}
                          className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md"
                        >
                          <span className="hidden sm:inline">{t.next}</span>
                          <Icon name="chevron_right" size={18} />
                        </LocalizedLink>
                      )}
                    </div>
                  </nav>
                )}
              </>
            )}
          </section>

          {canManage && (
            <AdminSection title={tAdmin.sectionTitle}>
              <Link
                href={`/${lang}/tourism/routes/${route.id}/edit`}
                className="inline-flex items-center justify-center gap-sm px-md py-sm rounded-md border border-primary text-primary bg-surface text-label-md font-semibold hover:bg-primary/5 transition-all active:scale-95 w-full"
              >
                <Icon name="edit" size={18} />
                {tAdmin.editRoute}
              </Link>
              <DeleteEntityButton
                id={route.id}
                action={removeCityRoute}
                onSuccessHref={`/${lang}/tourism/routes`}
                className="w-full"
                labels={{
                  button:       tAdmin.deleteRoute,
                  confirmTitle: tAdmin.deleteRouteConfirmTitle,
                  confirmBody:  tAdmin.deleteRouteConfirmBody,
                  confirm:      tAdmin.confirm,
                  cancel:       tAdmin.cancel,
                  deleting:     tAdmin.deleting,
                  error:        tAdmin.deleteError,
                }}
              />
            </AdminSection>
          )}
        </div>
      </div>
    </>
  );
}
