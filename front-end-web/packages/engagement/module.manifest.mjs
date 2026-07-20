/**
 * Citizen-engagement module manifest — declares how the orchestrator's codegen
 * (`scripts/gen-modules.mjs`) wires this module in: which route group its shims
 * are generated under, and which env flag toggles it at build time.
 *
 * Note the route group keeps the historical `(citizen-engagement)` folder name
 * while the package/id is `engagement`.
 */
export default {
  id: 'engagement',
  routeGroup: '(citizen-engagement)',
  envFlag: 'MODULE_ENGAGEMENT',
};
