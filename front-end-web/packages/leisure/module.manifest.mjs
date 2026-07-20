/**
 * Leisure module manifest — declares how the orchestrator's codegen
 * (`scripts/gen-modules.mjs`) wires this module in: which route group its shims
 * are generated under, and which env flag toggles it at build time.
 */
export default {
  id: 'leisure',
  routeGroup: '(leisure)',
  envFlag: 'MODULE_LEISURE',
};
