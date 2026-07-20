import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageSecurityAlerts } from '@modelcity/engagement/lib/auth/roles';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import SecurityAlertForm from '@modelcity/engagement/components/organisms/SecurityAlertForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import barrios from '@modelcity/core/lib/config/neighbourhoods';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.securityAlerts');

export const dynamic = 'force-dynamic';

/**
 * Create Security Alert page — `/{lang}/security/alerts/new`
 *
 * Accessible only to backoffice staff and platform administrators.
 * Hosts the {@link SecurityAlertForm} where the map picker drives the
 * latitude / longitude values.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function CreateAlertPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'security');
  const t = dict.security.alertForm;

  const session = await auth0.getSession();
  if (!canManageSecurityAlerts(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      <IconPageHeader icon="add_alert" title={t.pageTitleCreate} subtitle={t.pageSubtitleCreate} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <SecurityAlertForm
          t={t}
          tSeverity={dict.security.severity}
          barrios={barrios}
          lang={lang}
        />
      </div>
    </div>
  );
}

