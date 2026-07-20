/**
 * Observability — request correlation.
 *
 * The backend inspects an inbound `X-Model-City-Correlation-Id` header at its
 * entry point: when the header is present it reuses that value instead of
 * generating a fresh correlationId, so a single id can be traced across the
 * whole frontend → backend → downstream-services chain.
 *
 * Every transaction initiated from the frontend must therefore attach this
 * header with a random UUID. This module holds the isomorphic primitives shared
 * by the server-side API clients (through `serverFetch`) and the browser-side
 * mTLS client (`certClient`).
 */

/** Header the backend reads to reuse an existing correlation id. */
export const CORRELATION_HEADER = 'X-Model-City-Correlation-Id';

/**
 * Generates a random correlation id. Uses the Web Crypto API, which is
 * available both in the Next.js server runtime and in the browser.
 *
 * @returns {string} RFC 4122 v4 UUID
 */
export function newCorrelationId() {
  return globalThis.crypto.randomUUID();
}

/**
 * Merges the correlation header on top of any caller-provided headers.
 *
 * @param {string} correlationId
 * @param {Record<string, string>} [headers={}]
 * @returns {Record<string, string>}
 */
export function withCorrelationId(correlationId, headers = {}) {
  return { ...headers, [CORRELATION_HEADER]: correlationId };
}
