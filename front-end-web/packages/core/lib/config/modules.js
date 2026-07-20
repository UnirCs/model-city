/**
 * Feature-module configuration.
 *
 * The portal groups its sections into activatable modules that mirror the
 * back-end microservices. Each module can be switched off through an
 * environment variable (default ON), the same idea as the back-end's
 * `@ConditionalOnProperty` toggles. When a module is disabled it disappears
 * from every navigation surface, from the home service grid, and its routes
 * become unreachable (404 via the per-section layout guards).
 *
 * Module → microservice → owned route prefixes:
 *   • leisure     (leisure service)            → /events, /sports-spaces, /tourism
 *   • engagement  (citizen-engagement service) → /participation, /security
 *   • mobility    (mobility service)           → /mobility
 *
 * The `core` service (home, profile, help, registration, administration) is
 * always on and is therefore not modelled as a toggleable module.
 *
 * Flags are read from `NEXT_PUBLIC_MODULE_*` so they are available both in
 * server components (home grid, layout guards) and client components
 * (navigation surfaces). Because `NEXT_PUBLIC_*` values are inlined at build
 * time, toggling a module requires a rebuild — consistent with the rest of
 * this project's build-time configuration (see `next.config.mjs`).
 */

/** Identifiers of the toggleable feature modules. */
export const MODULES = Object.freeze({
  LEISURE: 'leisure',
  ENGAGEMENT: 'engagement',
  MOBILITY: 'mobility',
});

/**
 * Route path prefixes owned by each module (paths are relative to the
 * `/[lang]` segment, e.g. `/events`).
 * @type {Record<string, string[]>}
 */
const MODULE_ROUTE_PREFIXES = {
  [MODULES.LEISURE]: ['/events', '/sports-spaces', '/tourism'],
  [MODULES.ENGAGEMENT]: ['/participation', '/security'],
  [MODULES.MOBILITY]: ['/mobility'],
};

/**
 * A module is enabled unless its flag is explicitly set to the string
 * `"false"`. Any other value (including unset) keeps it on.
 * @param {string | undefined} value
 */
function isEnabled(value) {
  return value !== 'false';
}

/**
 * Returns whether a feature module is currently enabled.
 *
 * Each `process.env.NEXT_PUBLIC_*` access is written literally so the bundler
 * can inline it — do not refactor this into a dynamic property lookup.
 *
 * @param {string} moduleId  One of {@link MODULES}.
 * @returns {boolean}
 */
export function isModuleEnabled(moduleId) {
  switch (moduleId) {
    case MODULES.LEISURE:
      return isEnabled(process.env.NEXT_PUBLIC_MODULE_LEISURE);
    case MODULES.ENGAGEMENT:
      return isEnabled(process.env.NEXT_PUBLIC_MODULE_ENGAGEMENT);
    case MODULES.MOBILITY:
      return isEnabled(process.env.NEXT_PUBLIC_MODULE_MOBILITY);
    default:
      // Unknown ids and core routes are always available.
      return true;
  }
}

/**
 * Resolves the module that owns a given route path, or `null` when the path
 * belongs to the always-on core service.
 *
 * @param {string} path  Path relative to `/[lang]` (e.g. `/events`).
 * @returns {string | null}
 */
export function moduleForPath(path) {
  for (const [moduleId, prefixes] of Object.entries(MODULE_ROUTE_PREFIXES)) {
    if (prefixes.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))) {
      return moduleId;
    }
  }
  return null;
}

/**
 * Returns whether a route path is reachable given the active modules.
 * Core paths (no owning module) are always reachable.
 *
 * @param {string} path  Path relative to `/[lang]` (e.g. `/security/alerts`).
 * @returns {boolean}
 */
export function isPathEnabled(path) {
  const moduleId = moduleForPath(path);
  return moduleId === null || isModuleEnabled(moduleId);
}
