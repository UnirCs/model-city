import { listSystemTrails } from '@modelcity/core/lib/api/systemTrails';
import { trailModuleById, isTrailModuleAvailable } from '@modelcity/core/lib/config/systemTrails';
import { dateToIso } from '@modelcity/core/lib/format';
import SystemTrailFilters from '@modelcity/core/components/molecules/SystemTrailFilters';
import SystemTrailList from '@modelcity/core/components/organisms/SystemTrailList';
import LocalizedPager from '@modelcity/core/components/molecules/LocalizedPager';

/**
 * Organism: UserActivityPanel (async server component)
 *
 * The system-trail panel embedded in a citizen/worker detail view: a module
 * selector (one enabled module at a time), event-type and date-range filters, a
 * full-width event list and a paginator — all scoped to a fixed
 * `responsibleUserId`. Centralises module resolution, fetching and pager href
 * building so both detail pages stay thin.
 *
 * @param {{
 *   userId: string,
 *   basePath: string,            // lang-relative, e.g. '/administration/citizens/{id}'
 *   sp: Record<string, string>,  // resolved searchParams
 *   accessToken: string,
 *   lang: string,
 *   sectionTitle?: string,       // heading shown inside the list container
 *   eventLabels: object,         // admin.events
 *   pagerLabels: object,         // admin.common (page/of/prev/next)
 * }} props
 */
export default async function UserActivityPanel({ userId, basePath, sp, accessToken, lang, sectionTitle, eventLabels, pagerLabels }) {
  const moduleId = isTrailModuleAvailable(sp.module) ? sp.module : 'core';
  const servicePath = trailModuleById(moduleId).servicePath;

  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const eventType = sp.eventType || undefined;
  const from = dateToIso(sp.from, 'start') ?? undefined;
  const to = dateToIso(sp.to, 'end') ?? undefined;

  const data = await listSystemTrails(
    { servicePath, eventType, responsibleUserId: userId, from, to, page },
    accessToken,
  );
  const fetchError = data == null;
  const events = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 0;

  const buildHref = (n) => {
    const qs = new URLSearchParams();
    qs.set('module', moduleId);
    if (eventType) qs.set('eventType', eventType);
    if (sp.from) qs.set('from', sp.from);
    if (sp.to) qs.set('to', sp.to);
    qs.set('page', String(n));
    return `${basePath}?${qs.toString()}`;
  };

  return (
    <div className="flex flex-col gap-md">
      <SystemTrailFilters
        labels={eventLabels}
        initial={{
          module: moduleId,
          eventType: sp.eventType ?? '',
          from: sp.from ?? '',
          to: sp.to ?? '',
        }}
      />
      <SystemTrailList
        events={events}
        lang={lang}
        fetchError={fetchError}
        title={sectionTitle}
        labels={eventLabels}
      />
      {!fetchError && (
        <LocalizedPager
          page={page}
          totalPages={totalPages}
          hrefBuilder={buildHref}
          labels={pagerLabels}
          ariaLabel="user events pagination"
        />
      )}
    </div>
  );
}
