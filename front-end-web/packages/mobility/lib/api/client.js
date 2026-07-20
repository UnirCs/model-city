/**
 * Server-side API client for the Mobility microservice.
 *
 * Covers:
 *   - Personal cars (citizen) — POST /users/{id}/cars, GET /users/{id}/cars
 *   - Street reservations (citizen) — list, create, renew
 *   - Street reservations (staff)   — paginated list with filters
 *   - Sanctions (citizen)           — list, detail with image
 *   - Sanctions (staff)             — paginated list, detail, create
 *
 * Environment variables:
 *   MICROSERVICE_BASE_URL          – gateway base URL  (default: http://localhost:8762)
 *   MICROSERVICE_MOBILITY_APP_NAME – gateway service name (default: mobility)
 *
 * ⚠️  Server-side only. Called from Server Components and Server Actions.
 */

import { serverFetch } from '@modelcity/core/lib/observability/serverFetch';

const GATEWAY = process.env.MICROSERVICE_BASE_URL          ?? 'http://localhost:8762';
const APP     = process.env.MICROSERVICE_MOBILITY_APP_NAME ?? 'mobility';
const BASE    = `${GATEWAY}/${APP}`;

/** Common Bearer header builder. */
function authHeaders(accessToken, extra = {}) {
  return accessToken
    ? { Authorization: `Bearer ${accessToken}`, ...extra }
    : { ...extra };
}

/* ─────────────────────────────────────────────────────────────────────────
 * Cars (citizen)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Lists the cars registered by the given user (paginated, page size 5).
 *
 * @param {string} userId        – Auth0 sub of the user
 * @param {{ page?: number, size?: number }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}  Spring Page object, or null on error.
 */
export async function getUserCars(userId, { page = 0, size = 5 } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page), size: String(size) });
  try {
    const res = await serverFetch(
      `${BASE}/users/${encodeURIComponent(userId)}/cars?${qs.toString()}`,
      { cache: 'no-store', headers: authHeaders(accessToken) },
    );
    if (!res.ok) {
      return null;
    }
    return await res.json();
  } catch (err) {
    return null;
  }
}

/**
 * Fetches every car registered by the given user across all pages.
 * Used when a flat list is needed (e.g. vehicle selector in the reservation form).
 *
 * @param {string} userId
 * @param {string | undefined} accessToken
 * @returns {Promise<Array<object>>}  Flat array; empty on error.
 */
export async function getAllUserCars(userId, accessToken) {
  const all = [];
  let page = 0;
  const MAX_PAGES = 20;
  while (page < MAX_PAGES) {
    const data = await getUserCars(userId, { page, size: 5 }, accessToken);
    if (!data) break;
    all.push(...(data.content ?? []));
    const totalPages = data.page?.totalPages ?? 0;
    page += 1;
    if (page >= totalPages) break;
  }
  return all;
}

/**
 * Registers a new car for the given user.
 *
 * @param {string} userId
 * @param {{ licensePlate: string, nickname?: string, brand?: string, model?: string }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function createUserCar(userId, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/users/${encodeURIComponent(userId)}/cars`, {
      method: 'POST',
      headers: authHeaders(accessToken, { 'Content-Type': 'application/json' }),
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
 * Street reservations (citizen)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Lists the user's street reservations (active + past 30 days), paginated.
 *
 * @param {string} userId
 * @param {{ page?: number, size?: number }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}  Spring Page object, or null on error.
 */
export async function getUserStreetReservations(userId, { page = 0, size = 10 } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page), size: String(size) });
  try {
    const res = await serverFetch(
      `${BASE}/users/${encodeURIComponent(userId)}/street-reservations?${qs.toString()}`,
      { cache: 'no-store', headers: authHeaders(accessToken) },
    );
    if (!res.ok) {
      return null;
    }
    return await res.json();
  } catch (err) {
    return null;
  }
}

/**
 * Creates a new street reservation for the given user.
 *
 * @param {string} userId
 * @param {{ carId: number, latitude: number, longitude: number, durationMinutes: number }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function createUserStreetReservation(userId, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/users/${encodeURIComponent(userId)}/street-reservations`, {
      method: 'POST',
      headers: authHeaders(accessToken, { 'Content-Type': 'application/json' }),
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

/**
 * Renews an active street reservation. The body shape is identical to a new
 * reservation (carId, lat, lng, durationMinutes).
 *
 * @param {string} userId
 * @param {string | number} reservationId
 * @param {{ carId: number, latitude: number, longitude: number, durationMinutes: number }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function renewUserStreetReservation(userId, reservationId, payload, accessToken) {
  try {
    const url = `${BASE}/users/${encodeURIComponent(userId)}/street-reservations/${reservationId}/renewals`;
    const res = await serverFetch(url, {
      method: 'POST',
      headers: authHeaders(accessToken, { 'Content-Type': 'application/json' }),
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
 * Street reservations (staff)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Builds the query string for the staff reservations listing endpoint.
 * Empty / null values are omitted.
 */
