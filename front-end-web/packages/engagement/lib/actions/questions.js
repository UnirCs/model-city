'use server';

import { auth0 } from '@modelcity/core/lib/auth/auth0';
import {
  createPublicQuestion,
  updatePublicQuestion,
  patchPublicQuestion,
} from '@modelcity/engagement/lib/api/client';
import { canManageParticipation, canCreateQuestion } from '@modelcity/engagement/lib/auth/roles';
import { revalidatePath } from 'next/cache';

/* ─────────────────────────────────────────────────────────────────────────
 * Helpers
 * ───────────────────────────────────────────────────────────────────── */

/** Returns today's date as an ISO date string "YYYY-MM-DD". */
function todayIso() {
  return new Date().toISOString().split('T')[0];
}

/** Returns yesterday's date as an ISO date string "YYYY-MM-DD". */
function yesterdayIso() {
  const d = new Date();
  d.setUTCDate(d.getUTCDate() - 1);
  return d.toISOString().split('T')[0];
}

/* ─────────────────────────────────────────────────────────────────────────
 * createQuestion — admin only
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: creates a new civic consultation.
 * Only platform administrators may invoke this action.
 *
 * @param {{
 *   title:           Record<string, string>,
 *   description:     Record<string, string>,
 *   imageUrl?:       string,
 *   openDate:        string,
 *   closeDate:       string,
 *   zoneId:          number,
 *   neighbourhoodId: number,
 *   objectives:      Array<{ objective: Record<string, string>, sortOrder: number }>,
 * }} payload
 * @returns {Promise<{ ok: true, id: number | string } | { error: string }>}
 */
export async function createQuestion(payload) {
  const session = await auth0.getSession();
  if (!canCreateQuestion(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await createPublicQuestion(payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'create_failed' };
  }

  revalidatePath('/[lang]/participation/questions', 'page');
  return { ok: true, id: result.data?.id };
}

/* ─────────────────────────────────────────────────────────────────────────
 * updateQuestion — admin / backoffice, future questions only
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: fully updates a future civic consultation.
 * Accessible to platform administrators and backoffice staff.
 *
 * @param {string | number} id
 * @param {object} payload  – CivicQuestionRequestDto fields
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function updateQuestion(id, payload) {
  const session = await auth0.getSession();
  if (!canManageParticipation(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await updatePublicQuestion(id, payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'update_failed' };
  }

  revalidatePath('/[lang]/participation/questions', 'page');
  revalidatePath(`/[lang]/participation/questions/${id}`, 'page');
  return { ok: true };
}

/* ─────────────────────────────────────────────────────────────────────────
 * openQuestion — admin / backoffice (future → active)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: starts a future consultation by setting its openDate to today.
 * Only accessible to platform administrators and backoffice staff.
 *
 * @param {string | number} id
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function openQuestion(id) {
  const session = await auth0.getSession();
  if (!canManageParticipation(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await patchPublicQuestion(id, { openDate: todayIso() }, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'open_failed' };
  }

  revalidatePath('/[lang]/participation/questions', 'page');
  revalidatePath(`/[lang]/participation/questions/${id}`, 'page');
  return { ok: true };
}

/* ─────────────────────────────────────────────────────────────────────────
 * closeQuestion — admin / backoffice (active → past)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: closes an active consultation by setting its closeDate to today.
 * Only accessible to platform administrators and backoffice staff.
 *
 * @param {string | number} id
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function closeQuestion(id) {
  const session = await auth0.getSession();
  if (!canManageParticipation(session)) {
    return { error: 'forbidden' };
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await patchPublicQuestion(id, { closeDate: yesterdayIso() }, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'close_failed' };
  }

  revalidatePath('/[lang]/participation/questions', 'page');
  revalidatePath(`/[lang]/participation/questions/${id}`, 'page');
  return { ok: true };
}

