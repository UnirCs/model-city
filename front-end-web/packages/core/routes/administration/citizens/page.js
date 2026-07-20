import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canAccessAdministration } from '@modelcity/core/lib/auth/roles';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getUsers } from '@modelcity/core/lib/api/client';
import { setUserStatusAction } from '@modelcity/core/lib/actions/users';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import AdminUserFilters from '@modelcity/core/components/molecules/AdminUserFilters';
import UserCardGrid from '@modelcity/core/components/organisms/UserCardGrid';
import LocalizedPager from '@modelcity/core/components/molecules/LocalizedPager';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.administrationCitizens');

export const dynamic = 'force-dynamic';

/**
 * Administration → Citizens.
 * Small-card grid (5 per row, 20 per page) of enrolled citizens with name /
 * neighbourhood filters, account enable-disable and a link to each detail view.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ name?: string, neighbourhoodId?: string, page?: string }>,
 * }} props
 */
export default async function AdministrationCitizensPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = (await searchParams) ?? {};

  const [session, dict] = await Promise.all([
    auth0.getSession(),
    getDictionary(lang, 'admin'),
  ]);

  if (!canAccessAdministration(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const t = dict.admin.citizens;
  const common = dict.admin.common;
  const accessToken = session.tokenSet?.accessToken;

  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const name = sp.name?.trim() || undefined;
  const neighbourhoodId = sp.neighbourhoodId || undefined;

  const data = await getUsers({ citizen: true, name, neighbourhoodId, page }, accessToken);
  const fetchError = data == null;
  const users = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 0;

  const buildHref = (n) => {
    const qs = new URLSearchParams();
    if (name) qs.set('name', name);
    if (neighbourhoodId) qs.set('neighbourhoodId', neighbourhoodId);
    qs.set('page', String(n));
    return `/administration/citizens?${qs.toString()}`;
  };

  return (
    <div className="space-y-lg">
      <IconPageHeader icon="groups" title={t.title} subtitle={t.subtitle} />

      <AdminUserFilters
        mode="citizens"
        labels={{ ...t, roleLabels: common.roleLabels }}
        initial={{ name: sp.name ?? '', neighbourhoodId: sp.neighbourhoodId ?? '' }}
      />

      {fetchError && <FetchErrorBanner message={t.loadError} />}

      {!fetchError && (
        <>
          <UserCardGrid
            users={users}
            lang={lang}
            mode="citizens"
            detailBasePath="/administration/citizens"
            onToggleStatus={setUserStatusAction}
            labels={{ ...common, empty: t.empty }}
          />
          <LocalizedPager
            page={page}
            totalPages={totalPages}
            hrefBuilder={buildHref}
            labels={common}
            ariaLabel="citizens pagination"
          />
        </>
      )}
    </div>
  );
}
