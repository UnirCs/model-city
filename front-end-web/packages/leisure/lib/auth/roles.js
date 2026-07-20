/**
 * Leisure capability helpers (events, sports-space reservations and tourism
 * content).
 *
 * Built on the core role primitives. All accept the full session object
 * returned by `auth0.getSession()`.
 */
import { ROLES, hasRole, hasAnyRole } from '@modelcity/core/lib/auth/roles';

/**
 * Returns true when the user can create, update or delete tourism content
 * (city routes and city places). Backoffice staff and platform administrators.
 */
export function canManageTourism(session) {
  return hasAnyRole(session, [ROLES.BACKOFFICE, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true when the user can create, update or delete public spaces.
 * Reserved to platform administrators (per the leisure/reservations spec).
 */
export function canManagePublicSpaces(session) {
  return hasRole(session, ROLES.PLATFORM_ADMIN);
}

/**
 * Returns true when the user can manage reservable resources of a public
 * space and remove reservations: field operators and platform administrators.
 * Also drives whether the privileged reservation view (with citizen info)
 * is shown.
 */
export function canManageSpaceResources(session) {
  return hasAnyRole(session, [ROLES.OPERATOR, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true when the user is allowed to create a reservation through
 * the citizen UI. Restricted to citizens — staff use the management views.
 */
export function canBookReservation(session) {
  return hasRole(session, ROLES.CITIZEN);
}

/**
 * Returns true when the user can create or update leisure events.
 * Backoffice staff and platform administrators.
 */
export function canManageEvents(session) {
  return hasAnyRole(session, [ROLES.BACKOFFICE, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true when the user can soft-delete a leisure event.
 * Reserved to platform administrators (deletion triggers automatic refunds).
 */
export function canDeleteEvents(session) {
  return hasRole(session, ROLES.PLATFORM_ADMIN);
}

/**
 * Returns true when the user can view the privileged list of tickets sold
 * for an event and issue manual refunds: backoffice and platform admin.
 */
export function canManageEventTickets(session) {
  return hasAnyRole(session, [ROLES.BACKOFFICE, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true when the user can purchase / claim tickets from the citizen
 * UI. Restricted to citizens — staff use the management views.
 */
export function canBuyEventTickets(session) {
  return hasRole(session, ROLES.CITIZEN);
}
