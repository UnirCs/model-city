/**
 * Core authorisation primitives shared by every module.
 *
 * Auth0 injects a custom namespaced claim that carries the user's roles.
 * WHERE the claim lives depends on which token the Auth0 Action targeted:
 *
 *   • ID Token  → claim appears in `session.user` (decoded by the SDK).
 *   • Access Token → claim is inside the signed JWT at `session.tokenSet.accessToken`.
 *     When an `audience` is configured the Access Token is a full JWT, so we
 *     can decode its payload (base64url) without re-verifying the signature
 *     — Auth0 already validated it during the callback.
 *
 * The role catalogue is global (a single flat list issued by Auth0), so it
 * lives here as the single source of truth. Each feature module builds its own
 * capability helpers on top of {@link ROLES}, {@link hasRole} and
 * {@link hasAnyRole} (see `@/lib/<module>/auth/roles`). This file only keeps
 * the primitives plus the cross-cutting capabilities (administration,
 * backoffice and navigation-level gates).
 *
 * All helpers accept the **full session object** (the value returned by
 * `auth0.getSession()`) so they can inspect both tokens transparently.
 */

/**
 * Namespaced claim name where Auth0 stores the roles array.
 *
 * Auth0 uses the API audience as the claim namespace, so it is derived from
 * `AUTH0_AUDIENCE` (the same value {@link auth0} requests) to keep the front-end
 * tied to the configured tenant, not to a hard-coded city. This is only read
 * server-side (where the access token is decoded); client bundles that import
 * {@link ROLES} never evaluate the claim.
 */
export const ROLES_CLAIM = `${process.env.AUTH0_AUDIENCE}/roles`;

/** All known platform roles. */
export const ROLES = Object.freeze({
  PLATFORM_ADMIN:  'MODEL-CITY-PLATFORM-ADMIN',
  OPERATOR:        'MODEL-CITY-OPERATOR',
  BACKOFFICE:      'MODEL-CITY-BACKOFFICE',
  CITIZEN:         'MODEL-CITY-CITIZEN',
  MOBILITY_AGENT:  'MODEL-CITY-MOBILITY-AGENT',
});

/**
 * Safely decodes the payload section of a JWT without verifying the signature.
 * The signature was already verified by Auth0 during the OAuth callback.
 *
 * @param {string} jwt
 * @returns {Record<string, unknown>}
 */
function decodeJwtPayload(jwt) {
  try {
    const payloadB64 = jwt.split('.')[1];
    if (!payloadB64) return {};
    // Buffer is available in Node.js (Next.js server runtime)
    return JSON.parse(Buffer.from(payloadB64, 'base64url').toString('utf8'));
  } catch {
    return {};
  }
}

/**
 * Extracts the roles array from an Auth0 session.
 *
 * Resolution order:
 *   1. `session.user[ROLES_CLAIM]`  — present when the Action targets the ID Token.
 *   2. Decoded `session.tokenSet.accessToken[ROLES_CLAIM]` — present when the
 *      Action targets the Access Token (most common when an audience is set).
 *
 * @param {object | null | undefined} session  — value from `auth0.getSession()`
 * @returns {string[]}
 */
export function getUserRoles(session) {
  if (!session) return [];

  const accessToken = session.tokenSet?.accessToken;
  if (accessToken) {
    const payload = decodeJwtPayload(accessToken);
    const fromAccessToken = payload[ROLES_CLAIM];
    if (Array.isArray(fromAccessToken) && fromAccessToken.length > 0) {
      return fromAccessToken;
    }
  }
  return [];
}

/**
 * Checks if the session's user has a specific role.
 *
 * @param {object | null | undefined} session
 * @param {string} role
 */
export function hasRole(session, role) {
  return getUserRoles(session).includes(role);
}

/**
 * Checks if the session's user has at least one of the given roles.
 *
 * @param {object | null | undefined} session
 * @param {string[]} roles
 */
export function hasAnyRole(session, roles) {
  const userRoles = getUserRoles(session);
  return roles.some((r) => userRoles.includes(r));
}

/* ─────────────────────────────────────────────────────────────────────────
 * Cross-cutting capability helpers (administration, backoffice, navigation).
 * Module-specific capabilities live in `@/lib/<module>/auth/roles`.
 * All accept the full session object returned by auth0.getSession().
 * ───────────────────────────────────────────────────────────────────── */

/** Returns true when the user holds the standard citizen role. */
export function isCitizen(session) {
  return hasRole(session, ROLES.CITIZEN);
}

/** Only platform administrators can access the Administration section. */
export function canAccessAdministration(session) {
  return hasRole(session, ROLES.PLATFORM_ADMIN);
}

/** Only platform administrators can manage other users. */
export function canManageUsers(session) {
  return hasRole(session, ROLES.PLATFORM_ADMIN);
}

/** Backoffice tooling is available to backoffice staff and platform admins. */
export function canAccessBackoffice(session) {
  return hasAnyRole(session, [ROLES.BACKOFFICE, ROLES.PLATFORM_ADMIN]);
}

/** Operator panel is open to operators and platform admins. */
export function canAccessOperatorPanel(session) {
  return hasAnyRole(session, [ROLES.OPERATOR, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true for users who are staff (not a citizen).
 * Used to show the "citizen-only" notice instead of the voting zone.
 */
export function isStaffUser(session) {
  return hasAnyRole(session, [
    ROLES.OPERATOR,
    ROLES.BACKOFFICE,
    ROLES.PLATFORM_ADMIN,
    ROLES.MOBILITY_AGENT,
  ]);
}

/**
 * Returns true when the user can see the events, tourism and participation
 * sections. Available to citizens, backoffice and platform admins.
 * Operators and mobility agents do not have access.
 */
export function canViewGeneralSections(session) {
  return hasAnyRole(session, [ROLES.CITIZEN, ROLES.BACKOFFICE, ROLES.PLATFORM_ADMIN]);
}

/**
 * Returns true when the user can see the bookings section ("Tu ciudad").
 * Available to citizens, operators and platform admins.
 * Backoffice and mobility agents do not have access.
 */
export function canViewBookings(session) {
  return hasAnyRole(session, [ROLES.CITIZEN, ROLES.OPERATOR, ROLES.PLATFORM_ADMIN]);
}
