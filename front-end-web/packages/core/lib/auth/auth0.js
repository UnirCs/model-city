import { Auth0Client } from "@auth0/nextjs-auth0/server";
import { NextResponse } from "next/server";
import { SUPPORTED_LANGS, DEFAULT_LANG } from "../i18n/dictionaries";
import { checkUserExists } from "@modelcity/core/lib/api/client";

/**
 * Extracts the language prefix from a returnTo path (e.g. "/es/home" → "es").
 * Falls back to DEFAULT_LANG if not found.
 *
 * @param {string | undefined} returnTo
 * @returns {string}
 */
function extractLang(returnTo) {
  if (!returnTo) return DEFAULT_LANG;
  const match = returnTo.match(/^\/([a-z]{2})(\/|$)/);
  const candidate = match?.[1];
  return candidate && SUPPORTED_LANGS.includes(candidate) ? candidate : DEFAULT_LANG;
}

/**
 * Auth0 client instance.
 * Automatically reads: AUTH0_DOMAIN, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET,
 * AUTH0_SECRET, APP_BASE_URL.
 */
export const auth0 = new Auth0Client({
  authorizationParameters: {
    // Audience registered in Auth0 → the Access Token will be a RS256-signed JWT
    // that Spring Boot can validate directly against the tenant's JWKS.
    audience: 'https://model-city.aranjuez.es',
    scope: 'openid profile email',
  },

  /**
   * onCallback — executes ONCE, right after Auth0 completes
   * the code-for-tokens exchange (Authorization Code Flow).
   *
   * Flow:
   *   1. OAuth error       → landing with error message
   *   2. Existing user     → /{lang}/home (or returnTo)
   *   3. New user          → /{lang}/completar-registro (registration form)
   *
   * The sign-in POST to the backend is done from /completar-registro
   * once the user completes their data, NOT here.
   *
   * @param {import("@auth0/nextjs-auth0/server").SdkError | null} error
   * @param {import("@auth0/nextjs-auth0/server").OnCallbackContext} ctx
   * @param {import("@auth0/nextjs-auth0/server").SessionData | null} session
   * @returns {Promise<NextResponse>}
   */
  async onCallback(error, ctx, session) {
    const base    = ctx.appBaseUrl ?? process.env.APP_BASE_URL ?? 'http://localhost:3000';
    const lang    = extractLang(ctx.returnTo);
    const returnTo = ctx.returnTo ?? `/${lang}/home`;

    // 1. OAuth flow error → redirect to landing with error message
    if (error) {
      console.error('[auth] onCallback error:', JSON.stringify({
        message: error.message,
        code:    error.code,
        cause:   String(error.cause ?? ''),
        status:  error.status,
      }, null, 2));
      return NextResponse.redirect(
        new URL(`/${lang}?error=${encodeURIComponent(error.message)}`, base)
      );
    }

    const sub         = session?.user?.sub;
    const accessToken = session?.tokenSet?.accessToken;

    // 2. Check if the user already exists in the backend
    if (sub && accessToken) {
      const { exists, expired } = await checkUserExists(sub, accessToken);
      if (expired) {
        // Token expired → redirect to logout (will go to landing page)
        return NextResponse.redirect(new URL('/auth/logout', base));
      }
      if (!exists) {
        // New user → complete registration form
        return NextResponse.redirect(new URL(`/${lang}/register`, base));
      }
    }

    // 3. Existing user → default destination
    return NextResponse.redirect(new URL(returnTo, base));
  },
});
