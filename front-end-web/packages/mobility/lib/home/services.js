/**
 * Mobility module home-grid card — citizen stays / vehicle management.
 * Registered with the core home-service registry by the composition root
 * (`src/lib/composition/registerModules.js`). Text lives in the `home`
 * dictionary under `home.services[id]`.
 *
 * @type {import('@modelcity/core/lib/config/services').HomeService[]}
 */
export const mobilityHomeServices = [
  { id: 'movilidad', icon: 'local_shipping', href: '/mobility/my-stays' },
];
