import Link from 'next/link';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canBuyEventTickets } from '@modelcity/leisure/lib/auth/roles';
import { finalizeEventCheckout } from '@modelcity/leisure/lib/actions/stripeCheckout';
import Icon from '@modelcity/core/components/atoms/Icon';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.events');

export const dynamic = 'force-dynamic';

/**
 * Stripe embedded-checkout return target — `/{lang}/events/{id}/checkout/return`.
 *
 * Stripe redirects the browser here with `?session_id=cs_...` once the user
 * has finished the embedded checkout flow. We immediately verify the session
 * server-side and, on success, ask the backend to materialise the ticket.
 *
 * @param {{
 *   params: Promise<{ lang: string, id: string }>,
 *   searchParams: Promise<{ session_id?: string }>,
 * }} props
 */
export default async function CheckoutReturnPage({ params, searchParams }) {
  const { lang, id } = await params;
  const sp = await searchParams;

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.checkoutReturn;

  const session = await auth0.getSession();
  if (!canBuyEventTickets(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const sessionId = sp.session_id;
  const result = sessionId
    ? await finalizeEventCheckout(sessionId, id)
    : { error: 'missing_session' };

  const success = result?.ok === true;

  let message = t.errorGeneric;
  let icon = 'error_outline';
  let iconClass = 'text-error';

  if (success) {
    message = t.success;
    icon = 'check_circle';
    iconClass = 'text-tertiary';
  } else if (result?.error === 'missing_session') {
    message = t.missingSession;
  } else if (result?.error === 'not_complete' || result?.error === 'not_paid') {
    message = t.notPaid;
    iconClass = 'text-on-surface-variant';
    icon = 'pending';
  } else if (result?.error === 'event_mismatch') {
    message = t.mismatch;
  } else if (result?.error === 'forbidden') {
    message = t.forbidden;
  }

  return (
    <div className="py-xl flex flex-col items-center gap-md text-center">
      <Icon name={icon} size={56} className={`${iconClass} opacity-80`} />
      <h1 className="text-h2 text-primary">{success ? t.titleSuccess : t.titleError}</h1>
      <p className="text-body-lg text-on-surface-variant max-w-prose">{message}</p>

      <div className="flex flex-wrap items-center justify-center gap-md mt-md">
        {/* `replace` prevents the user from navigating back to this return page */}
        <Link
          href={`/${lang}/events/${id}`}
          replace
          className="inline-flex items-center gap-sm px-md py-sm rounded-md border border-outline-variant text-on-surface hover:bg-surface-container transition-colors text-label-md"
        >
          <Icon name="arrow_back" size={18} />
          {t.backToEvent}
        </Link>
        <Link
          href={`/${lang}/events/my-tickets`}
          replace
          className="inline-flex items-center gap-sm px-md py-sm rounded-md bg-primary text-on-primary text-label-md font-semibold hover:opacity-90 transition-all"
        >
          <Icon name="confirmation_number" size={18} />
          {t.viewMyTickets}
        </Link>
      </div>
    </div>
  );
}

