/**
 * Server-side `fetch` wrapper that attaches the request correlation header.
 *
 * Used by every server-side API client so that all backend calls carry an
 * `X-Model-City-Correlation-Id` header (see `./correlation`).
 *
 * ⚠️  Server-only. Do NOT import from Client Components — for browser-initiated
 *     transactions generate an id per call with `newCorrelationId()` instead.
 */

import { cache } from 'react';
import { cookies, headers } from 'next/headers';
import { CORRELATION_HEADER, newCorrelationId } from './correlation';
import { DEFAULT_LANG, SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';
import { APP_LOCALE_HEADER } from '@modelcity/core/lib/i18n/localeHeader';

/**
 * Request-scoped correlation id.
 *
 * `cache` memoises the result for the lifetime of a single server request — a
 * Server Action invocation or a Server Component render — so every backend call
 * made while handling one frontend transaction shares the same correlation id.
 */
export const getCorrelationId = cache(() => newCorrelationId());

/**
 * Resolves the language used for backend content negotiation.
 *
 * The active locale is the one in the URL — the proxy forwards it as the
 * `APP_LOCALE_HEADER` request header — so it is authoritative. The saved
 * `NEXT_LANG` cookie is only a fallback for requests that did not pass through
 * the proxy (e.g. outside a normal app route).
 *
 * @returns {Promise<string>} A supported language code.
 */
async function resolveActiveLang() {
  // 1. Active locale from the URL, propagated by the proxy.
  try {
    const store = await headers();
    const fromUrl = store.get(APP_LOCALE_HEADER);
    if (fromUrl && SUPPORTED_LANGS.includes(fromUrl)) return fromUrl;
  } catch {
    // headers() throws outside a request context; ignore.
  }
  // 2. Fallback: saved preference cookie.
  try {
    const store = await cookies();
    const c = store.get('NEXT_LANG')?.value;
    if (c && SUPPORTED_LANGS.includes(c)) return c;
  } catch {
    // cookies() throws outside a request context; ignore.
  }
  return DEFAULT_LANG;
}

function DEBUG(label, data) {
  console.debug(`[DEBUG] ${label}`, data);
}

/**
 * Drop-in replacement for `fetch` used by the server-side API clients. Adds the
 * correlation header to the outgoing request, preserving any headers the caller
 * already set.
 *
 * Logs one DEBUG entry for the outgoing request and another for the response.
 *
 * @param {RequestInfo | URL} input
 * @param {RequestInit} [init={}]
 * @returns {Promise<Response>}
 */
export async function serverFetch(input, init = {}) {
  const url = typeof input === 'string' ? input : input.url ?? String(input);
  const method = init.method ?? 'GET';

  const correlationId = getCorrelationId();

  const lang = await resolveActiveLang();

  const outgoingHeaders = {
    ...init.headers,
    [CORRELATION_HEADER]: correlationId,
    'Accept-Language': lang,
  };

  DEBUG('request', {
    url,
    method,
    headers: outgoingHeaders,
    body: init.body,
    correlationId,
  });

  const response = await fetch(input, {
    ...init,
    headers: outgoingHeaders,
  });

  const cloned = response.clone();
  let body = null;
  try {
    body = await cloned.text();
  } catch {
    // ignore
  }

  DEBUG('response', {
    url,
    method,
    status: response.status,
    statusText: response.statusText,
    headers: Object.fromEntries(response.headers),
    body,
  });

  return response;
}
