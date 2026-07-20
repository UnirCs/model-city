/**
 * Orchestrator module registry — the single source of truth for which feature
 * modules exist and how the build-time codegen (`modelcity gen`) composes
 * them.
 *
 * Each feature module is declared by its own manifest
 * (`@modelcity/<id>/module.manifest.mjs`), resolved through `node_modules` —
 * workspace symlinks here in the platform monorepo, real installs in a city
 * project. A city's copy of this file imports only the modules it contracts
 * (the Maven-reactor `<modules>` analogue).
 *
 * A module is included in a build unless its `envFlag` is exactly the string
 * "false" — the same convention as the runtime `NEXT_PUBLIC_MODULE_*` toggles
 * and the Docker `MODULE_*` build args. The `core` module is always on and has
 * no flag.
 *
 * Display/registration order is the order below (drives nav section order and
 * i18n loader merge precedence).
 */
import leisure from '@modelcity/leisure/module.manifest.mjs';
import engagement from '@modelcity/engagement/module.manifest.mjs';
import mobility from '@modelcity/mobility/module.manifest.mjs';

/** All toggleable feature modules, in registration/display order. */
export const FEATURE_MODULES = [leisure, engagement, mobility];

/** The always-on core module (its routes are always generated). */
export const CORE_MODULE = { id: 'core', routeGroup: '(core)' };

/** A feature module is enabled unless its flag is exactly "false". */
export function isModuleEnabled(mod) {
  return process.env[mod.envFlag] !== 'false';
}

/** The feature modules selected for this build. */
export function enabledFeatureModules() {
  return FEATURE_MODULES.filter(isModuleEnabled);
}
