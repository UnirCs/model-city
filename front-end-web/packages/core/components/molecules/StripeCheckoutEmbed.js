'use client';

import { loadStripe } from '@stripe/stripe-js';
import {
  EmbeddedCheckoutProvider,
  EmbeddedCheckout,
} from '@stripe/react-stripe-js';
import Icon from '@modelcity/core/components/atoms/Icon';

const PUBLISHABLE_KEY = process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY;

// Singleton — loadStripe must only be called once at module level.
const stripePromise = PUBLISHABLE_KEY ? loadStripe(PUBLISHABLE_KEY) : null;

/**
 * Molecule: StripeCheckoutEmbed
 *
 * Generic Stripe Embedded Checkout wrapper. The caller supplies a
 * `fetchClientSecret` function that `EmbeddedCheckoutProvider` invokes once on
 * mount to obtain the checkout session's client secret, following the official
 * quickstart pattern (which eliminates any risk of an infinite re-render loop).
 *
 * This molecule is intentionally payment-domain-agnostic: every feature module
 * (event tickets, parking reservations, …) owns its own checkout-session server
 * action and passes the resulting `fetchClientSecret`, so this core component
 * never depends on a feature module. See, for example,
 * `components/molecules/leisure/EventCheckoutEmbed`.
 *
 * @param {{
 *   fetchClientSecret: () => Promise<string>,
 *   labels: {
 *     misconfigured: string,
 *   },
 * }} props
 */
export default function StripeCheckoutEmbed({ fetchClientSecret, labels }) {
  if (!PUBLISHABLE_KEY || !stripePromise) {
    return (
      <p className="flex items-center gap-xs text-body-sm text-error">
        <Icon name="error" size={16} />
        {labels.misconfigured}
      </p>
    );
  }

  return (
    <div className="overflow-hidden border border-outline-variant">
      <EmbeddedCheckoutProvider
        stripe={stripePromise}
        options={{ fetchClientSecret }}
      >
        <EmbeddedCheckout />
      </EmbeddedCheckoutProvider>
    </div>
  );
}
