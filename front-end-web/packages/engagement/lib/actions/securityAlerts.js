'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canManageSecurityAlerts } from '@modelcity/engagement/lib/auth/roles';
import {
  createSecurityAlert as apiCreate,
  deleteSecurityAlert as apiDelete,
} from '@modelcity/engagement/lib/api/client';

/**
 * Server Action: creates a new security alert. Restricted to backoffice staff
 * and platform administrators.
 *
 * @param {{
 *   severity:         'IMPORTANT' | 'MEDIUM' | 'MILD',
 *   title:            string,
 *   description:      string,
 *   latitude:         number,
 *   longitude:        number,
 *   zoneId:           number,
 *   neighbourhoodId?: number | null,
 *   expiresAt:        string,  // ISO-8601 OffsetDateTime
 * }} payload
 * @returns {Promise<{ ok: true, id?: number | string } | { error: string }>}
 */
export async function createSecurityAlert(payload) {
  const session = await auth0.getSession();
  if (!canManageSecurityAlerts(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await apiCreate(payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'create_failed' };
  }

  revalidatePath('/[lang]/security/alerts', 'page');
  revalidatePath('/[lang]/security/alerts/manage', 'page');
  return { ok: true, id: result.data?.id };
}

/**
 * Server Action: deletes a security alert. Restricted to backoffice staff and
 * platform administrators.
 *
 * @param {string | number} id
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function removeSecurityAlert(id) {
  const session = await auth0.getSession();
  if (!canManageSecurityAlerts(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await apiDelete(id, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'delete_failed' };
  }

  revalidatePath('/[lang]/security/alerts', 'page');
  revalidatePath('/[lang]/security/alerts/manage', 'page');
  return { ok: true };
}


