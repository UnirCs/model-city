import Link from 'next/link';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canViewMobilityOps, canIssueSanctions } from '@modelcity/mobility/lib/auth/roles';
import { listSanctions } from '@modelcity/mobility/lib/api/client';
import { dateToIso } from '@modelcity/mobility/lib/utils/format';
import Icon from '@modelcity/core/components/atoms/Icon';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import MobilityFilters from '@modelcity/mobility/components/molecules/MobilityFilters';
import PaginationBar from '@modelcity/mobility/components/molecules/PaginationBar';
import SanctionsManageList from '@modelcity/mobility/components/organisms/SanctionsManageList';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobilitySanctions');

export const dynamic = 'force-dynamic';

/**
 * Staff page — `/{lang}/mobility/sanctions`
 *
 * Paginated registry of every sanction. Allows filtering by plate / date
 * range and links to the new-sanction form. Restricted to operators and
 * platform administrators.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{
 *     licensePlate?: string,
 *     fromDate?: string,
 *     toDate?: string,
 *     page?: string,
 *   }>,
 * }} props
 */
export default async function SanctionsManagePage({ params, searchParams }) {
  const { lang } = await params;
  const sp = (await searchParams) ?? {};
  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.sanctionsManage;
  const tDetail = dict.mobility.sanctionDetail;

  const session = await auth0.getSession();
  if (!canViewMobilityOps(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const canCreate = canIssueSanctions(session);
  const accessToken = session?.tokenSet?.accessToken;

  const page = Number(sp.page ?? 0) || 0;
  const result = await listSanctions(
    {
      licensePlate: sp.licensePlate?.trim() || undefined,
      from: dateToIso(sp.fromDate, 'start') ?? undefined,
      to:   dateToIso(sp.toDate,   'end')   ?? undefined,
      page,
    },
    accessToken,
  );

  const fetchError = result == null;
  const content    = result?.content ?? [];
  const totalPages = result?.page?.totalPages ?? 0;

  return (
    <div className="space-y-lg">
      <IconPageHeader
        icon="gavel"
        title={t.bannerTitle}
        subtitle={t.bannerSubtitle}
        iconWrapClassName="bg-error/10"
        iconClassName="text-error"
      />

      <MobilityFilters
        labels={t}
        initial={{
          licensePlate: sp.licensePlate ?? '',
          fromDate: sp.fromDate ?? '',
          toDate: sp.toDate ?? '',
        }}
      />

      {fetchError && <FetchErrorBanner message={t.loadError} />}

      {!fetchError && (
        <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
          <div className="flex items-center gap-sm mb-md">
            <Icon name="gavel" fill size={20} className="text-secondary" />
            <h2 className="text-h3 text-primary">{t.sectionTitle}</h2>
          </div>
          <SanctionsManageList
            sanctions={content}
            labels={t}
            detailLabels={tDetail}
            lang={lang}
            canCreate={canCreate}
            createButtonLabel={t.createButton}
            createHref={`/${lang}/mobility/sanctions/new`}
          />
          <PaginationBar page={page} totalPages={totalPages} labels={t} />
        </div>
      )}
    </div>
  );
}

