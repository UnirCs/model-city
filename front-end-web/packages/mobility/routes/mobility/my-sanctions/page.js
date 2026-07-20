import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getUserSanctions } from '@modelcity/mobility/lib/api/client';
import Icon from '@modelcity/core/components/atoms/Icon';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import LocalizedPager from '@modelcity/core/components/molecules/LocalizedPager';
import MySanctionsList from '@modelcity/mobility/components/organisms/MySanctionsList';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobilityMySanctions');

export const dynamic = 'force-dynamic';

/**
 * Citizen page — `/{lang}/mobility/my-sanctions`
 *
 * Lists every sanction the current user has received. The base64 evidence
 * is loaded lazily through a Server Action when the user opens the detail
 * modal of any item.
 *
 * @param {{ params: Promise<{ lang: string }>, searchParams: Promise<{ page?: string }> }} props
 */
export default async function MySanctionsPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;
  const page = Math.max(0, parseInt(sp.page ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.mySanctions;

  const session = await auth0.getSession();
  const sub = session?.user?.sub;
  const accessToken = session?.tokenSet?.accessToken;

  const data = sub ? await getUserSanctions(sub, { page }, accessToken) : null;
  const fetchError = data == null && sub != null;
  const safe = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  return (
    <div className="space-y-lg">
      <IconPageHeader
        icon="gavel"
        title={t.bannerTitle}
        subtitle={t.bannerSubtitle}
        iconWrapClassName="bg-error/10"
        iconClassName="text-error"
      />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="flex items-center gap-sm mb-md">
          <Icon name="gavel" fill size={20} className="text-secondary" />
          <h2 className="text-h3 text-primary">{t.sectionTitle}</h2>
        </div>
        {fetchError ? (
          <FetchErrorBanner message={t.loadError} bare />
        ) : (
          <>
            <MySanctionsList
              sanctions={safe}
              labels={t}
              detailLabels={dict.mobility.sanctionDetail}
              lang={lang}
            />
            <LocalizedPager
              basePath="/mobility/my-sanctions"
              page={page}
              totalPages={totalPages}
              labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
              ariaLabel="sanctions pagination"
            />
          </>
        )}
      </div>
    </div>
  );
}
