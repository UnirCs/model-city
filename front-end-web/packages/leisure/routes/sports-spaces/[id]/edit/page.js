import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManagePublicSpaces } from '@modelcity/leisure/lib/auth/roles';
import { getPublicSpace } from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import PublicSpaceForm from '@modelcity/leisure/components/organisms/PublicSpaceForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.bookingsCreateSpace');

export const dynamic = 'force-dynamic';

/**
 * Edit public space page — `/{lang}/sports-spaces/{id}/edit`.
 * Admin-only.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function EditPublicSpacePage({ params }) {
  const { lang, id } = await params;
  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.spaceForm;
  const tDetail = dict.leisure.spaceDetail;

  const session = await auth0.getSession();
  if (!canManagePublicSpaces(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const result = await getPublicSpace(id, accessToken, true);

  if ('error' in result) {
    return <InlineStatus icon="error_outline" tone="error" message={tDetail.loadError} />;
  }
  if ('notFound' in result) {
    return <InlineStatus icon="search_off" message={tDetail.notFound} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />
      <IconPageHeader icon="edit_location" title={t.pageTitleEdit} subtitle={t.pageSubtitleEdit} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <PublicSpaceForm mode="edit" space={result.data} t={t} lang={lang} />
      </div>
    </div>
  );
}

