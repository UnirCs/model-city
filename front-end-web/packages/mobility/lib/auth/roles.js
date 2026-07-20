/**
 * Mobility capability helpers (operator views and sanction issuing).
 *
 * Built on the core role primitives. All accept the full session object
 * returned by `auth0.getSession()`.
 */
import { ROLES, hasAnyRole } from '@modelcity/core/lib/auth/roles';

/**
 * Returns true when the user can access the mobility operator views
 * (active street reservations table, sanctions registry). Restricted to
 * mobility agents and platform administrators.
 */
export function canViewMobilityOps(session) {
  return hasAnyRole(session, [ROLES.MOBILITY_AGENT, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true when the user can issue new sanctions. Same scope as
 * {@link canViewMobilityOps}: mobility agents and platform administrators.
 */
export function canIssueSanctions(session) {
  return hasAnyRole(session, [ROLES.MOBILITY_AGENT, ROLES.PLATFORM_ADMIN]);
}
