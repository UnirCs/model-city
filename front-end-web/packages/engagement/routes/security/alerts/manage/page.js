import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageSecurityAlerts } from '@modelcity/engagement/lib/auth/roles';
import { getSecurityAlerts } from '@modelcity/engagement/lib/api/client';
import { getZoneName, getNeighbourhoodName } from '@modelcity/engagement/lib/security/geo';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import SecurityAlertsManageView from '@modelcity/engagement/components/organisms/SecurityAlertsManageView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.securityManage');

// Always SSR — admins need up-to-date data
export const dynamic = 'force-dynamic';

/**
 * Alert Management page — `/{lang}/security/alerts/manage`
 *
 * Restricted to backoffice / platform administrators. Fetches a page of alerts
 * and delegates rendering to the {@link SecurityAlertsManageView} organism.
 *
 * @param {{ params: Promise<{ lang: string }>, searchParams: Promise<{ page?: string }> }} props
 */
export default async function AlertManagePage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const dict = await getDictionary(lang, 'security');
  const t = dict.security.manage;

  const session = await auth0.getSession();
  if (!canManageSecurityAlerts(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const data = await getSecurityAlerts({ page, size: 4 }, accessToken);
  const fetchError = data == null;
  const safeAlerts = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  // Pre-compute zone / neighbourhood names server-side so the client island
  // doesn't need to ship the lookup table.
  const areas = Object.fromEntries(
    safeAlerts.map((a) => [a.id, {
      zoneName: getZoneName(a.zoneId),
      neighbourhoodName: getNeighbourhoodName(a.neighbourhoodId),
    }]),
  );

  return (
    <div className="space-y-lg">
      <div>
        <h1 className="text-h2 text-primary font-semibold">{t.bannerTitle}</h1>
        <p className="text-body-md text-on-surface-variant mt-xs">
          {t.bannerSubtitle}
        </p>
      </div>

      {fetchError && <FetchErrorBanner message={t.loadError} />}

      {!fetchError && (
        <SecurityAlertsManageView
          lang={lang}
          t={t}
          tSeverity={dict.security.severity}
          tCenter={dict.security.alertCenter}
          alerts={safeAlerts}
          areas={areas}
          page={page}
          totalPages={totalPages}
        />
      )}
    </div>
  );
}
