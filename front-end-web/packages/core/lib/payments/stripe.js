/**
 * Lazy server-side Stripe client.
 *
 * We do not eagerly construct the SDK at import time because Next.js builds
 * may run with `STRIPE_SECRET_KEY` absent (e.g. CI lint step). The factory
 * therefore validates the env var on first call and surfaces a clear error.
 *
 * ⚠️  Server-only. Never import from Client Components.
 */
import Stripe from 'stripe';

let cached = null;

/** Returns the singleton Stripe SDK instance, or throws when not configured. */
export function getStripe() {
  if (cached) return cached;
  const key = process.env.STRIPE_SECRET_KEY;
  if (!key) {
    throw new Error('STRIPE_SECRET_KEY is not configured');
  }
  cached = new Stripe(key);
  return cached;
}

