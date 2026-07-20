'use server';

import { headers } from 'next/headers';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canBuyEventTickets } from '@modelcity/leisure/lib/auth/roles';
import { getEvent, purchaseEventTicket } from '@modelcity/leisure/lib/api/client';
import { getStripe } from '@modelcity/core/lib/payments/stripe';

/**
 * Builds an absolute URL pointing back at our app (used as Stripe's return
 * target). Reads the inbound request headers because there is no built-in
 * "base URL" helper in Next.js server actions.
 */
async function buildReturnUrl(lang, eventId) {
  const h = await headers();
  const host = h.get('x-forwarded-host') ?? h.get('host') ?? 'localhost:3000';
  const proto = h.get('x-forwarded-proto') ?? (host.startsWith('localhost') ? 'http' : 'https');
  const url = `${proto}://${host}/${lang}/events/${eventId}/checkout/return?session_id={CHECKOUT_SESSION_ID}`;
  console.log(`Constructed return URL for Stripe: ${url}`);
  return `${proto}://${host}/${lang}/events/${eventId}/checkout/return?session_id={CHECKOUT_SESSION_ID}`;
}

/**
 * Server Action: creates a Stripe **embedded** checkout session for the given
 * paid event and returns its `client_secret`, ready to be handed to the
 * `EmbeddedCheckoutProvider` on the client.
 *
 * Once the Stripe session is created the ticket purchase is immediately
 * registered on the leisure backend so that the webhook cannot arrive before
 * the record exists.
 *
 * Restricted to authenticated citizens. Free events do not go through this
 * flow — call {@link import('./events').claimFreeTicket} instead.
 *
 * @param {string | number} eventId
 * @param {string} lang
 */
export async function createEventCheckoutSession(eventId, lang) {
  const session = await auth0.getSession();
  if (!canBuyEventTickets(session)) return { error: 'forbidden' };

  const accessToken = session?.tokenSet?.accessToken;
  const evRes = await getEvent(eventId, accessToken);
  if ('error' in evRes) return { error: 'event_fetch_failed' };
  if ('notFound' in evRes) return { error: 'not_found' };

  const event = evRes.data;
  if (!event.requiresTicket || !event.paid) return { error: 'not_payable' };
  if (!event.stripePriceId) return { error: 'missing_price' };

  try {
    const stripe = getStripe();
    const returnUrl = await buildReturnUrl(lang, event.id);

    const checkoutSession = await stripe.checkout.sessions.create({
      ui_mode: 'embedded_page',
      mode: 'payment',
      line_items: [{ price: event.stripePriceId, quantity: 1 }],
      return_url: returnUrl,
      // Allows the return page to correlate the Stripe session with our
      // backend ticket creation without trusting client-side state.
      metadata: {
        eventId: String(event.id),
        citizenSub: session.user?.sub ?? '',
      },
    });

    const result = await purchaseEventTicket(eventId, accessToken, checkoutSession.id);
    if (!result.ok) {
      await stripe.checkout.sessions.expire(checkoutSession.id).catch((e) =>
        console.error('[stripeCheckout] expire session error:', e?.message ?? e),
      );
      return { error: result.body?.message ?? 'purchase_failed' };
    }

    return { ok: true, clientSecret: checkoutSession.client_secret };
  } catch (err) {
    console.error('[stripeCheckout] createSession error:', err?.message ?? err);
    return { error: 'stripe_error' };
  }
}

/**
 * Server Action: verifies that the Stripe checkout session completed
 * successfully.
 *
 * The ticket was already purchased at session-creation time, so this function
 * only confirms the payment status and the event identity.
 *
 * Called from the embedded-checkout return page after Stripe redirects the
 * browser to `?session_id=...`.
 *
 * @param {string} sessionId  Stripe checkout session ID.
 * @param {string | number} eventId
 */
export async function finalizeEventCheckout(sessionId, eventId) {
  const session = await auth0.getSession();
  if (!canBuyEventTickets(session)) return { error: 'forbidden' };
  if (!sessionId) return { error: 'missing_session' };

  try {
    const stripe = getStripe();
    const checkoutSession = await stripe.checkout.sessions.retrieve(sessionId);

    // `status` is "complete" once Stripe has finished the session — it does
    // NOT necessarily mean the payment succeeded. Combine with payment_status.
    if (checkoutSession.status !== 'complete') {
      return { error: 'not_complete', status: checkoutSession.status };
    }
    if (checkoutSession.payment_status !== 'paid') {
      return { error: 'not_paid', status: checkoutSession.payment_status };
    }
    if (String(checkoutSession.metadata?.eventId) !== String(eventId)) {
      return { error: 'event_mismatch' };
    }

    return { ok: true };
  } catch (err) {
    console.error('[stripeCheckout] finalize error:', err?.message ?? err);
    return { error: 'stripe_error' };
  }
}

