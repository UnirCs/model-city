/**
 * Module-boundary rules — the front-end equivalent of the back-end Maven
 * reactor's dependency graph.
 *
 * The portal is a workspace monorepo: an always-on `@modelcity/core` plus three
 * feature-module packages (`@modelcity/leisure`, `@modelcity/engagement`,
 * `@modelcity/mobility`), composed by the orchestrator app under `src/`. The
 * intended dependency direction is strictly:
 *
 *     module ──► core            (allowed)
 *     module ──► same module     (allowed)
 *     orchestrator (src/) ──► anything   (allowed; it composes the modules)
 *     core   ──► module          (FORBIDDEN — core must stay module-agnostic)
 *     module ──► other module    (FORBIDDEN — modules are isolated)
 *
 * `@modelcity/*` specifiers resolve to `packages/*` via jsconfig `paths`, so the
 * rules match on the resolved `packages/<name>/…` paths. Run: `npm run dep:check`.
 */

const CORE = '^packages/core/';
const LEISURE = '^packages/leisure/';
const ENGAGEMENT = '^packages/engagement/';
const MOBILITY = '^packages/mobility/';
const ANY_MODULE = '^packages/(?:leisure|engagement|mobility)/';

/** @type {import('dependency-cruiser').IConfiguration} */
module.exports = {
  forbidden: [
    {
      name: 'core-must-not-depend-on-modules',
      comment:
        'core is the always-on shared library; like the Maven reactor it must ' +
        'not depend on any feature module. Composition (i18n loaders, nav ' +
        'contributors) is inverted: core exposes registration hooks and the ' +
        'orchestrator (src/lib/composition/registerModules.js) wires the modules ' +
        'in — see docs/architecture/modular-frontend-packages.md.',
      severity: 'error',
      from: { path: CORE },
      to: { path: ANY_MODULE },
    },
    {
      name: 'leisure-must-not-depend-on-other-modules',
      comment: 'Feature modules are isolated from one another.',
      severity: 'error',
      from: { path: LEISURE },
      to: { path: '^packages/(?:engagement|mobility)/' },
    },
    {
      name: 'engagement-must-not-depend-on-other-modules',
      comment: 'Feature modules are isolated from one another.',
      severity: 'error',
      from: { path: ENGAGEMENT },
      to: { path: '^packages/(?:leisure|mobility)/' },
    },
    {
      name: 'mobility-must-not-depend-on-other-modules',
      comment: 'Feature modules are isolated from one another.',
      severity: 'error',
      from: { path: MOBILITY },
      to: { path: '^packages/(?:leisure|engagement)/' },
    },
  ],
  options: {
    doNotFollow: { path: 'node_modules' },
    exclude: { path: 'node_modules' },
    // Resolve the `@/*` and `@modelcity/*` aliases declared in jsconfig.json so
    // rules match on the resolved `src/...` / `packages/...` paths rather than
    // the raw import strings.
    enhancedResolveOptions: {
      extensions: ['.js', '.jsx', '.json'],
    },
    tsConfig: { fileName: 'jsconfig.json' },
  },
};
