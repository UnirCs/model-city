/**
 * Leisure module home-grid cards — events, tourism and sports-space bookings.
 * Registered with the core home-service registry by the composition root
 * (`src/lib/composition/registerModules.js`). Text lives in the `home`
 * dictionary under `home.services[id]`.
 *
 * @type {import('@modelcity/core/lib/config/services').HomeService[]}
 */
export const leisureHomeServices = [
  { id: 'eventos',  icon: 'event', href: '/events' },
  { id: 'turismo',  icon: 'map',   href: '/tourism/routes' },
  { id: 'reservas', icon: 'domain', href: '/sports-spaces' },
];
