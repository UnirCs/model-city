'use server';

import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { requestOperationAuthorization, verifyOtpCode } from '@modelcity/core/lib/actions/otp';
import { castVote as castVoteInCE } from '@modelcity/engagement/lib/api/client';

/** Operation constants for the public-question vote flow. */
const VOTE_OPERATION_TYPE = 'CONFIRM_ANSWER';
const VOTE_RESOURCE_TYPE  = 'public-question';

/**
 * Server Action: requests a vote authorization from the transaction-authorization
 * microservice, which triggers an OTP email to the citizen.
 *
 * @param {{ questionId: string | number, verificationToken?: string }} params
 * @returns {Promise<{ ok: true, operationAuthorizationId: string } | { error: string }>}
 */
export async function requestVoteAuthorization({ questionId, verificationToken }) {
  return requestOperationAuthorization({
    operationType: VOTE_OPERATION_TYPE,
    resourceType:  VOTE_RESOURCE_TYPE,
    resourceId:    String(questionId),
    verificationToken,
  });
}

/**
 * Server Action: verifies the OTP code and, if valid, submits the final vote.
 *
 * Steps:
 *   1. PATCH  /transaction-authorization/operation-authorizations/{id}  — verify OTP
 *   2. POST   /citizen-engagement/public-questions/{id}/answers          — cast vote
 *
 * @param {{
 *   operationAuthorizationId: string,
 *   otp:        string,
 *   questionId: string | number,
 *   vote:       'YES' | 'NO'
 * }} params
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function confirmVoteWithOtp({ operationAuthorizationId, otp, questionId, vote }) {
  /* ── Step 1: verify OTP (generic) ──────────────────────── */
  const otpResult = await verifyOtpCode({
    operationAuthorizationId,
    otp,
    operationType: VOTE_OPERATION_TYPE,
    resourceType:  VOTE_RESOURCE_TYPE,
    resourceId:    String(questionId),
  });

  if (!otpResult.ok) return otpResult;

  /* ── Step 2: cast vote via citizen-engagement client ────── */
  const session = await auth0.getSession();
  if (!session) return { error: 'unauthenticated' };

  const accessToken = session.tokenSet?.accessToken;
  const voteResult = await castVoteInCE(
    questionId,
    { operationAuthorizationId, vote },
    accessToken,
  );

  if (!voteResult.ok) {
    if (voteResult.status === 409) return { error: 'already_voted' };
    return { error: voteResult.body?.message ?? 'vote_failed' };
  }

  return { ok: true };
}
