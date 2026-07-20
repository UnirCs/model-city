import barrios from '@modelcity/core/lib/config/neighbourhoods';

/**
 * Lookup helpers for translating numeric zone / neighbourhood ids
 * coming from the backend into their human-readable names.
 */

/**
 * Returns the display name of a zone by id.
 *
 * @param {number | string | null | undefined} zoneId
 * @returns {string | null}
 */
export function getZoneName(zoneId) {
  if (zoneId == null) return null;
  const numeric = Number(zoneId);
  const zone = barrios.find((z) => z.id === numeric);
  return zone ? zone.zone : null;
}

/**
 * Returns the display name of a neighbourhood by id.
 *
 * @param {number | string | null | undefined} neighbourhoodId
 * @returns {string | null}
 */
export function getNeighbourhoodName(neighbourhoodId) {
  if (neighbourhoodId == null) return null;
  const numeric = Number(neighbourhoodId);
  for (const zone of barrios) {
    const found = zone.neighbourhoods.find((n) => n.id === numeric);
    if (found) return found.displayName;
  }
  return null;
}

/**
 * Returns the list of neighbourhoods belonging to the given zone.
 *
 * @param {number | string | null | undefined} zoneId
 * @returns {Array<{ id: number, displayName: string, name: string }>}
 */
export function getNeighbourhoodsForZone(zoneId) {
  if (zoneId == null) return [];
  const numeric = Number(zoneId);
  const zone = barrios.find((z) => z.id === numeric);
  return zone ? zone.neighbourhoods : [];
}

