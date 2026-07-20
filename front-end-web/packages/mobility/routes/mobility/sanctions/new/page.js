import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canIssueSanctions } from '@modelcity/mobility/lib/auth/roles';
import Icon from '@modelcity/core/components/atoms/Icon';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';
import CreateSanctionForm from '@modelcity/mobility/components/organisms/CreateSanctionForm';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobilitySanctions');

export const dynamic = 'force-dynamic';

/**
 * Parses a coordinate query string into a number or returns null when the
 * value is missing / not a valid finite number.
 */
function parseCoord(value) {
  if (value == null || value === '') return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

/**
 * Staff page — `/{lang}/mobility/sanctions/new`
 *
 * Form for issuing a new sanction. Accepts query params (license plate +
 * coordinates) so it can be deep-linked from the active-stays table with
 * the ticket information pre-filled.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{
 *     licensePlate?: string,
 *     latitude?: string,
 *     longitude?: string,
 *   }>,
 * }} props
 */
export default async function NewSanctionPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = (await searchParams) ?? {};
  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.sanctionForm;

  const session = await auth0.getSession();
  if (!canIssueSanctions(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      <header className="flex items-center gap-sm">
        <div className="w-10 h-10 rounded-md bg-error/10 flex items-center justify-center shrink-0">
          <Icon name="gavel" fill size={24} className="text-error" />
        </div>
        <div>
          <h1 className="text-h2 text-primary font-semibold">{t.pageTitle}</h1>
          <p className="text-body-sm text-on-surface-variant" style={{ maxWidth: '60ch' }}>
            {t.pageSubtitle}
          </p>
        </div>
      </header>

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <CreateSanctionForm
          labels={t}
          lang={lang}
          initial={{
            licensePlate: sp.licensePlate ?? '',
            latitude:     parseCoord(sp.latitude),
            longitude:    parseCoord(sp.longitude),
          }}
        />
      </div>
    </div>
  );
}

