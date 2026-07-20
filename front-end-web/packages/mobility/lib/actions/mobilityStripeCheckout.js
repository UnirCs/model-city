'use server';

import { headers } from 'next/headers';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { isCitizen } from '@modelcity/core/lib/auth/roles';
import { getStripe } from '@modelcity/core/lib/payments/stripe';
import {
  createUserStreetReservation,
  renewUserStreetReservation,
} from '@modelcity/mobility/lib/api/client';

/** Parking product ID — comes from env so it can be swapped per environment. */
const PARKING_PRODUCT_ID =
  process.env.STRIPE_PARKING_PRODUCT_ID ?? 'prod_UcLFFfXlNLiZS2';

/**
 * Builds the absolute return URL for the parking checkout session.
 * Reads inbound request headers to reconstruct the base URL.
 *
 * @param {string} lang
 * @returns {Promise<string>}
 */
async function buildParkingReturnUrl(lang) {
  const h = await headers();
  const host = h.get('x-forwarded-host') ?? h.get('host') ?? 'localhost:3000';
  const proto =
    h.get('x-forwarded-proto') ??
    (host.startsWith('localhost') ? 'http' : 'https');
  return `${proto}://${host}/${lang}/mobility/parking-checkout/return?session_id={CHECKOUT_SESSION_ID}`;
}

/**
 * Returns the Stripe price ID for `amountCents` EUR on the parking product.
 *
 * Resolution order:
 *   1. Find an existing active one-time EUR price with the exact unit amount.
 *   2. If none found, create a new price.
 *
 * @param {import('stripe').Stripe} stripe
 * @param {number} amountCents
 * @returns {Promise<string>} price ID
 */
async function getOrCreateParkingPrice(stripe, amountCents) {
  const prices = await stripe.prices.list({
    product: PARKING_PRODUCT_ID,
    active: true,
    limit: 100,
  });

  const existing = prices.data.find(
    (p) =>
      p.unit_amount === amountCents &&
      p.currency === 'eur' &&
      p.type === 'one_time',
  );
  if (existing) return existing.id;

  const created = await stripe.prices.create({
    product: PARKING_PRODUCT_ID,
    unit_amount: amountCents,
    currency: 'eur',
  });
  return created.id;
}

/* ─────────────────────────────────────────────────────────────────────────
 * Public server actions
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Server Action: creates a Stripe **embedded** checkout session for a new
 * parking reservation and returns its `client_secret`.
 *
 * Pricing: 1 cent per minute (20 min = €0.20, 150 min = €1.50).
 *
 * Once the Stripe session is created the reservation is immediately registered
 * on the mobility backend so that the webhook cannot arrive before the record
 * exists.
 *
 * @param {{
 *   carId: number,
 *   latitude: number,
 *   longitude: number,
 *   durationMinutes: number,
 * }} payload
 * @param {string} lang
 * @returns {Promise<{ ok: true, clientSecret: string } | { error: string }>}
 */
export async function createParkingCheckoutSession(payload, lang) {
  const session = await auth0.getSession();
  if (!isCitizen(session)) return { error: 'forbidden' };

  const { carId, latitude, longitude, durationMinutes } = payload ?? {};
  if (!carId || latitude == null || longitude == null || !durationMinutes) {
    return { error: 'invalid_payload' };
  }
  if (durationMinutes < 20 || durationMinutes > 150) {
    return { error: 'invalid_duration' };
  }

  // 1 cent per minute
  const amountCents = durationMinutes;

  try {
    const stripe = getStripe();
    const priceId = await getOrCreateParkingPrice(stripe, amountCents);
    const returnUrl = await buildParkingReturnUrl(lang);

    const checkoutSession = await stripe.checkout.sessions.create({
      ui_mode: 'embedded_page',
      mode: 'payment',
      line_items: [{ price: priceId, quantity: 1 }],
      return_url: returnUrl,
      metadata: {
        type: 'new',
        carId: String(carId),
        latitude: String(latitude),
        longitude: String(longitude),
        durationMinutes: String(durationMinutes),
        citizenSub: session.user?.sub ?? '',
      },
    });

    const accessToken = session?.tokenSet?.accessToken;
    const sub = session?.user?.sub;
    const backendPayload = {
      carId,
      latitude,
      longitude,
      durationMinutes,
      checkoutSessionId: checkoutSession.id,
      price: amountCents,
    };
    console.log('[mobilityStripeCheckout] creating reservation with payload:', backendPayload);

    const result = await createUserStreetReservation(sub, backendPayload, accessToken);
    if (!result.ok) {
      await stripe.checkout.sessions.expire(checkoutSession.id).catch((e) =>
        console.error('[mobilityStripeCheckout] expire session error:', e?.message ?? e),
      );
      return { error: result.body?.message ?? 'create_failed' };
    }

    return { ok: true, clientSecret: checkoutSession.client_secret };
  } catch (err) {
    console.error(
      '[mobilityStripeCheckout] createSession error:',
      err?.message ?? err,
    );
    return { error: 'stripe_error' };
  }
}

