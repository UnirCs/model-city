import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canAccessAdministration } from '@modelcity/core/lib/auth/roles';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getUsers } from '@modelcity/core/lib/api/client';
import { setUserStatusAction, createAgentAction } from '@modelcity/core/lib/actions/users';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import AdminUserFilters from '@modelcity/core/components/molecules/AdminUserFilters';
import UserCardGrid from '@modelcity/core/components/organisms/UserCardGrid';
import AddWorkerButton from '@modelcity/core/components/organisms/AddWorkerButton';
import LocalizedPager from '@modelcity/core/components/molecules/LocalizedPager';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.administrationWorkers');

export const dynamic = 'force-dynamic';

/**
 * Administration → Workers.
 * Small-card grid (5 per row, 20 per page) of operators, backoffice and
 * mobility agents with name / role filters, access enable-disable, a link to
 * each detail view and the "Add municipal operator" action.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ name?: string, role?: string, page?: string }>,
 * }} props
 */
export default async function AdministrationWorkersPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = (await searchParams) ?? {};

  const [session, dict] = await Promise.all([
    auth0.getSession(),
    getDictionary(lang, 'admin'),
  ]);

  if (!canAccessAdministration(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const t = dict.admin.workers;
  const common = dict.admin.common;
  const accessToken = session.tokenSet?.accessToken;

  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);
  const name = sp.name?.trim() || undefined;
  const role = sp.role || undefined;

  const data = await getUsers({ citizen: false, name, role, page }, accessToken);
  const fetchError = data == null;
  const users = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 0;

  const buildHref = (n) => {
    const qs = new URLSearchParams();
    if (name) qs.set('name', name);
    if (role) qs.set('role', role);
    qs.set('page', String(n));
    return `/administration/workers?${qs.toString()}`;
  };

  return (
    <div className="space-y-lg">
      <div className="flex items-start justify-between gap-md flex-wrap">
        <IconPageHeader icon="badge" title={t.title} subtitle={t.subtitle} />
        <AddWorkerButton
          onCreateAgent={createAgentAction}
          buttonLabel={t.addWorker}
          modalLabels={dict.admin.createAgent}
        />
      </div>

      <AdminUserFilters
        mode="workers"
        labels={{ ...t, roleLabels: common.roleLabels }}
        initial={{ name: sp.name ?? '', role: sp.role ?? '' }}
      />

      {fetchError && <FetchErrorBanner message={t.loadError} />}

      {!fetchError && (
        <>
          <UserCardGrid
            users={users}
            lang={lang}
            mode="workers"
            detailBasePath="/administration/workers"
            onToggleStatus={setUserStatusAction}
            labels={{ ...common, empty: t.empty }}
          />
          <LocalizedPager
            page={page}
            totalPages={totalPages}
            hrefBuilder={buildHref}
            labels={common}
            ariaLabel="workers pagination"
          />
        </>
      )}
    </div>
  );
}
