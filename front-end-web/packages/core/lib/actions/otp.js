'use server';

import { auth0 } from '@modelcity/core/lib/auth/auth0';
import {
  createOperationAuthorization,
  verifyOperationAuthorization,
} from '@modelcity/core/lib/api/client';

/**
 * Maps the HTTP response from the transaction-authorization PATCH endpoint
 * to a structured OTP error code that the UI can act on.
 *
 * @param {number} status  HTTP status code
 * @param {object} body    Parsed response body
 * @returns {string}       One of the `otp_*` error codes
 */
function parseOtpError(status, body) {
  const msg = body?.message ?? body?.error ?? '';

  if (status === 410) return 'otp_expired';
  if (status === 409) return 'otp_already_used';
  if (status === 403) return 'otp_mismatch';

  if (status === 422) {
    if (/No attempts remaining/i.test(msg)) return 'otp_no_attempts';
    if (/attempt.*remaining.*new code/i.test(msg) || /new code has been sent/i.test(msg)) {
      return 'otp_new_sent';
    }
    return 'otp_mismatch';
  }

  return 'otp_invalid';
}

/**
 * Generic server action: requests a new operation authorization from the
 * transaction-authorization microservice, which triggers an OTP email.
 *
 * @param {{
 *   operationType: string,
 *   resourceType:  string,
 *   resourceId:    string,
 *   verificationToken?: string
 * }} params
 * @returns {Promise<{ ok: true, operationAuthorizationId: string } | { error: string }>}
 */
export async function requestOperationAuthorization({ operationType, resourceType, resourceId, verificationToken }) {
  const session = await auth0.getSession();
  if (!session) return { error: 'unauthenticated' };

  const accessToken = session.tokenSet?.accessToken;
  const result = await createOperationAuthorization(
    { operationType, resourceType, resourceId, verificationToken },
    accessToken,
  );

  if (!result.ok) {
    return { error: result.body?.message ?? 'authorization_failed' };
  }

  return { ok: true, operationAuthorizationId: result.operationAuthorizationId };
}

/**
 * Generic server action: verifies an OTP code against an existing operation
 * authorization via PATCH on the transaction-authorization microservice.
 *
 * @param {{
 *   operationAuthorizationId: string,
 *   otp:            string,
 *   operationType:  string,
 *   resourceType:   string,
 *   resourceId:     string
 * }} params
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function verifyOtpCode({
  operationAuthorizationId,
  otp,
  operationType,
  resourceType,
  resourceId,
}) {
  const session = await auth0.getSession();
  if (!session) return { error: 'unauthenticated' };

  const accessToken = session.tokenSet?.accessToken;
  const result = await verifyOperationAuthorization(
    operationAuthorizationId,
    { otp, operationType, resourceType, resourceId },
    accessToken,
  );

  if (!result.ok) {
    return { error: parseOtpError(result.status, result.body) };
  }

  return { ok: true };
}

