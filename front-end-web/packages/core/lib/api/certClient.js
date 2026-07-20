/**
 * Browser-side API client for Core microservice endpoints that require
 * the browser to present a TLS client certificate (mTLS / FNMT digital cert).
 *
 * The request goes directly to the Application Load Balancer (ALB), bypassing
 * the Next.js server, so the **browser** — not the server — performs the TLS
 * handshake and presents the user's installed client certificate.
 *
 * Environment variables:
 *   NEXT_PUBLIC_MICROSERVICE_ALB_URL – direct ALB base URL (default: https://localhost:8443)
 *
 * ⚠️  Client Components only ('use client'). Importing this in a Server
 *     Component or Server Action will break certificate verification because
 *     the server has no access to the user's browser certificate store.
 */

import { CORRELATION_HEADER, newCorrelationId } from '@modelcity/core/lib/observability/correlation';

const ALB_BASE = process.env.NEXT_PUBLIC_MICROSERVICE_ALB_URL ?? 'https://localhost:8443';

/**
 * Attempts to verify the user's FNMT digital certificate via the ALB endpoint.
 *
 * The browser automatically presents any installed client certificate during
 * the mTLS handshake with the ALB. The response contains the extracted DNI
 * when a valid certificate was presented, plus an opaque verification token
 * that must be forwarded to downstream authorization requests.
 *
 * @param {string} accessToken  – Auth0 Bearer JWT so the gateway can inject X-Auth-Sub.
 * @returns {Promise<{ valid: boolean, verificationToken?: string }>}
 */
export async function verifyCertificate(accessToken) {
  try {
    const res  = await fetch(`${ALB_BASE}/api/core/users/certificate/verify`, {
      method: 'GET',
      headers: {
        [CORRELATION_HEADER]: newCorrelationId(),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
    });
    const data = res.ok ? await res.json() : null;
    const valid = res.ok && typeof data?.dni === 'string' && data.dni.trim().length > 0;
    return {
      valid,
      verificationToken: data?.verificationToken,
    };
  } catch {
    return { valid: false };
  }
}

