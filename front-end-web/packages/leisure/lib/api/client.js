/**
 * Server-side API client for the Leisure microservice.
 *
 * Single entry point for the whole module's domains — events & ticketing,
 * public/sports spaces & reservations, and tourism (routes & city places).
 *
 * Environment variables:
 *   MICROSERVICE_BASE_URL – gateway base URL  (default: http://localhost:8762)
 *
 * ⚠️  Server-side only. Called from Server Components and Server Actions.
 */
import { serverFetch } from '@modelcity/core/lib/observability/serverFetch';

const GATEWAY = process.env.MICROSERVICE_BASE_URL ?? 'http://localhost:8762';
const BASE    = `${GATEWAY}/leisure`;

/* ─────────────────────────────────────────────────────────────────────────
 * Events – listing, detail and mutations
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches a paginated list of events, optionally filtered by type and
 * whether they are paid.
 *
 * @param {{ page?: number, eventType?: string | null, paid?: boolean | null }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}
 */
export async function getEvents({ page = 0, eventType, paid } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page) });
  if (eventType) qs.set('eventType', eventType);
  if (paid != null) qs.set('paid', String(paid));
  const url = `${BASE}/events?${qs.toString()}`;
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

/**
 * Fetches the full detail of a single event.
 *
 * @param {string | number} id
 * @param {string | undefined} accessToken
 * @returns {Promise<{ data: object } | { notFound: true } | { error: true }>}
 */
