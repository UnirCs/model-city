/**
 * Citizen-engagement capability helpers (participation consultations and
 * security alerts).
 *
 * Built on the core role primitives. All accept the full session object
 * returned by `auth0.getSession()`.
 */
import { ROLES, hasRole, hasAnyRole } from '@modelcity/core/lib/auth/roles';

/**
 * Returns true when the user can view the citizen participation section
 * (citizen, operator, backoffice and platform admin all have access).
 */
export function canViewParticipation(session) {
  return hasAnyRole(session, [
    ROLES.CITIZEN,
    ROLES.OPERATOR,
    ROLES.BACKOFFICE,
    ROLES.PLATFORM_ADMIN,
  ]);
}

/**
 * Returns true for users who can manage consultations
 * (edit data, close/open surveys): backoffice and platform admin.
 */
export function canManageParticipation(session) {
  return hasAnyRole(session, [ROLES.BACKOFFICE, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true only for platform administrators,
 * who can create new civic consultations.
 */
export function canCreateQuestion(session) {
  return hasRole(session, ROLES.PLATFORM_ADMIN);
}

/**
 * Returns true when the user can create or delete security alerts.
 * Restricted to backoffice staff and platform administrators.
 */
export function canManageSecurityAlerts(session) {
  return hasAnyRole(session, [ROLES.BACKOFFICE, ROLES.OPERATOR, ROLES.MOBILITY_AGENT, ROLES.PLATFORM_ADMIN]);
}
