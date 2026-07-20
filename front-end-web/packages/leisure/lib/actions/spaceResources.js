'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canManageSpaceResources } from '@modelcity/leisure/lib/auth/roles';
import {
  createPublicSpaceResource,
  updatePublicSpaceResource,
  deletePublicSpaceResource,
} from '@modelcity/leisure/lib/api/client';

/**
 * Server Action: creates (POST) or fully updates (PUT) a reservable resource
 * inside a public space. Allowed to operators and platform administrators.
 *
 * @param {string | number} spaceId
 * @param {{ name: string, description?: string, resourceType: string }} payload
 * @param {string | number | null | undefined} resourceId
 */
export async function saveSpaceResource(spaceId, payload, resourceId = null) {
  const session = await auth0.getSession();
  if (!canManageSpaceResources(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = resourceId
    ? await updatePublicSpaceResource(spaceId, resourceId, payload, accessToken)
    : await createPublicSpaceResource(spaceId, payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'save_failed' };
  }

  revalidatePath(`/[lang]/sports-spaces/${spaceId}`, 'page');
  return { ok: true, id: resourceId ?? result.data?.id };
}

/**
 * Server Action: soft-deletes a resource. Operator or admin.
 *
 * @param {string | number} spaceId
 * @param {string | number} resourceId
 */
export async function removeSpaceResource(spaceId, resourceId) {
  const session = await auth0.getSession();
  if (!canManageSpaceResources(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await deletePublicSpaceResource(spaceId, resourceId, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'delete_failed' };
  }

  revalidatePath(`/[lang]/sports-spaces/${spaceId}`, 'page');
  return { ok: true };
}

