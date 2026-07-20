import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getUserById } from '@modelcity/core/lib/api/client';
import ProfileCard from '@modelcity/core/components/organisms/ProfileCard';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('profile.title');

export const dynamic = 'force-dynamic';

/**
 * Profile page.
 * Auth check and AppShell are handled by the parent (app)/layout.js.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function ProfilePage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'profile');

  const session = await auth0.getSession();
  const { user, tokenSet } = session;
  const profile = await getUserById(user.sub, tokenSet?.accessToken);

  return <ProfileCard authUser={user} profile={profile} dict={dict.profile} lang={lang} />;
}

