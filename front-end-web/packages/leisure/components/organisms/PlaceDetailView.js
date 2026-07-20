import Icon from '@modelcity/core/components/atoms/Icon';
import PhotoGallery from '@modelcity/leisure/components/molecules/PhotoGallery';
import LocationSection from '@modelcity/leisure/components/molecules/LocationSection';
import {
  categoryIcon,
  categoryLabel,
  formatDuration,
} from '@modelcity/leisure/lib/utils/format';

/**
 * Organism: PlaceDetailView
 *
 * Renders the shared body of a city-place detail (banner, gallery, description
 * with visit-duration footer, access info, accessibility info, map and an
 * address card with the "open in external maps" CTA).
 *
 * Used by both the route-scoped detail page and the standalone locations
 * detail page since the backend payload is identical.
 *
 * The back button is intentionally NOT rendered here so each page can choose
 * its own label and behaviour.
 *
 * @param {{
 *   place: {
 *     id: number,
 *     name: string,
 *     description?: string,
 *     address?: string,
 *     latitude?: number | null,
 *     longitude?: number | null,
 *     category?: string,
 *     visitDurationMinutes?: number | null,
 *     accessInfo?: string,
 *     accessibilityInfo?: string,
 *     photoUrls?: string[],
 *   },
 *   t: object,                                  // tourism.placeDetail dictionary
 *   units: { hours: string, minutes: string },  // tourism.units dictionary
 *   adminSlot?: React.ReactNode,                 // staff controls rendered in the aside
 * }} props
 */
export default function PlaceDetailView({ place, t, units, adminSlot = null }) {
  const photos = place.photoUrls ?? [];
  const bannerImage = photos[0];
  const otherPhotos = photos.slice(1);
  const visitDuration = formatDuration(place.visitDurationMinutes, units);

  return (
    <>
      {/* Banner */}
      {bannerImage && (
        <section className="relative rounded-md overflow-hidden shadow-md h-64 md:h-80">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={bannerImage}
            alt={place.name}
            className="w-full h-full object-cover"
          />
          <div
            className="absolute inset-0 bg-linear-to-t from-primary/85 to-transparent"
            aria-hidden="true"
          />
          <div className="absolute bottom-md left-md right-md">
            <span className="inline-flex items-center gap-xs px-sm py-xs rounded-full text-caption font-semibold mb-xs bg-secondary-container text-on-secondary-container">
              <Icon name={categoryIcon(place.category)} size={14} />
              {categoryLabel(place.category, t.categoryLabels)}
            </span>
            <h1 className="text-h2 text-white leading-tight">{place.name}</h1>
          </div>
        </section>
      )}

      {/* Title block fallback when there is no banner image */}
      {!bannerImage && (
        <header className="space-y-xs">
          <span className="inline-flex items-center gap-xs px-sm py-xs rounded-full text-caption font-semibold bg-secondary-container text-on-secondary-container">
            <Icon name={categoryIcon(place.category)} size={14} />
            {categoryLabel(place.category, t.categoryLabels)}
          </span>
          <h1 className="text-h2 text-primary leading-tight">{place.name}</h1>
        </header>
      )}

      {/* Two-column grid */}
      <div className="grid grid-cols-1 lg:grid-cols-10 gap-lg">

        {/* LEFT COLUMN — description, gallery, access info */}
        <div className="lg:col-span-6 space-y-md">

          {(place.description || place.visitDurationMinutes != null) && (
            <section
              aria-labelledby="place-description-heading"
              className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
            >
              <h2
                id="place-description-heading"
                className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant"
              >
                {t.descriptionTitle}
              </h2>
              {place.description && (
                <p className="text-body-md text-on-surface-variant whitespace-pre-line">
                  {place.description}
                </p>
              )}

              {/* Visit duration — footer of the description card */}
              {place.visitDurationMinutes != null && (
                <div className="mt-md flex flex-col gap-xs">
                  <p className="text-caption uppercase tracking-widest font-bold text-secondary">
                    {t.visitDuration}
                  </p>
                  <div className="flex items-center gap-xs text-on-surface">
                    <Icon name="schedule" size={16} className="text-secondary shrink-0" />
                    <p className="text-body-md">{visitDuration}</p>
                  </div>
                </div>
              )}
            </section>
          )}

          {otherPhotos.length > 0 && (
            <section
              aria-labelledby="place-gallery-heading"
              className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
            >
              <h2
                id="place-gallery-heading"
                className="text-h3 text-primary mb-md pb-xs border-b border-outline-variant flex items-center gap-sm"
              >
                <Icon name="photo_library" size={20} className="text-secondary" />
                {t.gallery}
              </h2>
              <PhotoGallery
                photos={otherPhotos}
                alt={place.name}
                closeLabel={t.galleryClose}
                prevLabel={t.galleryPrev}
                nextLabel={t.galleryNext}
              />
            </section>
          )}

          {place.accessInfo && (
            <section
              aria-labelledby="place-access-heading"
              className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
            >
              <h2
                id="place-access-heading"
                className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant flex items-center gap-sm"
              >
                <Icon name="directions" size={20} className="text-secondary" />
                {t.accessTitle}
              </h2>
              <p className="text-body-md text-on-surface-variant whitespace-pre-line">
                {place.accessInfo}
              </p>
            </section>
          )}

          {place.accessibilityInfo && (
            <section
              aria-labelledby="place-accessibility-heading"
              className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
            >
              <h2
                id="place-accessibility-heading"
                className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant flex items-center gap-sm"
              >
                <Icon name="accessible" size={20} className="text-secondary" />
                {t.accessibilityTitle}
              </h2>
              <p className="text-body-md text-on-surface-variant whitespace-pre-line">
                {place.accessibilityInfo}
              </p>
            </section>
          )}
        </div>

        {/* RIGHT COLUMN — location + admin controls */}
        <aside className="lg:col-span-4 space-y-md">
          <LocationSection
            latitude={place.latitude}
            longitude={place.longitude}
            name={place.name}
            address={place.address}
            mapTitle={t.mapTitle}
            openInMapsLabel={t.openInMaps}
          />
          {adminSlot}
        </aside>
      </div>
    </>
  );
}
