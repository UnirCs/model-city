/**
 * Request header the proxy uses to propagate the **active locale** — the
 * `[lang]` segment of the URL — to Server Components and server-side API
 * clients.
 *
 * The URL is the authoritative source of the active language, so backend
 * content negotiation (`Accept-Language`) must follow it rather than the saved
 * `NEXT_LANG` preference cookie. The proxy sets this header on the forwarded
 * request; `serverFetch` reads it back via `headers()`.
 */
export const APP_LOCALE_HEADER = 'x-app-locale';
