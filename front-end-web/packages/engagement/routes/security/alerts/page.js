import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getSecurityAlerts } from '@modelcity/engagement/lib/api/client';
import { canManageSecurityAlerts } from '@modelcity/engagement/lib/auth/roles';
import PageHeader from '@modelcity/core/components/molecules/PageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import AlertCenterView from '@modelcity/engagement/components/organisms/AlertCenterView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.securityAlerts');

// Always SSR — alerts must stay fresh
export const dynamic = 'force-dynamic';

/**
 * Alert Centre page — `/{lang}/security/alerts`
 *
 * Fetches a page of security alerts from the citizen-engagement microservice
 * and delegates rendering to the {@link AlertCenterView} organism. Visible to
 * every authenticated user.
 *
 * @param {{ params: Promise<{ lang: string }>, searchParams: Promise<{ page?: string }> }} props
 */
export default async function AlertCenterPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'security');
  const t = dict.security.alertCenter;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;
  const canCreate = canManageSecurityAlerts(session);

  const data = await getSecurityAlerts({ page }, accessToken);
  const fetchError = data == null;
  const safeAlerts = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  return (
    <>
      <PageHeader icon="shield" title={t.bannerTitle} subtitle={t.bannerSubtitle} />

      {fetchError && <FetchErrorBanner message={t.loadError} className="mb-xl" />}

      {!fetchError && (
        <AlertCenterView
          lang={lang}
          t={t}
          tSeverity={dict.security.severity}
          canCreate={canCreate}
          alerts={safeAlerts}
          page={page}
          totalPages={totalPages}
        />
      )}
    </>
  );
}
