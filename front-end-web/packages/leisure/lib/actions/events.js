'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canManageEvents, canDeleteEvents, canManageEventTickets, canBuyEventTickets } from '@modelcity/leisure/lib/auth/roles';
import {
  createEvent,
  updateEvent,
  deleteEvent,
  purchaseEventTicket,
  refundEventTicket,
} from '@modelcity/leisure/lib/api/client';
import { getStripe } from '@modelcity/core/lib/payments/stripe';

/**
 * Server Action: creates (POST) or fully updates (PUT) a leisure event.
 * Restricted to backoffice staff and platform administrators.
 *
 * @param {object} payload  Mirrors the backend CreateEventRequest contract.
 * @param {string | number | null | undefined} id
 * @returns {Promise<{ ok: true, id?: number | string } | { error: string }>}
 */
export async function saveEvent(payload, id = null) {
  const session = await auth0.getSession();
  if (!canManageEvents(session)) return { error: 'forbidden' };

  const accessToken = session?.tokenSet?.accessToken;
  const result = id
    ? await updateEvent(id, payload, accessToken)
    : await createEvent(payload, accessToken);

  if (!result.ok) {
    return { error: result.body?.message ?? 'save_failed' };
  }

  revalidatePath('/[lang]/events', 'page');
  const targetId = id ?? result.data?.id;
  if (targetId) revalidatePath(`/[lang]/events/${targetId}`, 'page');
  return { ok: true, id: targetId };
}

/**
 * Server Action: soft-deletes an event. Platform admin only. Existing
 * tickets are automatically refunded by the backend.
 *
 * @param {string | number} id
 */
export async function removeEvent(id) {
  const session = await auth0.getSession();
  if (!canDeleteEvents(session)) return { error: 'forbidden' };

  const accessToken = session?.tokenSet?.accessToken;
  const result = await deleteEvent(id, accessToken);
  if (!result.ok) return { error: result.body?.message ?? 'delete_failed' };

  revalidatePath('/[lang]/events', 'page');
  return { ok: true };
}

/**
 * Server Action: claims a ticket for a free event (no Stripe required).
 * For paid events the citizen must first complete the Stripe checkout;
 * the success page calls this same action afterwards.
 *
 * @param {string | number} eventId
 */
export async function claimFreeTicket(eventId) {
  const session = await auth0.getSession();
  if (!canBuyEventTickets(session)) return { error: 'forbidden' };

  const accessToken = session?.tokenSet?.accessToken;
  const result = await purchaseEventTicket(eventId, accessToken);
  if (!result.ok) return { error: result.body?.message ?? 'purchase_failed' };

  revalidatePath('/[lang]/events/my-tickets', 'page');
  revalidatePath(`/[lang]/events/${eventId}`, 'page');
  return { ok: true, ticket: result.data };
}

/**
 * Server Action: issues a manual refund for a sold ticket. Backoffice or
 * platform admin only.
 *
 * For paid tickets the refund is **always processed through Stripe first**.
 * Only if Stripe succeeds does the action notify the backend so it can
 * update the ticket's status. Free tickets (no `stripePaymentIntentId`) are
 * handed directly to the backend.
 *
 * @param {string | number} eventId
 * @param {string | number} ticketId
 * @param {string} reason
 * @param {string | null | undefined} stripePaymentIntentId  Stripe PI ID stored
 *   on the ticket. Omit (or pass null) for free tickets.
 */
export async function refundTicket(eventId, ticketId, reason, stripePaymentIntentId) {
  const session = await auth0.getSession();
  if (!canManageEventTickets(session)) return { error: 'forbidden' };

  const cleanReason = (reason ?? '').trim();
  if (!cleanReason) return { error: 'reason_required' };
  if (cleanReason.length > 512) return { error: 'reason_too_long' };

  // ── 1. Stripe refund (paid tickets only) ──────────────────────────────────
  if (stripePaymentIntentId) {
    try {
      const stripe = getStripe();
      await stripe.refunds.create({
        payment_intent: stripePaymentIntentId,
        reason: 'requested_by_customer',
        metadata: { reason: cleanReason.slice(0, 500) },
      });
    } catch (err) {
      console.error('[events] Stripe refund error:', err?.message ?? err);
      return { error: 'stripe_error' };
    }
  }

  // ── 2. Backend notification ───────────────────────────────────────────────
  const accessToken = session?.tokenSet?.accessToken;
  const result = await refundEventTicket(
    eventId,
    ticketId,
    { reason: cleanReason },
    accessToken,
  );
  if (!result.ok) return { error: result.body?.message ?? 'refund_failed' };

  revalidatePath(`/[lang]/events/${eventId}/tickets`, 'page');
  return { ok: true, refund: result.data };
}

