/**
 * Server-side API client for the decentralised **system-trails** endpoints.
 *
 * Each vertical microservice exposes its own `GET /system-trails` endpoint
 * (core, citizen-engagement, leisure, mobility). The Administration → Registros
 * viewer and the per-user activity panels query one module at a time, passing
 * the module's gateway service path.
 *
 * All endpoints are admin-only and return the standard Spring `Page` envelope
 * (`Page<SystemEventDto>`). See `docs/SYSTEM-TRAILS-API.md`.
 *
 * Environment variables:
 *   MICROSERVICE_BASE_URL – gateway base URL (default: http://localhost:8762)
 *
 * ⚠️  Server-side only. Do NOT import in Client Components.
 */

import { serverFetch } from '@modelcity/core/lib/observability/serverFetch';

const GATEWAY = process.env.MICROSERVICE_BASE_URL ?? 'http://localhost:8762';

/**
 * Fetches a paginated page of system trail entries for a single module.
 *
 * @param {{
 *   servicePath: string,           // gateway service path, e.g. 'core'
 *   eventType?: string,            // exact event type (one at a time)
 *   responsibleUserId?: string,    // Auth0 sub of the actor (per-user view)
 *   from?: string,                 // ISO-8601 inclusive lower bound on occurredAt
 *   to?: string,                   // ISO-8601 inclusive upper bound on occurredAt
 *   page?: number,                 // 0-based; page size is fixed at 20 server-side
 * }} params
 * @param {string} accessToken      // Bearer JWT
 * @returns {Promise<object|null>}  // Spring Page object, or null on any error.
 */
export async function listSystemTrails(
  { servicePath, eventType, responsibleUserId, from, to, page = 0 } = {},
  accessToken,
) {
  if (!servicePath) return null;

  const qs = new URLSearchParams({ page: String(page) });
  if (eventType)         qs.set('eventType', eventType);
  if (responsibleUserId) qs.set('responsibleUserId', responsibleUserId);
  if (from)              qs.set('from', from);
  if (to)                qs.set('to', to);

  try {
    const res = await serverFetch(`${GATEWAY}/${servicePath}/system-trails?${qs.toString()}`, {
      headers: { Authorization: `Bearer ${accessToken}` },
      cache: 'no-store',
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (err) {
    return null;
  }
}
