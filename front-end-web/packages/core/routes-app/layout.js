import { redirect } from 'next/navigation';
import { checkUserExists } from '@modelcity/core/lib/api/client';
import AppShell from '@modelcity/core/components/templates/AppShell';
import {auth0} from "@modelcity/core/lib/auth/auth0";

/**
 * Every authenticated page redirects to login for anonymous visitors, so none
 * of them should be indexed. Declaring `noindex` once here is inherited by all
 * pages in the group; public pages live outside `(app)` and keep the default
 * indexable metadata.
 *
 * @type {import('next').Metadata}
 */
export const metadata = {
  robots: { index: false, follow: false },
};

/**
 * Layout shared by all authenticated app pages — the `(app)` route group's
 * session/registration gate. It lives in `packages/core/routes-app/` so the
 * codegen emits its shim at `[lang]/(app)/layout.js`; keeping it here (rather
 * than hand-written in the orchestrator) makes the orchestrator's route tree
 * entirely generated. The gate stays a `core` concern.
 *
 * Centralises the session check and renders the AppShell (TopNavBar + SideNavBar).
 * Any page inside (app)/ only needs to render its own content section.
 *
 * Guard logic (in order):
 *   1. No Auth0 session → redirect to login.
 *   2. Auth0 session exists but token is expired (401) → render app with session expired modal.
 *   3. Auth0 session exists but no backend profile → redirect to registration.
 *      A valid Auth0 session alone is not enough: the citizen registration form
 *      must have been completed so that a profile exists in the core service.
 *   4. Backend profile confirmed → render the app shell.
 *
 * The backend check is a lightweight HEAD request. On network error the helper
 * returns false so the user is redirected to registration/login as appropriate.
 *
 * @param {{ children: React.ReactNode, params: Promise<{ lang: string }> }} props
 */
export default async function AppLayout({ children, params }) {
  const { lang } = await params;

  const session = await auth0.getSession();

  if (!session) {
    console.log("No session, redirecting to login");
    redirect(`/auth/login?returnTo=${encodeURIComponent(`/${lang}/home`)}`);
  } else {
    const sub = session.user?.sub;
    const accessToken = session.tokenSet?.accessToken;
    console.log("Previous session found for ", sub);
    const { exists, expired } = await checkUserExists(sub, accessToken);

    if (expired) {
      console.log("Session token expired for ", sub);
      return <AppShell lang={lang} sessionExpired={true}>{children}</AppShell>;
    }

    if (!exists) {
      console.log("Session found but no database record for {} ", sub);
      redirect(`/${lang}/register`);
    }
  }
  return <AppShell lang={lang} sessionExpired={false}>{children}</AppShell>;
}
