'use server';

import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getUserById, setUserStatus, createAgent } from '@modelcity/core/lib/api/client';

/* ─────────────────────────────────────────────────────────────────────────
 * User detail
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: fetches the full profile of any platform user by their
 * Auth0 sub. Intended for the admin user-detail panel.
 *
 * @param {string} userId  – Auth0 sub of the target user
 * @returns {Promise<{ data: object } | { error: string }>}
 */
export async function getUserDetailAction(userId) {
  const session = await auth0.getSession();
  if (!session) return { error: 'unauthenticated' };

  const data = await getUserById(userId, session.tokenSet?.accessToken);
  if (!data) return { error: 'fetch_failed' };
  return { data };
}

/* ─────────────────────────────────────────────────────────────────────────
 * Enable / disable account
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: enables or disables a user account (reversible).
 * Admin-only operation — callers should verify the role before invoking.
 *
 * @param {string} userId  – Auth0 sub of the target user
 * @param {'ACTIVE'|'DISABLED'} status
 * @returns {Promise<{ ok: true } | { error: string, status?: number }>}
 */
export async function setUserStatusAction(userId, status) {
  const session = await auth0.getSession();
  if (!session) return { error: 'unauthenticated' };

  return setUserStatus(userId, status, session.tokenSet?.accessToken);
}

/* ─────────────────────────────────────────────────────────────────────────
 * Agent creation  (admin)
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: creates a new municipal agent and triggers an invitation
 * e-mail via the core microservice (POST /agents → 202 Accepted).
 * Admin-only — callers should verify the role before invoking.
 *
 * @param {{ role: string, name: string, email: string }} payload
 * @returns {Promise<{ ok: true } | { error: string, status: number, detail: string | null }>}
 */
export async function createAgentAction(payload) {
  const session = await auth0.getSession();
  if (!session) return { error: 'unauthenticated' };

  return createAgent(payload, session.tokenSet?.accessToken);
}

