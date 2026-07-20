'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import {
  createCityRoute,
  updateCityRoute,
  deleteCityRoute,
} from '@modelcity/leisure/lib/api/client';

/**
 * Server Action: creates a new city route (POST) or fully updates an existing
 * one (PUT) depending on whether `id` is provided. A single entry point keeps
 * the create and edit forms consistent.
 *
 * Restricted to backoffice staff and platform administrators.
 *
 * @param {{
 *   name: string,
 *   description: string,
 *   targetAudience: string,
 *   imageUrl?: string | null,
 *   estimatedDurationMinutes?: number | null,
 *   cityPlaceIds: number[],
 * }} payload
 * @param {string | number | null | undefined} id
 * @returns {Promise<{ ok: true, id?: number | string } | { error: string }>}
 */
export async function saveCityRoute(payload, id = null) {
  const session = await auth0.getSession();
  if (!canManageTourism(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = id
    ? await updateCityRoute(id, payload, accessToken)
    : await createCityRoute(payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'save_failed' };
  }

  revalidatePath('/[lang]/tourism/routes', 'page');
  if (id) revalidatePath(`/[lang]/tourism/routes/${id}`, 'page');
  return { ok: true, id: id ?? result.data?.id };
}

/**
 * Server Action: deletes a city route.
 *
 * Restricted to backoffice staff and platform administrators.
 *
 * @param {string | number} id
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function removeCityRoute(id) {
  const session = await auth0.getSession();
  if (!canManageTourism(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await deleteCityRoute(id, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'delete_failed' };
  }

  revalidatePath('/[lang]/tourism/routes', 'page');
  return { ok: true };
}

