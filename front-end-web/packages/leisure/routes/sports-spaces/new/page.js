import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManagePublicSpaces } from '@modelcity/leisure/lib/auth/roles';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import Icon from '@modelcity/core/components/atoms/Icon';
import PublicSpaceForm from '@modelcity/leisure/components/organisms/PublicSpaceForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.bookingsCreateSpace');

export const dynamic = 'force-dynamic';

/**
 * Create public space page — `/{lang}/sports-spaces/new`.
 * Admin-only.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function CreatePublicSpacePage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.spaceForm;

  const session = await auth0.getSession();
  if (!canManagePublicSpaces(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />
      <div className="flex items-center gap-sm">
        <div className="w-10 h-10 rounded-md bg-primary/10 flex items-center justify-center shrink-0">
          <Icon name="add_business" fill size={24} className="text-primary" />
        </div>
        <div>
          <h1 className="text-h2 text-primary font-semibold">{t.pageTitleCreate}</h1>
          <p className="text-body-sm text-on-surface-variant">{t.pageSubtitleCreate}</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <PublicSpaceForm mode="create" t={t} lang={lang} />
      </div>
    </div>
  );
}

