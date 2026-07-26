/**
 * Home dashboard service catalogue — a module-contributed registry.
 *
 * The home grid is no longer a static central list: each feature module
 * contributes its own cards through {@link registerHomeServices}, exactly the
 * way the i18n engine (`lib/i18n/dictionaries.js`) and the nav model
 * (`lib/nav/sections.js`) are composed. The orchestrator wires the active
 * modules in at startup (`src/lib/composition/registerModules.js`), so a module
 * excluded at build time never registers — and a module merely hidden at
 * runtime is dropped later by `isPathEnabled` (see `home/page.js`).
 *
 * Each card carries only structural fields (`id`, `icon`, `href`); the
 * `href` decides which module the card belongs to (see `lib/config/modules`),
 * and the user-visible text lives in the `home` dictionary namespace under
 * `home.services[id]` ({ title, description }), so the catalogue stays
 * localised. Exposed as an ES module so it never triggers Turbopack's
 * "require is not defined" SSR error that a static JSON import would.
 *
 * @typedef {{ id: string, icon: string, href: string }} HomeService
 */

/** Registered cards, in the order their owning modules registered them. */
const homeServices = [];

/** Card arrays already merged in, guarding against double registration (see
 * `registerNavContributor` for why this can otherwise happen). */
const registeredSources = new Set();

/**
 * Registers a feature module's home-grid cards. Called once per active module
 * at startup by the composition root; the registration order is the order the
 * cards appear in the grid. Idempotent: registering the same module's cards
 * array more than once (e.g. the composition root re-running within the same
 * process) is a no-op after the first call.
 *
 * @param {HomeService[]} cards
 */
export function registerHomeServices(cards) {
  if (registeredSources.has(cards)) return;
  registeredSources.add(cards);
  homeServices.push(...cards);
}

/**
 * Returns the full catalogue contributed by the active modules. Callers are
 * responsible for the runtime `isPathEnabled` filtering and localisation.
 *
 * @returns {HomeService[]}
 */
export function getHomeServices() {
  return homeServices;
}
