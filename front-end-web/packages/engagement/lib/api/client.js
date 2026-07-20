/**
 * Server-side API client for the Citizen Engagement microservice.
 *
 * Covers public-question listing, individual question retrieval, and
 * vote submission after an OTP has been verified.
 *
 * Environment variables:
 *   MICROSERVICE_BASE_URL – gateway base URL  (default: http://localhost:8762)
 *
 * ⚠️  Server-side only. Called from Server Components and Server Actions.
 */

import { serverFetch } from '@modelcity/core/lib/observability/serverFetch';

const GATEWAY = process.env.MICROSERVICE_BASE_URL ?? 'http://localhost:8762';
const BASE    = `${GATEWAY}/engagement`;

/* ─────────────────────────────────────────────────────────────────────────
 * Questions – listing
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches a paginated list of public questions filtered by status.
 *
 * @param {{
 *   status: 'active' | 'future' | 'past',
 *   page?:  number,
 *   size?:  number,
 * }} params
 * @param {string | undefined} accessToken  – optional; sent as Bearer JWT when provided.
 * @returns {Promise<object | null>}  Parsed JSON page object, or null on any error.
 */
export async function getPublicQuestions({ status, page = 0, size = 3 }, accessToken) {
  const url = `${BASE}/public-questions?status=${status}&page=${page}&size=${size}`;
  try {
    const res = await serverFetch(url, {
      cache: 'no-store',
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
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
 * Questions – detail
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches the full detail of a single public consultation.
 *
 * Returns a discriminated union so callers can handle 404 and generic errors
 * as separate cases without catching exceptions.
 *
 * @param {string | number} id
 * @param {string | undefined} accessToken
 * @returns {Promise<
 *   { data: object } |
 *   { notFound: true } |
 *   { error: true }
 * >}
 */
export async function getPublicQuestion(id, accessToken, includeTranslations = false) {
  const url = `${BASE}/public-questions/${id}${includeTranslations ? '?translations=full' : ''}`;
  try {
    const res = await serverFetch(url, {
      cache: 'no-store',
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
    });
    if (res.status === 404) return { notFound: true };
    if (!res.ok) {
      return { error: true };
    }
    return { data: await res.json() };
  } catch (err) {
    return { error: true };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Questions – create (admin)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Creates a new civic consultation.
 *
 * @param {{
 *   title:           string,
 *   description:     string,
 *   imageUrl?:       string,
 *   openDate:        string,   – ISO date "YYYY-MM-DD"
 *   closeDate:       string,   – ISO date "YYYY-MM-DD"
 *   zoneId:          number,
 *   neighbourhoodId: number,
 *   objectives:      Array<{ objective: string, sortOrder: number }>,
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function createPublicQuestion(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-questions`, {
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
    return { ok: true, data: await res.json() };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Questions – full update (admin / backoffice, future questions only)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fully updates a future civic consultation (PUT).
 *
 * @param {string | number} id
 * @param {object} payload  – CivicQuestionRequestDto fields
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { ok: false, status: number, body: object }>}
 */
export async function updatePublicQuestion(id, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-questions/${id}`, {
      method: 'PUT',
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

/* ─────────────────────────────────────────────────────────────────────────
 * Questions – partial update (admin / backoffice – open / close survey)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Partially updates a civic consultation (PATCH).
 * Used to start a future survey (set openDate = today) or to close an active
 * survey (set closeDate = today).
 *
 * @param {string | number} id
 * @param {{ openDate?: string, closeDate?: string }} payload  – only the date being changed
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { ok: false, status: number, body: object }>}
 */
export async function patchPublicQuestion(id, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-questions/${id}`, {
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

/* ─────────────────────────────────────────────────────────────────────────
 * Votes
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Casts a vote on a public question after the OTP has been verified.
 *
 * Returns the raw HTTP status and body on failure so that the calling
 * Server Action can map them to user-visible error messages.
 *
 * @param {string | number} questionId
 * @param {{
 *   operationAuthorizationId: string,
 *   vote: 'YES' | 'NO',
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<
 *   { ok: true } |
 *   { ok: false, status: number, body: object }
 * >}
 */
export async function castVote(questionId, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-questions/${questionId}/answers`, {
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

    return { ok: true };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/* ─────────────────────────────────────────────────────────────────────────
 * Security alerts
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches security alerts from the citizen-engagement microservice with pagination.
 *
 * @param {{ page?: number, size?: number }} options
 * @param {string | undefined} accessToken
 * @returns {Promise<{ content: Array<object>, page: { totalPages: number } } | null>}  Paginated alerts response, or null on error.
 */
export async function getSecurityAlerts({ page = 0, size = 4 } = {}, accessToken) {
  try {
    const url = new URL(`${BASE}/security-alerts`);
    url.searchParams.set('page', String(page));
    url.searchParams.set('size', String(size));
    const res = await serverFetch(url.toString(), {
      cache: 'no-store',
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
    });
    if (!res.ok) {
      return null;
    }
    return await res.json();
  } catch (err) {
    return null;
  }
}

/**
 * Creates a new security alert.
 *
 * @param {{
 *   severity:        'IMPORTANT' | 'MEDIUM' | 'MILD',
 *   title:           string,
 *   description:     string,
 *   latitude:        number,
 *   longitude:       number,
 *   zoneId:          number,
 *   neighbourhoodId?: number | null,
 *   expiresAt:       string,  // ISO-8601 OffsetDateTime
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function createSecurityAlert(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/security-alerts`, {
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
    return { ok: true, data: await res.json().catch(() => ({})) };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/**
 * Deletes a security alert by id.
 *
 * @param {string | number} id
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { ok: false, status: number, body: object }>}
 */
export async function deleteSecurityAlert(id, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/security-alerts/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${accessToken}` },
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
