'use client';

import { useState, useTransition } from 'react';
import { useRouter } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Badge from '@modelcity/core/components/atoms/Badge';
import Modal from '@modelcity/core/components/molecules/Modal';
import Button from '@modelcity/core/components/atoms/Button';
import { refundTicket } from '@modelcity/leisure/lib/actions/events';
import {
  formatEventDateTime,
  formatPrice,
  ticketStatusBadge,
  ticketStatusLabel,
} from '@modelcity/leisure/lib/utils/format';

const TEXTAREA_CLS =
  'w-full px-md py-sm rounded-md border border-outline-variant text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary resize-none';

/**
 * Single refund modal shared across every row in the table.
 * Mounted once — avoids N Modal instances with N sets of effects.
 * Remounts (via key) when a different ticket is selected so its local
 * state (reason, error) is always clean.
 */
function RefundModal({ eventId, ticket, onClose, onSuccess, labels }) {
  const [reason, setReason]   = useState('');
  const [error, setError]     = useState('');
  const [pending, startTransition] = useTransition();

  function handleClose() {
    setReason('');
    setError('');
    onClose();
  }

  function handleSubmit() {
    setError('');
    const trimmed = reason.trim();
    if (!trimmed)               { setError(labels.errorRequired); return; }
    if (trimmed.length > 512)   { setError(labels.errorTooLong);  return; }

    startTransition(async () => {
      const result = await refundTicket(
        eventId,
        ticket.id,
        trimmed,
        ticket.stripePaymentIntentId ?? null,
      );
      if (result?.error === 'reason_required') { setError(labels.errorRequired); return; }
      if (result?.error === 'reason_too_long') { setError(labels.errorTooLong);  return; }
      if (result?.error === 'stripe_error')    { setError(labels.errorStripe);   return; }
      if (result?.error)                        { setError(labels.errorGeneric);  return; }
      handleClose();
      onSuccess();
    });
  }

  return (
    <Modal
      open={!!ticket}
      onClose={handleClose}
      title={labels.dialogTitle}
      closeLabel={labels.close}
    >
      <div className="flex flex-col gap-md">
        <label className="flex flex-col gap-xs">
          <span className="text-label-md text-on-surface font-medium">{labels.reasonLabel}</span>
          <textarea
            rows={4}
            value={reason}
            maxLength={512}
            onChange={(e) => { setReason(e.target.value); setError(''); }}
            placeholder={labels.reasonPlaceholder}
            className={TEXTAREA_CLS}
          />
          <span className="text-caption text-on-surface-variant text-right">
            {reason.length} / 512
          </span>
        </label>

        {error && (
          <p className="flex items-center gap-xs text-body-sm text-error">
            <Icon name="error" size={16} />
            {error}
          </p>
        )}

        <div className="flex items-center justify-end gap-md">
          <Button type="button" variant="outline-error" onClick={handleClose} disabled={pending}>
            {labels.cancel}
          </Button>
          <Button type="button" variant="error" onClick={handleSubmit} disabled={pending}>
            {pending ? labels.processing : labels.confirm}
          </Button>
        </div>
      </div>
    </Modal>
  );
}

/**
 * Organism: EventTicketsTable
 *
 * Client component that renders the paginated table of sold tickets for a
 * given event. A single shared refund modal is mounted once at the table
 * level and opened with the selected ticket's data — avoiding the N-modals
 * / N-effects pattern that caused cascading re-renders.
 *
 * @param {{
 *   eventId: number | string,
 *   tickets: Array<object>,
 *   t: object,
 *   statusLabels: Record<string, string>,
 *   refundLabels: object,
 *   lang: string,
 * }} props
 */
export default function EventTicketsTable({
  eventId,
  tickets,
  t,
  statusLabels,
  refundLabels,
  lang,
}) {
  const router = useRouter();
  const [selectedTicket, setSelectedTicket] = useState(null);

  if (tickets.length === 0) {
    return (
      <div className="py-2xl text-center text-on-surface-variant flex flex-col items-center gap-md">
        <Icon name="inbox" size={48} className="opacity-40" />
        <p className="text-body-lg">{t.empty}</p>
      </div>
    );
  }

  return (
    <>
      <div className="bg-surface-container-lowest rounded-md shadow-sm border border-outline-variant overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container-low border-b border-outline-variant text-label-md text-on-surface-variant">
                <th className="p-md">{t.colId}</th>
                <th className="p-md">{t.colCitizen}</th>
                <th className="p-md">{t.colPurchasedAt}</th>
                <th className="p-md">{t.colPrice}</th>
                <th className="p-md">{t.colStatus}</th>
                <th className="p-md text-right">{t.colActions}</th>
              </tr>
            </thead>
            <tbody className="text-body-md text-on-surface">
              {tickets.map((ticket) => {
                const isRefunded = ticket.status === 'REFUNDED';
                return (
                  <tr
                    key={ticket.id}
                    className="border-b last:border-b-0 border-outline-variant hover:bg-surface-container-lowest transition-colors"
                  >
                    <td className="p-md font-semibold tracking-wide text-primary tabular-nums">
                      #{ticket.id}
                    </td>
                    <td className="p-md">
                      <div className="flex flex-col">
                        <span className="text-body-md text-on-surface">
                          {ticket.citizenName || t.anonymousCitizen}
                        </span>
                        <span className="text-caption text-on-surface-variant truncate max-w-72">
                          {ticket.citizenSub}
                        </span>
                      </div>
                    </td>
                    <td className="p-md tabular-nums text-caption text-on-surface-variant">
                      {formatEventDateTime(ticket.purchasedAt, lang)}
                    </td>
                    <td className="p-md tabular-nums">
                      {formatPrice(ticket.pricePaid, ticket.currency, lang, t.free)}
                    </td>
                    <td className="p-md">
                      <Badge
                        status={ticketStatusBadge(ticket.status)}
                        label={ticketStatusLabel(ticket.status, statusLabels)}
                      />
                    </td>
                    <td className="p-md text-right">
                      <Button
                        type="button"
                        variant="outline-error"
                        size="sm"
                        disabled={isRefunded}
                        onClick={() => setSelectedTicket(ticket)}
                      >
                        <Icon name="undo" size={16} />
                        {refundLabels.button}
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Single modal shared across all rows — mounted once, avoids N effect instances */}
      <RefundModal
        key={selectedTicket?.id ?? 'none'}
        eventId={eventId}
        ticket={selectedTicket}
        onClose={() => setSelectedTicket(null)}
        onSuccess={() => { setSelectedTicket(null); router.refresh(); }}
        labels={refundLabels}
      />
    </>
  );
}
