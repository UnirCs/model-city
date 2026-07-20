import Icon from '@modelcity/core/components/atoms/Icon';
import PlaceMapClient from '@modelcity/leisure/components/molecules/PlaceMapClient';

/**
 * Molecule: LocationSection
 *
 * Reusable "location" panel shared by place / space detail views. Merges the
 * map, the postal address and a centred "open in external maps" CTA into a
 * single bordered section. Renders nothing when there is neither coordinates
 * nor an address.
 *
 * @param {{
 *   latitude?: number | null,
 *   longitude?: number | null,
 *   name: string,
 *   address?: string,
 *   mapTitle: string,
 *   openInMapsLabel: string,
 *   headingId?: string,
 * }} props
 */
export default function LocationSection({
  latitude,
  longitude,
  name,
  address,
  mapTitle,
  openInMapsLabel,
  headingId = 'location-map-heading',
}) {
  const hasCoords = latitude != null && longitude != null;
  if (!hasCoords && !address) return null;

  const mapsHref = hasCoords
    ? `https://www.google.com/maps/search/?api=1&query=${latitude},${longitude}`
    : null;

  return (
    <section
      aria-labelledby={headingId}
      className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
    >
      <h2
        id={headingId}
        className="text-h3 text-primary mb-md pb-xs border-b border-outline-variant flex items-center gap-sm"
      >
        <Icon name="map" fill size={20} className="text-secondary" />
        {mapTitle}
      </h2>

      {hasCoords && (
        <PlaceMapClient
          latitude={latitude}
          longitude={longitude}
          name={name}
          mapTitle={mapTitle}
        />
      )}

      {address && (
        <p className="text-body-md text-on-surface-variant flex items-start gap-xs mt-md">
          <Icon name="location_on" fill size={18} className="text-secondary shrink-0 mt-0.5" />
          <span>{address}</span>
        </p>
      )}

      {mapsHref && (
        <div className="mt-md flex justify-center">
          <a
            href={mapsHref}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center justify-center gap-sm px-md py-sm rounded-md bg-secondary text-on-secondary hover:bg-secondary-fixed-dim hover:text-on-secondary-fixed transition-colors shadow-sm font-semibold text-label-md"
          >
            {openInMapsLabel}
            <Icon name="open_in_new" size={18} />
          </a>
        </div>
      )}
    </section>
  );
}
