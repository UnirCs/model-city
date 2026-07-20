'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canManageTourism } from '@modelcity/leisure/lib/auth/roles';
import {
  createCityPlace,
  updateCityPlace,
  deleteCityPlace,
} from '@modelcity/leisure/lib/api/client';

/**
 * Server Action: creates a new city place (POST) or fully updates an existing
 * one (PUT) depending on whether `id` is provided. Restricted to backoffice
 * staff and platform administrators.
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
 * @param {string | number | null | undefined} id
 * @returns {Promise<{ ok: true, id?: number | string } | { error: string }>}
 */
export async function saveCityPlace(payload, id = null) {
  const session = await auth0.getSession();
  if (!canManageTourism(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = id
    ? await updateCityPlace(id, payload, accessToken)
    : await createCityPlace(payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'save_failed' };
  }

  revalidatePath('/[lang]/tourism/locations', 'page');
  if (id) revalidatePath(`/[lang]/tourism/locations/${id}`, 'page');
  return { ok: true, id: id ?? result.data?.id };
}

/**
 * Server Action: deletes a city place. Restricted to backoffice staff and
 * platform administrators.
 *
 * @param {string | number} id
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function removeCityPlace(id) {
  const session = await auth0.getSession();
  if (!canManageTourism(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await deleteCityPlace(id, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'delete_failed' };
  }

  revalidatePath('/[lang]/tourism/locations', 'page');
  return { ok: true };
}

