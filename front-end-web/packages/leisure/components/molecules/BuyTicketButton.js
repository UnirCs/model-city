'use client';

import { useState, useTransition } from 'react';
import { useRouter } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import Modal from '@modelcity/core/components/molecules/Modal';
import EventCheckoutEmbed from '@modelcity/leisure/components/molecules/EventCheckoutEmbed';
import { claimFreeTicket } from '@modelcity/leisure/lib/actions/events';
import { useAnnounce } from '@modelcity/core/lib/a11y/AnnouncerProvider';

/**
 * Molecule: BuyTicketButton
 *
 * Sole entry point for a citizen to acquire an event ticket.
 * Two flows:
 *   • Free events  → calls {@link claimFreeTicket} directly and refreshes.
 *   • Paid events  → opens an in-page modal with the Stripe Embedded Checkout.
 *
 * @param {{
 *   eventId: number | string,
 *   paid: boolean,
 *   disabled?: boolean,
 *   lang: string,
 *   labels: {
 *     buyPaid: string,
 *     claimFree: string,
 *     processing: string,
 *     successFree: string,
 *     errorGeneric: string,
 *     errorForbidden: string,
 *     modalTitle: string,
 *     close: string,
 *     misconfigured: string,
 *   },
 * }} props
 */
export default function BuyTicketButton({ eventId, paid, disabled, lang, labels }) {
  const router = useRouter();
  const announce = useAnnounce();
  const [pending, startTransition] = useTransition();
  const [message, setMessage] = useState(null);
  const [error, setError] = useState('');
  const [checkoutOpen, setCheckoutOpen] = useState(false);

  function handleClick() {
    setError('');
    setMessage(null);

    if (paid) {
      setCheckoutOpen(true);
      return;
    }

    startTransition(async () => {
      const result = await claimFreeTicket(eventId);
      if (result?.error === 'forbidden') {
        setError(labels.errorForbidden);
        announce(labels.errorForbidden, 'assertive');
        return;
      }
      if (result?.error) {
        setError(labels.errorGeneric);
        announce(labels.errorGeneric, 'assertive');
        return;
      }
      setMessage(labels.successFree);
      announce(labels.successFree, 'polite');
      router.refresh();
    });
  }

  return (
    <div className="flex flex-col gap-sm items-center">
      <Button
        type="button"
        variant="primary"
        disabled={disabled || pending}
        onClick={handleClick}
      >
        <Icon name={paid ? 'shopping_cart_checkout' : 'confirmation_number'} size={20} />
        {pending ? labels.processing : paid ? labels.buyPaid : labels.claimFree}
      </Button>

      {message && (
        <p className="flex items-center gap-xs text-body-sm text-tertiary">
          <Icon name="check_circle" size={16} />
          {message}
        </p>
      )}
      {error && (
        <p className="flex items-center gap-xs text-body-sm text-error">
          <Icon name="error" size={16} />
          {error}
        </p>
      )}

      {/* Stripe Embedded Checkout modal — only mounted for paid events */}
      {paid && (
        <Modal
          open={checkoutOpen}
          onClose={() => setCheckoutOpen(false)}
          title={labels.modalTitle}
          closeLabel={labels.close}
          panelClassName="w-[min(36rem,calc(100vw-2rem))]"
          bodyClassName="flex-1"
        >
          <EventCheckoutEmbed eventId={eventId} lang={lang} labels={labels} />
        </Modal>
      )}
    </div>
  );
}
