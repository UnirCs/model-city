/**
 * Server-side API client for the Core microservice.
 *
 * Handles all user management operations: existence check (used during the
 * OAuth callback), profile retrieval and citizen registration.
 *
 * Environment variables:
 *   MICROSERVICE_BASE_URL      – gateway base URL  (default: http://localhost:8762)
 *   MICROSERVICE_CORE_APP_NAME – gateway service name  (default: core)
 *
 * ⚠️  Server-side only. Do NOT import in Client Components.
 *     For browser-side certificate verification use `@modelcity/core/lib/api/certClient`.
 */

import { serverFetch } from '@modelcity/core/lib/observability/serverFetch';

const GATEWAY = process.env.MICROSERVICE_BASE_URL       ?? 'http://localhost:8762';
const APP     = process.env.MICROSERVICE_CORE_APP_NAME  ?? 'core';
const BASE    = `${GATEWAY}/${APP}`;

/* ─────────────────────────────────────────────────────────────────────────
 * User existence
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Checks whether a user with the given Auth0 `sub` exists in the core service.
 * Uses a lightweight HEAD request to avoid transferring a response body.
 *
 * @param {string} sub          – Auth0 unique user identifier
 * @param {string} accessToken  – Bearer JWT
 * @returns {Promise<{ exists: boolean, expired: boolean }>}
 */
export async function checkUserExists(sub, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/users/${encodeURIComponent(sub)}`, {
      method: 'HEAD',
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    if (res.status === 404) return { exists: false, expired: false };
    if (res.status === 401) return { exists: false, expired: true };
    return { exists: res.ok, expired: false };
  } catch (err) {
    return { exists: false, expired: false };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * User listing  (admin)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches a paginated list of platform users.
 * Pass `citizen: true` to get only citizens; `citizen: false` for staff/workers.
 * Omit the param to get everyone. Optional filters: `name` (case-insensitive
 * substring), `neighbourhoodId` (citizens) and `role` (workers, dash format).
 *
 * Each element matches UserSummaryDto:
 *   { id, name, email, role, status, neighbourhoodId, neighbourhoodName, createdAt }
 *
 * @param {{
 *   citizen?: boolean,
 *   name?: string,
 *   neighbourhoodId?: number|string,
 *   role?: string,
 *   page?: number,
 *   size?: number,
 * }} params
 * @param {string} accessToken
 * @returns {Promise<object | null>}  Spring Page object, or null on any error.
 */
export async function getUsers(
  { citizen, name, neighbourhoodId, role, page = 0, size = 20 } = {},
  accessToken,
) {
  const qs = new URLSearchParams({ page: String(page), size: String(size) });
  if (citizen !== undefined)               qs.set('citizen', String(citizen));
  if (name)                                qs.set('name', name);
  if (neighbourhoodId !== undefined && neighbourhoodId !== '') {
    qs.set('neighbourhoodId', String(neighbourhoodId));
  }
  if (role)                                qs.set('role', role);
  try {
    const res = await serverFetch(`${BASE}/users?${qs.toString()}`, {
      headers: { Authorization: `Bearer ${accessToken}` },
      cache: 'no-store',
    });
    if (!res.ok) {
      return null;
    }
    return await res.json();
  } catch (err) {
    return null;
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Single user detail  (profile + admin detail view)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches the full profile of a user identified by their Auth0 `sub`.
 * Used both for the citizen's own profile page and for the admin detail view.
 *
 * @param {string} id           – Auth0 sub / user ID
 * @param {string} accessToken
 * @returns {Promise<object | null>}  null on any error.
 */
export async function getUserById(id, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/users/${encodeURIComponent(id)}`, {
      headers: { Authorization: `Bearer ${accessToken}` },
      cache: 'no-store',
    });
    if (!res.ok) {
      return null;
    }
    return await res.json();
  } catch (err) {
    return null;
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Registration
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Registers a new citizen in the core service.
 *
 * @param {{
 *   sub:              string,
 *   email:            string,
 *   name:             string,
 *   neighbourhoodName: string,
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function registerUser(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/users`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      return { error: body.message ?? 'registration_failed' };
    }

    return { ok: true };
  } catch (err) {
    return { error: 'network_error' };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Agent creation  (admin)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Creates a new municipal agent (operator) in the core service.
 * The backend sends an invitation e-mail to the given address.
 *
 * @param {{
 *   role:  string,   – e.g. 'MODEL-CITY-OPERATOR' | 'MODEL-CITY-BACKOFFICE' | 'MODEL-CITY-MOBILITY-AGENT'
 *   name:  string,
 *   email: string,
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function createAgent(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/agents`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (res.status === 202) return { ok: true };

    const body = await res.json().catch(() => ({}));
    return {
      error:  body.message ?? body.error ?? 'create_agent_failed',
      status: res.status,
      detail: body.detail ?? body.description ?? null,
    };
  } catch (err) {
    return { error: 'network_error', status: 0, detail: err?.message ?? null };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Enable / disable account  (admin)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Enables or disables a user account by setting its status.
 * Disabling is reversible — passing `'ACTIVE'` re-enables the account.
 * Platform admins cannot be disabled (the backend replies `403`).
 *
 * @param {string} id           – Auth0 sub of the target user
 * @param {'ACTIVE'|'DISABLED'} status
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { error: string, status?: number }>}
 */
export async function setUserStatus(id, status, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/users/${encodeURIComponent(id)}`, {
      method: 'PATCH',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ status }),
    });
    if (res.status === 204 || res.ok) return { ok: true };
    if (res.status === 403) return { error: 'forbidden', status: 403 };
    const body = await res.json().catch(() => ({}));
    return { error: body.message ?? 'status_change_failed', status: res.status };
  } catch (err) {
    return { error: 'network_error', status: 0 };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Create operation authorization (→ triggers OTP email)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * POSTs to the operation-authorizations endpoint to initiate an OTP flow.
 * On success the backend sends a one-time code to the citizen's email and
 * returns the authorization ID needed for the verification step.
 *
 * @param {{
 *   operationType: string,
 *   resourceType:  string,
 *   resourceId:    string,
 *   verificationToken?: string,
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<AuthorizationCreated | AuthorizationError>}
 */
export async function createOperationAuthorization(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/operation-authorizations`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      return { ok: false, status: res.status, body };
    }

    const data = await res.json();
    return { ok: true, operationAuthorizationId: data.operationAuthorizationId };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Verify operation authorization (OTP code)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * PATCHes an existing operation authorization to verify the OTP code.
 * Returns the raw HTTP status and body on failure so that the calling
 * Server Action can map them to domain-specific error codes.
 *
 * @param {string} operationAuthorizationId
 * @param {{
 *   otp:           string,
 *   operationType: string,
 *   resourceType:  string,
 *   resourceId:    string,
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<VerificationOk | VerificationError>}
 */
export async function verifyOperationAuthorization(operationAuthorizationId, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/operation-authorizations/${operationAuthorizationId}`, {
      method: 'PATCH',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      return { ok: false, status: res.status, body };
    }

    return { ok: true };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