function buildReservationsQuery({ licensePlate, from, to, active, page = 0 } = {}) {
  const qs = new URLSearchParams({ page: String(page) });
  if (licensePlate)    qs.set('licensePlate', licensePlate);
  if (from)            qs.set('from', from);
  if (to)              qs.set('to', to);
  if (active !== undefined && active !== null && active !== '') qs.set('active', String(active));
  return qs.toString();
}

/**
 * Staff endpoint: paginated list of all street reservations matching the
 * provided filters. When `active` is supplied it takes precedence over the
 * date range (handled server-side).
 *
 * @param {{
 *   licensePlate?: string,
 *   from?:         string,   – ISO OffsetDateTime
 *   to?:           string,   – ISO OffsetDateTime
 *   active?:       boolean | null,
 *   page?:         number,
 * }} params
 * @param {string} accessToken
 * @returns {Promise<object | null>}
 */
export async function listStreetReservations(params = {}, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/street-reservations?${buildReservationsQuery(params)}`, {
      cache: 'no-store',
      headers: authHeaders(accessToken),
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
 * Sanctions (citizen)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Lists the sanctions associated with the given user, paginated.
 * The image base64 is NOT included — call {@link getUserSanction} to retrieve it.
 *
 * @param {string} userId
 * @param {{ page?: number, size?: number }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}  Spring Page object, or null on error.
 */
export async function getUserSanctions(userId, { page = 0, size = 10 } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page), size: String(size) });
  try {
    const res = await serverFetch(
      `${BASE}/users/${encodeURIComponent(userId)}/sanctions?${qs.toString()}`,
      { cache: 'no-store', headers: authHeaders(accessToken) },
    );
    if (!res.ok) {
      return null;
    }
    return await res.json();
  } catch (err) {
    return null;
  }
}

/**
 * Fetches the full detail of a single sanction belonging to the given user,
 * including its base64 image.
 *
 * @param {string} userId
 * @param {string | number} sanctionId
 * @param {string | undefined} accessToken
 * @returns {Promise<{ data: object } | { notFound: true } | { error: true }>}
 */
export async function getUserSanction(userId, sanctionId, accessToken) {
  try {
    const res = await serverFetch(
      `${BASE}/users/${encodeURIComponent(userId)}/sanctions/${sanctionId}`,
      { cache: 'no-store', headers: authHeaders(accessToken) },
    );
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
 * Sanctions (staff)
 * ───────────────────────────────────────────────────────────────────── */

function buildSanctionsQuery({ licensePlate, from, to, page = 0 } = {}) {
  const qs = new URLSearchParams({ page: String(page) });
  if (licensePlate) qs.set('licensePlate', licensePlate);
  if (from)         qs.set('from', from);
  if (to)           qs.set('to', to);
  return qs.toString();
}

/**
 * Staff endpoint: paginated list of every sanction matching the provided
 * filters. Image base64 is NOT included.
 *
 * @param {{ licensePlate?: string, from?: string, to?: string, page?: number }} params
 * @param {string} accessToken
 * @returns {Promise<object | null>}
 */
export async function listSanctions(params = {}, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/sanctions?${buildSanctionsQuery(params)}`, {
      cache: 'no-store',
      headers: authHeaders(accessToken),
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
 * Staff endpoint: full sanction detail including base64 image.
 *
 * @param {string | number} sanctionId
 * @param {string} accessToken
 * @returns {Promise<{ data: object } | { notFound: true } | { error: true }>}
 */
export async function getSanction(sanctionId, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/sanctions/${sanctionId}`, {
      cache: 'no-store',
      headers: authHeaders(accessToken),
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

/**
 * Staff endpoint: issues a new sanction.
 *
 * @param {{ licensePlate: string, latitude: number, longitude: number, imageBase64: string }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function createSanction(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/sanctions`, {
      method: 'POST',
      headers: authHeaders(accessToken, { 'Content-Type': 'application/json' }),
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

