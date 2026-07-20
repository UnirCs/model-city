/**
 * Citizen-engagement module home-grid cards — participation consultations and
 * security alerts. Registered with the core home-service registry by the
 * composition root (`src/lib/composition/registerModules.js`). Text lives in
 * the `home` dictionary under `home.services[id]`.
 *
 * @type {import('@modelcity/core/lib/config/services').HomeService[]}
 */
export const engagementHomeServices = [
  { id: 'participacion', icon: 'campaign', href: '/participation/questions' },
  { id: 'seguridad',     icon: 'warning',  href: '/security/alerts' },
];
