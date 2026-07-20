'use client';

import { useCallback } from 'react';
import StripeCheckoutEmbed from '@modelcity/core/components/molecules/StripeCheckoutEmbed';
import { createEventCheckoutSession } from '@modelcity/leisure/lib/actions/stripeCheckout';

/**
 * Molecule: EventCheckoutEmbed
 *
 * Leisure-owned wrapper around the generic {@link StripeCheckoutEmbed}. It binds
 * the event-ticket checkout flow to Stripe by deriving `fetchClientSecret` from
 * the leisure `createEventCheckoutSession` server action, keeping the
 * leisure-specific dependency inside the leisure module instead of leaking into
 * the core component.
 *
 * @param {{
 *   eventId: number | string,
 *   lang: string,
 *   labels: { misconfigured: string },
 * }} props
 */
export default function EventCheckoutEmbed({ eventId, lang, labels }) {
  // Called once by EmbeddedCheckoutProvider on mount.
  const fetchClientSecret = useCallback(
    () =>
      createEventCheckoutSession(eventId, lang).then((result) => {
        if (result?.clientSecret) return result.clientSecret;
        throw new Error(result?.error ?? 'unknown_error');
      }),
    [eventId, lang],
  );

  return <StripeCheckoutEmbed fetchClientSecret={fetchClientSecret} labels={labels} />;
}
