'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canManagePublicSpaces } from '@modelcity/leisure/lib/auth/roles';
import {
  createPublicSpace,
  updatePublicSpace,
  deletePublicSpace,
} from '@modelcity/leisure/lib/api/client';

/**
 * Server Action: creates (POST) or fully updates (PUT) a public space.
 * Restricted to platform administrators.
 *
 * @param {{
 *   name: string,
 *   description: string,
 *   address?: string | null,
 *   latitude?: number | null,
 *   longitude?: number | null,
 *   photoUrls?: string[],
 * }} payload
 * @param {string | number | null | undefined} id
 * @returns {Promise<{ ok: true, id?: number | string } | { error: string }>}
 */
export async function savePublicSpace(payload, id = null) {
  const session = await auth0.getSession();
  if (!canManagePublicSpaces(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = id
    ? await updatePublicSpace(id, payload, accessToken)
    : await createPublicSpace(payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'save_failed' };
  }

  revalidatePath('/[lang]/sports-spaces', 'page');
  const targetId = id ?? result.data?.id;
  if (targetId) revalidatePath(`/[lang]/sports-spaces/${targetId}`, 'page');
  return { ok: true, id: targetId };
}

/**
 * Server Action: soft-deletes a public space. Admin-only.
 *
 * @param {string | number} id
 */
export async function removePublicSpace(id) {
  const session = await auth0.getSession();
  if (!canManagePublicSpaces(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await deletePublicSpace(id, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'delete_failed' };
  }

  revalidatePath('/[lang]/sports-spaces', 'page');
  return { ok: true };
}

