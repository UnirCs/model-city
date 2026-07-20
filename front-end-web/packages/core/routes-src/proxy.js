import { NextResponse } from 'next/server';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { SUPPORTED_LANGS, DEFAULT_LANG } from '@modelcity/core/lib/i18n/dictionaries';
import { APP_LOCALE_HEADER } from '@modelcity/core/lib/i18n/localeHeader';

/** Cookie name used to persist the user's language preference. */
const LANG_COOKIE = 'NEXT_LANG';

/**
 * Detects the preferred language from cookie then Accept-Language header.
 *
 * @param {import('next/server').NextRequest} request
 * @returns {string} A supported language code.
 */
function detectLang(request) {
  // 1. Explicit cookie set by the language switcher.
  const cookieLang = request.cookies.get(LANG_COOKIE)?.value;
  if (cookieLang && SUPPORTED_LANGS.includes(cookieLang)) return cookieLang;

  // 2. Browser Accept-Language header (e.g. "fr-FR,fr;q=0.9,en;q=0.8").
  const acceptLang = request.headers.get('accept-language') ?? '';
  for (const segment of acceptLang.split(',')) {
    const primary = segment.split(';')[0].trim().split('-')[0].toLowerCase();
    if (SUPPORTED_LANGS.includes(primary)) return primary;
  }

  return DEFAULT_LANG;
}

/**
 * Next.js Proxy (formerly Middleware).
 *
 * Responsibilities:
 *   1. Redirect bare paths (without a lang prefix) to /{lang}/path.
 *   2. Delegate all Auth0 route handling to the SDK middleware.
 *
 * Auth0 handles:
 *   GET  /auth/login        → Redirect to Auth0 Universal Login
 *   GET  /auth/logout       → Sign out from Auth0 and clear session cookie
 *   GET  /auth/callback     → Exchange OAuth code for session
 *   GET  /auth/profile      → Return authenticated user profile (JSON)
 *   GET  /auth/access-token → Return current access token
 */
export async function proxy(request) {
  const { pathname } = request.nextUrl;

  // Skip Auth0 routes and Next.js internals — handled downstream.
  const isAuth =
    pathname.startsWith('/auth/') ||
    pathname.startsWith('/_next/');

  if (!isAuth) {
    // The explicit locale in the URL is authoritative for the active language.
    // The cookie preference is only consulted when the path carries no locale,
    // so deep links like /en/page always render in their own language even if
    // the user's saved preference differs.
    const hasLangPrefix = SUPPORTED_LANGS.some(
      (l) => pathname === `/${l}` || pathname.startsWith(`/${l}/`),
    );

    if (!hasLangPrefix) {
      const lang = detectLang(request);
      const url = request.nextUrl.clone();
      // "/" → "/{lang}", "/home" → "/{lang}/home", etc.
      url.pathname = `/${lang}${pathname === '/' ? '' : pathname}`;
      return NextResponse.redirect(url);
    }
  }

  // Delegate session handling to the Auth0 SDK first.
  const authRes = await auth0.middleware(request);

  // Auth/internal routes, or any redirect the SDK issued, pass through
  // untouched.
  if (isAuth || authRes.headers.get('location')) {
    return authRes;
  }

  // For app routes, propagate the active locale (the URL's [lang] segment) to
  // Server Components and serverFetch via a request header, so backend content
  // negotiation follows the URL rather than the saved NEXT_LANG cookie. The
  // SDK's response (rolling-session cookies, cache-control) is carried over.
  const requestHeaders = new Headers(request.headers);
  requestHeaders.set(APP_LOCALE_HEADER, pathname.split('/')[1]);

  const res = NextResponse.next({ request: { headers: requestHeaders } });
  authRes.cookies.getAll().forEach((cookie) => res.cookies.set(cookie));
  return res;
}

export const config = {
  matcher: [
    // Run on all paths except static assets.
    '/((?!_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)',
  ],
};
