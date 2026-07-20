'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canBookReservation, canManageSpaceResources } from '@modelcity/leisure/lib/auth/roles';
import {
  createReservation,
  deleteReservation,
} from '@modelcity/leisure/lib/api/client';

/**
 * Maps a backend error message to a stable client-side error code so the
 * UI can translate it without leaking server text.
 */
function mapReservationError(status, body) {
  if (status === 409) return 'conflict';
  const msg = (body?.message ?? '').toString().toLowerCase();
  if (msg.includes('advance') || msg.includes('soon')) return 'too_soon';
  if (msg.includes('window'))   return 'outside_window';
  if (msg.includes('duration')) return 'too_long';
  if (msg.includes('order') || msg.includes('end')) return 'invalid_range';
  return 'save_failed';
}

/** Returns tomorrow's date as `YYYY-MM-DD` (server local time). */
function tomorrowIso() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().slice(0, 10);
}

/**
 * Server Action: creates a new reservation for the current citizen.
 *
 * @param {{ spaceId: string | number, resourceId: string | number,
 *           reservationDate: string, startTime: string, endTime: string }} input
 */
export async function bookReservation({
  spaceId,
  resourceId,
  reservationDate,
  startTime,
  endTime,
}) {
  const session = await auth0.getSession();
  if (!canBookReservation(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;

  // Server-side: reservations must be at least 1 day in advance.
  if (reservationDate < tomorrowIso()) {
    return { error: 'too_soon' };
  }

  const result = await createReservation(
    spaceId,
    resourceId,
    { reservationDate, startTime, endTime },
    accessToken,
  );

  if (!result.ok) {
    return { error: mapReservationError(result.status, result.body) };
  }

  revalidatePath(`/[lang]/sports-spaces/${spaceId}/resources/${resourceId}`, 'page');
  return { ok: true, id: result.data?.id };
}

/**
 * Server Action: hard-deletes a reservation. Operator or admin only.
 */
export async function removeReservation({ spaceId, resourceId, reservationId }) {
  const session = await auth0.getSession();
  if (!canManageSpaceResources(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await deleteReservation(spaceId, resourceId, reservationId, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'delete_failed' };
  }

  revalidatePath(`/[lang]/sports-spaces/${spaceId}/resources/${resourceId}`, 'page');
  return { ok: true };
}





