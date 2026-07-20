import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canAccessAdministration } from '@modelcity/core/lib/auth/roles';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { listSystemTrails } from '@modelcity/core/lib/api/systemTrails';
import { trailModuleById, isTrailModuleAvailable } from '@modelcity/core/lib/config/systemTrails';
import { dateToIso } from '@modelcity/core/lib/format';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import SystemTrailFilters from '@modelcity/core/components/molecules/SystemTrailFilters';
import SystemTrailList from '@modelcity/core/components/organisms/SystemTrailList';
import LocalizedPager from '@modelcity/core/components/molecules/LocalizedPager';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.administrationRecords');

export const dynamic = 'force-dynamic';

/**
 * Administration → Records.
 * Read-only viewer of system trails, queried one module at a time (only enabled
 * modules are selectable). Supports filtering by event type, responsible user
 * id and date range, paginated at 20 per page (full-width rows).
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{
 *     module?: string, eventType?: string, responsibleUserId?: string,
 *     from?: string, to?: string, page?: string,
 *   }>,
 * }} props
 */
export default async function AdministrationRecordsPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = (await searchParams) ?? {};

  const [session, dict] = await Promise.all([
    auth0.getSession(),
    getDictionary(lang, 'admin'),
  ]);

  if (!canAccessAdministration(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const t = dict.admin.events;
  const common = dict.admin.common;
  const accessToken = session.tokenSet?.accessToken;

  // Resolve module: keep the requested one only if it is currently enabled.
  const moduleId = isTrailModuleAvailable(sp.module) ? sp.module : 'core';
  const servicePath = trailModuleById(moduleId).servicePath;

  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const eventType = sp.eventType || undefined;
  const responsibleUserId = sp.responsibleUserId?.trim() || undefined;
  const from = dateToIso(sp.from, 'start') ?? undefined;
  const to = dateToIso(sp.to, 'end') ?? undefined;

  const data = await listSystemTrails(
    { servicePath, eventType, responsibleUserId, from, to, page },
    accessToken,
  );
  const fetchError = data == null;
  const events = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 0;

  const buildHref = (n) => {
    const qs = new URLSearchParams();
    qs.set('module', moduleId);
    if (eventType) qs.set('eventType', eventType);
    if (responsibleUserId) qs.set('responsibleUserId', responsibleUserId);
    if (sp.from) qs.set('from', sp.from);
    if (sp.to) qs.set('to', sp.to);
    qs.set('page', String(n));
    return `/administration/records?${qs.toString()}`;
  };

  return (
    <div className="space-y-lg">
      <IconPageHeader icon="receipt_long" title={t.title} subtitle={t.subtitle} />

      <SystemTrailFilters
        labels={t}
        showResponsible
        initial={{
          module: moduleId,
          eventType: sp.eventType ?? '',
          responsibleUserId: sp.responsibleUserId ?? '',
          from: sp.from ?? '',
          to: sp.to ?? '',
        }}
      />

      <SystemTrailList events={events} lang={lang} fetchError={fetchError} labels={t} />

      {!fetchError && (
        <LocalizedPager
          page={page}
          totalPages={totalPages}
          hrefBuilder={buildHref}
          labels={common}
          ariaLabel="records pagination"
        />
      )}
    </div>
  );
}
