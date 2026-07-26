/**
 * Static neighbourhood reference data, grouped by city zone.
 *
 * This is fixed configuration data for the city (not a backend mock): the
 * portal renders these zones/neighbourhoods in registration, profile,
 * participation and security forms. The catalogue itself is city-specific, so
 * it lives in the root `modelcity.config.js` (imported via `@modelcity/config`);
 * this module re-exports it to keep the stable
 * `@modelcity/core/lib/config/neighbourhoods` import path used across the app.
 *
 * `id` (and the per-neighbourhood `id`) mirror the numeric primary keys
 * expected by the backend API.
 *
 * @type {Array<{
 *   id:             number,
 *   zone:           string,
 *   zoneName:       string,
 *   neighbourhoods: Array<{ id: number, displayName: string, name: string }>
 * }>}
 */
import modelCityConfig from '@modelcity/config';

const neighbourhoods = modelCityConfig.neighbourhoods;

export default neighbourhoods;
