import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { getAllUserCars } from '@modelcity/mobility/lib/api/client';
import Icon from '@modelcity/core/components/atoms/Icon';
import ReservationForm from '@modelcity/mobility/components/organisms/ReservationForm';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobility');

export const dynamic = 'force-dynamic';

/**
 * Citizen page — `/{lang}/mobility/reserve`
 *
 * Hosts the "register stay" workflow: map picker + car selector + duration
 * slider. The user's cars are fetched server-side so the form opens with
 * an immediately-usable car dropdown.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function ReserveStreetPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.reserve;

  const session = await auth0.getSession();
  const sub = session?.user?.sub;
  const accessToken = session?.tokenSet?.accessToken;

  const safeCars = sub ? await getAllUserCars(sub, accessToken) : [];

  return (
    <div className="space-y-lg">
      <header className="flex items-center gap-sm">
        <div className="w-10 h-10 rounded-md bg-primary/10 flex items-center justify-center shrink-0">
          <Icon name="local_parking" fill size={24} className="text-primary" />
        </div>
        <div>
          <h1 className="text-h2 text-primary font-semibold">{t.bannerTitle}</h1>
          <p className="text-body-sm text-on-surface-variant" style={{ maxWidth: '60ch' }}>
            {t.bannerSubtitle}
          </p>
        </div>
      </header>

      <ReservationForm cars={safeCars} labels={t} lang={lang} />
    </div>
  );
}

