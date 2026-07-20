import Link from 'next/link';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canAccessAdministration } from '@modelcity/core/lib/auth/roles';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getUserById } from '@modelcity/core/lib/api/client';
import Icon from '@modelcity/core/components/atoms/Icon';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import UserDetailView from '@modelcity/core/components/organisms/UserDetailView';
import UserActivityPanel from '@modelcity/core/components/organisms/UserActivityPanel';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.administrationWorkers');

export const dynamic = 'force-dynamic';

/**
 * Administration → Worker detail.
 * Shows the worker's profile and their system-trail activity (filterable by
 * module, event type and date range).
 *
 * @param {{
 *   params: Promise<{ lang: string, id: string }>,
 *   searchParams: Promise<Record<string, string>>,
 * }} props
 */
export default async function WorkerDetailPage({ params, searchParams }) {
  const { lang, id } = await params;
  const sp = (await searchParams) ?? {};
  const userId = decodeURIComponent(id);

  const [session, dict] = await Promise.all([
    auth0.getSession(),
    getDictionary(lang, 'admin'),
  ]);

  if (!canAccessAdministration(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const detail = dict.admin.userDetail;
  const common = dict.admin.common;
  const accessToken = session.tokenSet?.accessToken;
  const backHref = `/${lang}/administration/workers`;

  const profile = await getUserById(userId, accessToken);

  if (!profile) {
    return (
      <div className="flex flex-col items-center gap-md py-2xl text-center">
        <Icon name="person_off" size={48} className="text-on-surface-variant" />
        <h1 className="text-h2 text-primary font-semibold">{detail.notFoundTitle}</h1>
        <p className="text-body-md text-on-surface-variant">{detail.notFoundBody}</p>
        <Link
          href={backHref}
          className="inline-flex items-center gap-xs px-md py-sm rounded-md bg-primary text-on-primary text-label-md font-semibold hover:opacity-90 transition-opacity"
        >
          <Icon name="arrow_back" size={18} />
          {detail.notFoundAction}
        </Link>
      </div>
    );
  }

  return (
    <UserDetailView
      profile={profile}
      lang={lang}
      backHref={backHref}
      detailLabels={detail}
      commonLabels={common}
    >
      <UserActivityPanel
        userId={userId}
        basePath={`/administration/workers/${id}`}
        sp={sp}
        accessToken={accessToken}
        lang={lang}
        sectionTitle={detail.eventsTitle}
        eventLabels={dict.admin.events}
        pagerLabels={common}
      />
    </UserDetailView>
  );
}
