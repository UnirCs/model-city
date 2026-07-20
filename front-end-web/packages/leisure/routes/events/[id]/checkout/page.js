import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canBuyEventTickets } from '@modelcity/leisure/lib/auth/roles';
import { getEvent } from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import EventCheckoutEmbed from '@modelcity/leisure/components/molecules/EventCheckoutEmbed';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.events');

export const dynamic = 'force-dynamic';

/**
 * Stripe embedded checkout page —
 * `/{lang}/events/{id}/checkout`.
 *
 * Citizens land here from the event detail page when the event is paid.
 * Renders Stripe's `EmbeddedCheckout` (loaded client-side) that fetches the
 * `client_secret` from our `createEventCheckoutSession` server action.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function EventCheckoutPage({ params }) {
  const { lang, id } = await params;

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.checkout;
  const tDetail = dict.leisure.eventDetail;

  const session = await auth0.getSession();
  if (!canBuyEventTickets(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const evRes = await getEvent(id, accessToken);

  if ('notFound' in evRes) {
    return <InlineStatus icon="search_off" message={tDetail.notFound} />;
  }
  if ('error' in evRes) {
    return <InlineStatus icon="error_outline" tone="error" message={tDetail.loadError} />;
  }

  const event = evRes.data;
  if (!event.requiresTicket || !event.paid) {
    return <InlineStatus icon="info" message={t.notPayable} />;
  }
  if (event.acquired) {
    return <InlineStatus icon="check_circle" message={tDetail.alreadyAcquired} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      <IconPageHeader icon="shopping_cart_checkout" title={t.pageTitle} subtitle={event.name} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <EventCheckoutEmbed eventId={event.id} lang={lang} labels={t} />
      </div>
    </div>
  );
}

