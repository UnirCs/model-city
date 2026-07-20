'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canIssueSanctions } from '@modelcity/mobility/lib/auth/roles';
import {
  createUserCar as apiCreateCar,
  createUserStreetReservation as apiCreateReservation,
  renewUserStreetReservation as apiRenewReservation,
  createSanction as apiCreateSanction,
  getUserSanction as apiGetUserSanction,
  getSanction as apiGetSanction,
} from '@modelcity/mobility/lib/api/client';

/**
 * Helper: returns the current Auth0 session, the user sub and the access
 * token. When the user is unauthenticated, callers get `{ error }`.
 */
async function requireSession() {
  const session = await auth0.getSession();
  const sub = session?.user?.sub;
  const accessToken = session?.tokenSet?.accessToken;
  if (!sub || !accessToken) return { error: 'unauthenticated' };
  return { session, sub, accessToken };
}

/* ─────────────────────────────────────────────────────────────────────────
 * Cars
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: registers a new car for the current user.
 *
 * @param {{ licensePlate: string, nickname?: string, brand?: string, model?: string }} payload
 * @returns {Promise<{ ok: true, data: object } | { error: string, status?: number }>}
 */
export async function addUserCar(payload) {
  const ctx = await requireSession();
  if (ctx.error) return { error: ctx.error };

  const result = await apiCreateCar(ctx.sub, payload, ctx.accessToken);
  if (!result.ok) {
    return { error: result.body?.message ?? 'create_failed', status: result.status };
  }

  revalidatePath('/[lang]/mobility/cars', 'page');
  revalidatePath('/[lang]/mobility/reserve', 'page');
  return { ok: true, data: result.data };
}

/* ─────────────────────────────────────────────────────────────────────────
 * Street reservations
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: creates a new street reservation for the current user.
 *
 * @param {{ carId: number, latitude: number, longitude: number, durationMinutes: number }} payload
 * @returns {Promise<{ ok: true, data: object } | { error: string, status?: number }>}
 */
export async function createStreetReservation(payload) {
  const ctx = await requireSession();
  if (ctx.error) return { error: ctx.error };

  const result = await apiCreateReservation(ctx.sub, payload, ctx.accessToken);
  if (!result.ok) {
    return { error: result.body?.message ?? 'create_failed', status: result.status };
  }

  revalidatePath('/[lang]/mobility/my-stays', 'page');
  return { ok: true, data: result.data };
}

/**
 * Server Action: renews an active street reservation.
 *
 * @param {string | number} reservationId
 * @param {{ carId: number, latitude: number, longitude: number, durationMinutes: number }} payload
 * @returns {Promise<{ ok: true, data: object } | { error: string, status?: number }>}
 */
export async function renewStreetReservation(reservationId, payload) {
  const ctx = await requireSession();
  if (ctx.error) return { error: ctx.error };

  const result = await apiRenewReservation(ctx.sub, reservationId, payload, ctx.accessToken);
  if (!result.ok) {
    return { error: result.body?.message ?? 'create_failed', status: result.status };
  }

  revalidatePath('/[lang]/mobility/my-stays', 'page');
  return { ok: true, data: result.data };
}

/* ─────────────────────────────────────────────────────────────────────────
 * Sanctions
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: issues a new sanction. Restricted to mobility operators
 * and platform administrators.
 *
 * @param {{ licensePlate: string, latitude: number, longitude: number, imageBase64: string }} payload
 * @returns {Promise<{ ok: true, data: object } | { error: string, status?: number }>}
 */
export async function issueSanction(payload) {
  const session = await auth0.getSession();
  if (!canIssueSanctions(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await apiCreateSanction(payload, accessToken);
  if (!result.ok) {
    return { error: result.body?.message ?? 'create_failed', status: result.status };
  }

  revalidatePath('/[lang]/mobility/sanctions', 'page');
  return { ok: true, data: result.data };
}

/**
 * Server Action: returns the full detail of a sanction belonging to the
 * current user (including its base64 evidence). Used by the citizen-facing
 * detail modal to load the image lazily.
 *
 * @param {string | number} sanctionId
 * @returns {Promise<{ data: object } | { error: true }>}
 */
export async function fetchMySanctionDetail(sanctionId) {
  const ctx = await requireSession();
  if (ctx.error) return { error: true };

  const res = await apiGetUserSanction(ctx.sub, sanctionId, ctx.accessToken);
  if (res?.data) return { data: res.data };
  return { error: true };
}

/**
 * Server Action: returns the full detail of any sanction (staff scope).
 * Restricted to mobility operators and platform administrators.
 *
 * @param {string | number} sanctionId
 * @returns {Promise<{ data: object } | { error: true }>}
 */
export async function fetchSanctionDetail(sanctionId) {
  const session = await auth0.getSession();
  if (!canIssueSanctions(session)) return { error: true };

  const accessToken = session?.tokenSet?.accessToken;
  const res = await apiGetSanction(sanctionId, accessToken);
  if (res?.data) return { data: res.data };
  return { error: true };
}




