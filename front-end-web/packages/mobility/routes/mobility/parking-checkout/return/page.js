import Link from 'next/link';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { isCitizen } from '@modelcity/core/lib/auth/roles';
import { finalizeParkingCheckout } from '@modelcity/mobility/lib/actions/mobilityStripeCheckout';
import Icon from '@modelcity/core/components/atoms/Icon';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.mobility');

export const dynamic = 'force-dynamic';

/**
 * Stripe embedded-checkout return target for parking reservations.
 * `/{lang}/mobility/parking-checkout/return`
 *
 * Stripe redirects here with `?session_id=cs_...` once the citizen finishes
 * payment. We verify the session server-side and ask the mobility backend to
 * create or renew the reservation.
 *
 * @param {{
 *   params: Promise<{ lang: string }>,
 *   searchParams: Promise<{ session_id?: string }>,
 * }} props
 */
export default async function ParkingCheckoutReturnPage({ params, searchParams }) {
  const { lang } = await params;
  const sp = await searchParams;

  const dict = await getDictionary(lang, 'mobility');
  const t = dict.mobility.mobilityCheckout;

  const session = await auth0.getSession();
  if (!isCitizen(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const sessionId = sp.session_id;
  const result = sessionId
    ? await finalizeParkingCheckout(sessionId)
    : { error: 'missing_session' };

  const success = result?.ok === true;
  const isRenewal = result?.type === 'renewal';

  let message = t.errorGeneric;
  let icon = 'error_outline';
  let iconClass = 'text-error';

  if (success) {
    message = isRenewal ? t.successRenewal : t.successNew;
    icon = 'check_circle';
    iconClass = 'text-tertiary';
  } else if (result?.error === 'missing_session') {
    message = t.missingSession;
  } else if (result?.error === 'not_complete' || result?.error === 'not_paid') {
    message = t.notPaid;
    iconClass = 'text-on-surface-variant';
    icon = 'pending';
  } else if (result?.error === 'forbidden') {
    message = t.forbidden;
  }

  return (
    <div className="py-xl flex flex-col items-center gap-md text-center">
      <div
        className={`w-16 h-16 rounded-full flex items-center justify-center ${
          success ? 'bg-secondary-container' : 'bg-error-container'
        }`}
      >
        <Icon name={icon} fill size={36} className={iconClass} />
      </div>

      <div className="flex flex-col gap-xs max-w-prose">
        <h1 className="text-h2 text-primary">
          {success ? t.titleSuccess : t.titleError}
        </h1>
        <p className="text-body-lg text-on-surface-variant">{message}</p>
      </div>

      <div className="flex flex-wrap items-center justify-center gap-md mt-md">
        <Link
          href={`/${lang}/mobility/my-stays`}
          replace
          className="inline-flex items-center gap-sm px-md py-sm rounded-md bg-primary text-on-primary text-label-md font-semibold hover:opacity-90 transition-all shadow-sm"
        >
          <Icon name="receipt_long" size={18} />
          {t.viewMyStays}
        </Link>

        {!success && (
          <Link
            href={`/${lang}/mobility/reserve`}
            replace
            className="inline-flex items-center gap-sm px-md py-sm rounded-md border border-outline-variant text-on-surface hover:bg-surface-container transition-colors text-label-md"
          >
            <Icon name="arrow_back" size={18} />
            {t.backToReserve}
          </Link>
        )}
      </div>
    </div>
  );
}