/**
 * Server Action: creates a Stripe **embedded** checkout session for renewing
 * an existing parking reservation.
 *
 * Once the Stripe session is created the renewal is immediately registered on
 * the mobility backend so that the webhook cannot arrive before the record
 * exists.
 *
 * @param {string | number} reservationId
 * @param {{
 *   carId: number,
 *   latitude: number,
 *   longitude: number,
 *   durationMinutes: number,
 * }} payload
 * @param {string} lang
 * @returns {Promise<{ ok: true, clientSecret: string } | { error: string }>}
 */
export async function createParkingRenewalCheckoutSession(
  reservationId,
  payload,
  lang,
) {
  const session = await auth0.getSession();
  if (!isCitizen(session)) return { error: 'forbidden' };

  if (!reservationId) return { error: 'missing_reservation' };

  const { carId, latitude, longitude, durationMinutes } = payload ?? {};
  if (!carId || latitude == null || longitude == null || !durationMinutes) {
    return { error: 'invalid_payload' };
  }
  if (durationMinutes < 20 || durationMinutes > 150) {
    return { error: 'invalid_duration' };
  }

  const amountCents = durationMinutes;

  try {
    const stripe = getStripe();
    const priceId = await getOrCreateParkingPrice(stripe, amountCents);
    const returnUrl = await buildParkingReturnUrl(lang);

    const checkoutSession = await stripe.checkout.sessions.create({
      ui_mode: 'embedded_page',
      mode: 'payment',
      line_items: [{ price: priceId, quantity: 1 }],
      return_url: returnUrl,
      metadata: {
        type: 'renewal',
        reservationId: String(reservationId),
        carId: String(carId),
        latitude: String(latitude),
        longitude: String(longitude),
        durationMinutes: String(durationMinutes),
        citizenSub: session.user?.sub ?? '',
      },
    });

    const accessToken = session?.tokenSet?.accessToken;
    const sub = session?.user?.sub;
    const backendPayload = {
      carId,
      latitude,
      longitude,
      durationMinutes,
      checkoutSessionId: checkoutSession.id,
      price: amountCents,
    };

    const result = await renewUserStreetReservation(
      sub,
      reservationId,
      backendPayload,
      accessToken,
    );
    if (!result.ok) {
      await stripe.checkout.sessions.expire(checkoutSession.id).catch((e) =>
        console.error('[mobilityStripeCheckout] expire session error:', e?.message ?? e),
      );
      return { error: result.body?.message ?? 'renew_failed' };
    }

    return { ok: true, clientSecret: checkoutSession.client_secret };
  } catch (err) {
    console.error(
      '[mobilityStripeCheckout] createRenewalSession error:',
      err?.message ?? err,
    );
    return { error: 'stripe_error' };
  }
}

/**
 * Server Action: verifies that the Stripe checkout session completed
 * successfully.
 *
 * The reservation was already created/renewed at session-creation time, so
 * this function only confirms the payment status and returns the operation
 * type from the session metadata.
 *
 * Called from the `/{lang}/mobility/parking-checkout/return` page.
 *
 * @param {string} sessionId  Stripe checkout session ID.
 * @returns {Promise<
 *   { ok: true, type: 'new' | 'renewal' } |
 *   { error: string, status?: string }
 * >}
 */
export async function finalizeParkingCheckout(sessionId) {
  const session = await auth0.getSession();
  if (!isCitizen(session)) return { error: 'forbidden' };
  if (!sessionId) return { error: 'missing_session' };

  try {
    const stripe = getStripe();
    const checkoutSession = await stripe.checkout.sessions.retrieve(sessionId);

    if (checkoutSession.status !== 'complete') {
      return { error: 'not_complete', status: checkoutSession.status };
    }
    if (checkoutSession.payment_status !== 'paid') {
      return { error: 'not_paid', status: checkoutSession.payment_status };
    }

    const type = checkoutSession.metadata?.type ?? 'new';
    return { ok: true, type };
  } catch (err) {
    console.error(
      '[mobilityStripeCheckout] finalize error:',
      err?.message ?? err,
    );
    return { error: 'stripe_error' };
  }
}

