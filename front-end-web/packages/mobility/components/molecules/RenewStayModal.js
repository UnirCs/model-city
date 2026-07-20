'use client';

import { useCallback, useState } from 'react';
import Modal from '@modelcity/core/components/molecules/Modal';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import DurationSlider from '@modelcity/core/components/atoms/DurationSlider';
import StripeCheckoutEmbed from '@modelcity/core/components/molecules/StripeCheckoutEmbed';
import { createParkingRenewalCheckoutSession } from '@modelcity/mobility/lib/actions/mobilityStripeCheckout';
import {
  formatDurationMinutes,
  formatParkingPrice,
  PARKING_MIN_MINUTES,
  PARKING_MAX_MINUTES,
  PARKING_STEP_MINUTES,
} from '@modelcity/mobility/lib/utils/format';

/**
 * Molecule: RenewStayModal
 *
 * Lightweight modal hosting a duration slider for renewing an active street
 * reservation. On submit (after validation) it opens a Stripe Embedded
 * Checkout inside the same modal — payment triggers the backend renewal via
 * the `/{lang}/mobility/parking-checkout/return` return page.
 *
 * The parent is expected to remount this component with a fresh `key` for
 * every new reservation target so internal state resets automatically.
 *
 * @param {{
 *   open: boolean,
 *   reservation: {
 *     id: number,
 *     carId: number,
 *     licensePlate: string,
 *     latitude: number,
 *     longitude: number,
 *   } | null,
 *   labels: object,
 *   stayLabels: object,
 *   lang: string,
 *   onClose: () => void,
 * }} props
 */
export default function RenewStayModal({
  open,
  reservation,
  labels,
  stayLabels,
  lang,
  onClose,
}) {
  const [duration, setDuration]           = useState(60);
  const [error, setError]                 = useState(null);
  const [checkoutOpen, setCheckoutOpen]   = useState(false);
  const [checkoutPayload, setCheckoutPayload] = useState(null);

  const fetchClientSecret = useCallback(() => {
    if (!checkoutPayload || !reservation) return Promise.reject(new Error('no_payload'));
    return createParkingRenewalCheckoutSession(
      reservation.id,
      checkoutPayload,
      lang,
    ).then((r) => {
      if (r?.clientSecret) return r.clientSecret;
      throw new Error(r?.error ?? 'unknown_error');
    });
  }, [checkoutPayload, reservation, lang]);

  function handleSubmit(event) {
    event.preventDefault();
    if (!reservation) return;
    if (duration < PARKING_MIN_MINUTES || duration > PARKING_MAX_MINUTES) {
      setError(labels.errors.durationRange);
      return;
    }
    setError(null);

    setCheckoutPayload({
      carId: reservation.carId,
      latitude: reservation.latitude,
      longitude: reservation.longitude,
      durationMinutes: duration,
    });
    setCheckoutOpen(true);
  }

  if (!reservation) return null;

  return (
    <>
      <Modal
        open={open && !checkoutOpen}
        onClose={onClose}
        title={stayLabels.renewModalTitle}
        closeLabel={labels.cancel}
      >
        <form onSubmit={handleSubmit} className="flex flex-col gap-md">
          <div className="rounded-md bg-surface-container border border-outline-variant p-md flex items-center gap-sm">
            <Icon name="local_parking" fill size={20} className="text-secondary" />
            <div className="flex-1 min-w-0">
              <p className="text-caption text-on-surface-variant uppercase tracking-wide">
                {labels.currentTicket}
              </p>
              <p className="text-body-md text-primary font-semibold tracking-wide truncate">
                {reservation.licensePlate}
              </p>
            </div>
          </div>

          <div>
            <p className="text-label-md text-on-surface mb-sm">{labels.newDuration}</p>
            <DurationSlider
              value={duration}
              onChange={setDuration}
              min={PARKING_MIN_MINUTES}
              max={PARKING_MAX_MINUTES}
              step={PARKING_STEP_MINUTES}
              formatDuration={formatDurationMinutes}
            />
          </div>

          {/* Price preview */}
          <div className="flex items-center justify-between rounded-md bg-surface-container border border-outline-variant px-md py-sm">
            <span className="text-body-sm text-on-surface-variant">{labels.priceLabel}</span>
            <span className="text-h3 text-secondary font-bold tabular-nums">
              {formatParkingPrice(duration)}
            </span>
          </div>

          {error && (
            <div className="p-sm bg-error-container text-on-error-container rounded-md flex items-center gap-xs text-caption">
              <Icon name="error_outline" size={16} />
              <span>{error}</span>
            </div>
          )}

          <div className="flex justify-end gap-sm pt-sm border-t border-outline-variant">
            <Button type="button" variant="outline-error" onClick={onClose}>
              {labels.cancel}
            </Button>
            <Button type="submit" variant="primary">
              <Icon name="payments" size={16} />
              {labels.submit}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Stripe Embedded Checkout modal — opens after duration confirmation */}
      <Modal
        open={checkoutOpen}
        onClose={() => setCheckoutOpen(false)}
        title={labels.stripeModalTitle}
        closeLabel={labels.cancel}
        panelClassName="w-[min(42rem,calc(100vw-2rem))]"
      >
        {checkoutOpen && checkoutPayload && (
          <StripeCheckoutEmbed
            fetchClientSecret={fetchClientSecret}
            labels={{ misconfigured: labels.stripeMisconfigured }}
          />
        )}
      </Modal>
    </>
  );
}