export async function getEvent(id, accessToken, includeTranslations = false) {
  const url = `${BASE}/events/${id}${includeTranslations ? '?translations=full' : ''}`;
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

/**
 * Creates a new event.
 *
 * @param {object} payload  Matches the backend CreateEventRequest contract.
 * @param {string} accessToken
 */
export async function createEvent(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/events`, {
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

/**
 * Fully updates an existing event (PUT).
 *
 * @param {string | number} id
 * @param {object} payload
 * @param {string} accessToken
 */
export async function updateEvent(id, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/events/${id}`, {
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
    return { ok: true, data: await res.json().catch(() => ({})) };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/**
 * Soft-deletes an event. The backend automatically issues refunds for any
 * existing tickets associated with the event.
 *
 * @param {string | number} id
 * @param {string} accessToken
 */
export async function deleteEvent(id, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/events/${id}`, {
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

/* ─────────────────────────────────────────────────────────────────────────
 * Event tickets
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Purchases / claims a ticket for the calling citizen.
 *
 * For paid events `checkoutSessionId` must be provided so the backend
 * can persist the Stripe reference and return it later when listing tickets
 * (enabling manual refunds). Free events omit this field.
 *
 * @param {string | number} eventId
 * @param {string} accessToken
 * @param {string | null | undefined} checkoutSessionId  Stripe checkout session ID.
 */
export async function purchaseEventTicket(eventId, accessToken, checkoutSessionId) {
  try {
    const body = checkoutSessionId
      ? JSON.stringify({ checkoutSessionId })
      : JSON.stringify({});
    const res = await serverFetch(`${BASE}/events/${eventId}/tickets`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body,
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
 * Lists every ticket sold for an event (backoffice view, paginated).
 *
 * @param {string | number} eventId
 * @param {{ page?: number }} params
 * @param {string} accessToken
 */
export async function getEventTickets(eventId, { page = 0 } = {}, accessToken) {
  const url = `${BASE}/events/${eventId}/tickets?page=${page}`;
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

/**
 * Issues a manual refund for a ticket (backoffice action).
 *
 * @param {string | number} eventId
 * @param {string | number} ticketId
 * @param {{ reason: string }} payload
 * @param {string} accessToken
 */
export async function refundEventTicket(eventId, ticketId, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/events/${eventId}/tickets/${ticketId}/refunds`, {
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

/**
 * Lists the calling citizen's own tickets (paginated), optionally filtered by
 * the event period: `current,week` (this week), `future` or `past`.
 *
 * @param {string} userId  Auth0 `sub` of the citizen.
 * @param {{ page?: number, period?: string | null }} params
 * @param {string} accessToken
 */
export async function getUserTickets(userId, { page = 0, period } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page) });
  if (period) qs.set('period', period);
  const url = `${BASE}/users/${encodeURIComponent(userId)}/tickets?${qs.toString()}`;
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

/* ═════════════════════════════════════════════════════════════════════════
 * Spaces – public/sports spaces, resources and reservations
 * ═════════════════════════════════════════════════════════════════════════ */
/* ─────────────────────────────────────────────────────────────────────────
 * Public spaces – listing (paginated, only active)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches a paginated list of active public spaces.
 *
 * @param {{ page?: number }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}  Spring Page payload or null on error.
 */
export async function getPublicSpaces({ page = 0 } = {}, accessToken) {
  const url = `${BASE}/public-spaces?page=${page}`;
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

/**
 * Fetches the full detail of a single public space.
 *
 * @param {string | number} id
 * @param {string | undefined} accessToken
 * @returns {Promise<{ data: object } | { notFound: true } | { error: true }>}
 */
export async function getPublicSpace(id, accessToken, includeTranslations = false) {
  const url = `${BASE}/public-spaces/${id}${includeTranslations ? '?translations=full' : ''}`;
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
 * Public spaces – mutations (admin only)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Creates a new public space.
 *
 * @param {{
 *   name: string,
 *   description: string,
 *   address?: string | null,
 *   latitude?: number | null,
 *   longitude?: number | null,
 *   photoUrls?: string[],
 * }} payload
 * @param {string} accessToken
 */
export async function createPublicSpace(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-spaces`, {
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

/**
 * Fully updates an existing public space (PUT).
 *
 * @param {string | number} id
 * @param {object} payload
 * @param {string} accessToken
 */
export async function updatePublicSpace(id, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-spaces/${id}`, {
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
    return { ok: true, data: await res.json() };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/**
 * Soft-deletes a public space.
 *
 * @param {string | number} id
 * @param {string} accessToken
 */
export async function deletePublicSpace(id, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-spaces/${id}`, {
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

/* ─────────────────────────────────────────────────────────────────────────
 * Public-space resources
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches the (non-paginated) list of resources for a public space.
 *
 * @param {string | number} spaceId
 * @param {string | undefined} accessToken
 * @returns {Promise<Array<object> | null>}
 */
export async function getPublicSpaceResources(spaceId, accessToken, includeTranslations = false) {
  const url = `${BASE}/public-spaces/${spaceId}/resources${includeTranslations ? '?translations=full' : ''}`;
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

/**
 * Creates a new resource inside a public space.
 *
 * @param {string | number} spaceId
 * @param {{ name: string, description?: string, resourceType: string }} payload
 * @param {string} accessToken
 */
export async function createPublicSpaceResource(spaceId, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-spaces/${spaceId}/resources`, {
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

/**
 * Fully updates a resource (PUT).
 *
 * @param {string | number} spaceId
 * @param {string | number} resourceId
 * @param {object} payload
 * @param {string} accessToken
 */
export async function updatePublicSpaceResource(spaceId, resourceId, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-spaces/${spaceId}/resources/${resourceId}`, {
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
    return { ok: true, data: await res.json() };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/**
 * Soft-deletes a resource.
 *
 * @param {string | number} spaceId
 * @param {string | number} resourceId
 * @param {string} accessToken
 */
export async function deletePublicSpaceResource(spaceId, resourceId, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/public-spaces/${spaceId}/resources/${resourceId}`, {
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

/* ─────────────────────────────────────────────────────────────────────────
 * Resource reservations
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Lists the reservations of a resource on a given date.
 *
 * The backend returns the privileged view (with citizen info) for
 * admin/operator users and the public view (only times) for the rest.
 *
 * @param {string | number} spaceId
 * @param {string | number} resourceId
 * @param {string} date  ISO `YYYY-MM-DD`
 * @param {string | undefined} accessToken
 * @returns {Promise<Array<object> | null>}
 */
export async function getResourceReservations(spaceId, resourceId, date, accessToken) {
  const url = `${BASE}/public-spaces/${spaceId}/resources/${resourceId}/reservations?date=${date}`;
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

/**
 * Creates a new reservation for the calling citizen.
 *
 * @param {string | number} spaceId
 * @param {string | number} resourceId
 * @param {{ reservationDate: string, startTime: string, endTime: string }} payload
 *   Dates as `YYYY-MM-DD`, times as `HH:mm`.
 * @param {string} accessToken
 */
export async function createReservation(spaceId, resourceId, payload, accessToken) {
  try {
    const res = await serverFetch(
      `${BASE}/public-spaces/${spaceId}/resources/${resourceId}/reservations`,
      {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      },
    );
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
 * Hard-deletes a reservation (admin/operator only).
 *
 * @param {string | number} spaceId
 * @param {string | number} resourceId
 * @param {string | number} reservationId
 * @param {string} accessToken
 */
export async function deleteReservation(spaceId, resourceId, reservationId, accessToken) {
  try {
    const res = await serverFetch(
      `${BASE}/public-spaces/${spaceId}/resources/${resourceId}/reservations/${reservationId}`,
      {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${accessToken}` },
      },
    );
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      return { ok: false, status: res.status, body };
    }
    return { ok: true };
  } catch (err) {
    return { ok: false, status: 0, body: { message: 'network_error' } };
  }
}

/* ═════════════════════════════════════════════════════════════════════════
 * Tourism – city routes and places
 * ═════════════════════════════════════════════════════════════════════════ */
/* ─────────────────────────────────────────────────────────────────────────
 * City routes – listing (paginated, 3 results per page server-side)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches a paginated list of city routes.
 *
 * @param {{ page?: number }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}  Parsed page object, or null on error.
 */
export async function getCityRoutes({ page = 0 } = {}, accessToken) {
  const url = `${BASE}/city-routes?page=${page}`;
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
 * City route – detail
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches the full detail of a single city route, including its places.
 *
 * @param {string | number} id
 * @param {string | undefined} accessToken
 * @returns {Promise<
 *   { data: object } |
 *   { notFound: true } |
 *   { error: true }
 * >}
 */
export async function getCityRoute(id, accessToken, includeTranslations = false) {
  const url = `${BASE}/city-routes/${id}${includeTranslations ? '?translations=full' : ''}`;
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
 * City places – paginated listing scoped to a route
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches a paginated list of city places belonging to a given route.
 *
 * @param {string | number} routeId
 * @param {{ page?: number, size?: number }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}  Spring Page object, or null on error.
 */
export async function getCityRouteCityPlaces(routeId, { page = 0, size = 10 } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page), size: String(size) });
  const url = `${BASE}/city-routes/${routeId}/city-places?${qs.toString()}`;
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
 * City place – detail (scoped to a route)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches the full detail of a single city place belonging to a route.
 *
 * @param {string | number} routeId
 * @param {string | number} placeId
 * @param {string | undefined} accessToken
 * @returns {Promise<
 *   { data: object } |
 *   { notFound: true } |
 *   { error: true }
 * >}
 */
export async function getCityPlace(routeId, placeId, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/city-routes/${routeId}/city-places/${placeId}`, {
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
 * City places – standalone listing (Locations section)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches a paginated list of standalone city places, optionally filtered
 * by category. The backend always returns 6 results per page.
 *
 * @param {{ page?: number, category?: string | null }} params
 * @param {string | undefined} accessToken
 * @returns {Promise<object | null>}
 */
export async function getCityPlaces({ page = 0, category } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page), size: '6' });
  if (category) qs.set('category', category);
  const url = `${BASE}/city-places?${qs.toString()}`;
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
 * City place – standalone detail (not scoped to a route)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches the full detail of a standalone city place.
 * The response shape is identical to getCityPlace (route-scoped).
 *
 * @param {string | number} id
 * @param {string | undefined} accessToken
 * @returns {Promise<
 *   { data: object } |
 *   { notFound: true } |
 *   { error: true }
 * >}
 */
export async function getStandaloneCityPlace(id, accessToken, includeTranslations = false) {
  const url = `${BASE}/city-places/${id}${includeTranslations ? '?translations=full' : ''}`;
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
 * City places – exhaustive listing (route/place admin forms)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Fetches every city place across all pages by iterating sequentially through
 * the backend's fixed page size. Used by the route builder form to populate
 * the "available places" picker. Returns an empty array on any error.
 *
 * @param {string | undefined} accessToken
 * @returns {Promise<Array<object>>}
 */
export async function getAllCityPlaces(accessToken) {
  const all = [];
  let page = 0;
  // Safety cap to avoid infinite loops on backend pagination bugs.
  const MAX_PAGES = 50;
  while (page < MAX_PAGES) {
    const data = await getCityPlaces({ page }, accessToken);
    if (!data) break;
    const content = data.content ?? [];
    all.push(...content);
    const totalPages = data.page?.totalPages ?? 0;
    page += 1;
    if (page >= totalPages) break;
  }
  return all;
}

/* ─────────────────────────────────────────────────────────────────────────
 * City routes – mutations (POST / PUT / DELETE)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Creates a new city route.
 *
 * @param {{
 *   name: string,
 *   description: string,
 *   targetAudience: string,
 *   imageUrl?: string | null,
 *   estimatedDurationMinutes?: number | null,
 *   cityPlaceIds: number[],
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function createCityRoute(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/city-routes`, {
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

/**
 * Fully updates an existing city route (PUT).
 *
 * @param {string | number} id
 * @param {object} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { ok: false, status: number, body: object }>}
 */
export async function updateCityRoute(id, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/city-routes/${id}`, {
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

/**
 * Deletes a city route.
 *
 * @param {string | number} id
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { ok: false, status: number, body: object }>}
 */
export async function deleteCityRoute(id, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/city-routes/${id}`, {
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

/* ─────────────────────────────────────────────────────────────────────────
 * City places – mutations (POST / PUT / DELETE)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Creates a new standalone city place.
 *
 * @param {{
 *   name: string,
 *   latitude: number,
 *   longitude: number,
 *   description: string,
 *   address?: string,
 *   photoUrls?: string[],
 *   accessInfo?: string,
 *   accessibilityInfo?: string,
 *   category?: string,
 *   visitDurationMinutes?: number | null,
 * }} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true, data: object } | { ok: false, status: number, body: object }>}
 */
export async function createCityPlace(payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/city-places`, {
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

/**
 * Fully updates an existing city place (PUT).
 *
 * @param {string | number} id
 * @param {object} payload
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { ok: false, status: number, body: object }>}
 */
export async function updateCityPlace(id, payload, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/city-places/${id}`, {
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

/**
 * Deletes a city place.
 *
 * @param {string | number} id
 * @param {string} accessToken
 * @returns {Promise<{ ok: true } | { ok: false, status: number, body: object }>}
 */
export async function deleteCityPlace(id, accessToken) {
  try {
    const res = await serverFetch(`${BASE}/city-places/${id}`, {
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
