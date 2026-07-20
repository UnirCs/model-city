'use client';

import { useCallback, useMemo, useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import FormField from '@modelcity/core/components/atoms/FormField';
import DurationSlider from '@modelcity/core/components/atoms/DurationSlider';
import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import LocationPickerMapClient from '@modelcity/core/components/molecules/LocationPickerMapClient';
import Modal from '@modelcity/core/components/molecules/Modal';
import StripeCheckoutEmbed from '@modelcity/core/components/molecules/StripeCheckoutEmbed';
import { createParkingCheckoutSession } from '@modelcity/mobility/lib/actions/mobilityStripeCheckout';
import {
  formatDurationMinutes,
  formatParkingPrice,
  PARKING_MIN_MINUTES,
  PARKING_MAX_MINUTES,
  PARKING_STEP_MINUTES,
} from '@modelcity/mobility/lib/utils/format';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border border-outline-variant text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary appearance-none';

/**
 * Organism: ReservationForm
 *
 * Two-column "register stay" form: map picker on the left, car selector +
 * duration slider on the right. On submit (after validation) it opens a Stripe
 * Embedded Checkout modal — payment triggers the backend reservation creation
 * via the `/{lang}/mobility/parking-checkout/return` return page.
 *
 * Pricing: 1 cent per minute. Min 20 min (€0.20), max 2h 30m (€1.50).
 *
 * @param {{
 *   cars: Array<{ id: number, licensePlate: string, nickname?: string }>,
 *   labels: object,
 *   lang: string,
 * }} props
 */
export default function ReservationForm({ cars, labels, lang }) {
  const [carId, setCarId]         = useState(cars[0]?.id ?? '');
  const [duration, setDuration]   = useState(60);
  const [latitude, setLatitude]   = useState(null);
  const [longitude, setLongitude] = useState(null);

  const [errors, setErrors]           = useState({});
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [checkoutPayload, setCheckoutPayload] = useState(null);

  const effectiveCarId = useMemo(() => {
    if (cars.find((c) => String(c.id) === String(carId))) return carId;
    return cars[0]?.id ?? '';
  }, [cars, carId]);

  const selectedCar = useMemo(
    () => cars.find((c) => String(c.id) === String(effectiveCarId)) ?? null,
    [cars, effectiveCarId],
  );

  // fetchClientSecret is recreated when checkoutPayload changes (on each submit).
  // EmbeddedCheckoutProvider calls it exactly once per mount, so remounting the
  // embed (via checkoutOpen toggle) ensures a fresh call for each payment attempt.
  const fetchClientSecret = useCallback(() => {
    if (!checkoutPayload) return Promise.reject(new Error('no_payload'));
    return createParkingCheckoutSession(checkoutPayload, lang).then((r) => {
      if (r?.clientSecret) return r.clientSecret;
      throw new Error(r?.error ?? 'unknown_error');
    });
  }, [checkoutPayload, lang]);

  function handleMapChange({ latitude: lat, longitude: lng }) {
    setLatitude(lat);
    setLongitude(lng);
    setErrors((p) => ({ ...p, location: '' }));
  }

  function validate() {
    const e = {};
    if (!effectiveCarId)                                   e.car      = labels.errors.carRequired;
    if (latitude == null || longitude == null)             e.location = labels.errors.locationRequired;
    if (duration < PARKING_MIN_MINUTES || duration > PARKING_MAX_MINUTES)
                                                           e.duration = labels.errors.durationRange;
    return e;
  }

  function handleSubmit(event) {
    event.preventDefault();
    const v = validate();
    if (Object.keys(v).length > 0) {
      setErrors(v);
      return;
    }
    setErrors({});

    setCheckoutPayload({
      carId: Number(effectiveCarId),
      latitude,
      longitude,
      durationMinutes: duration,
    });
    setCheckoutOpen(true);
  }

  return (
    <>
      <form
        onSubmit={handleSubmit}
        noValidate
        className="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start"
      >
        {/* ── LEFT: map picker ────────────────────────────────── */}
        <section className="lg:col-span-7 bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md flex flex-col gap-sm h-full">
          <header className="flex items-center gap-sm">
            <div className="w-10 h-10 rounded-md bg-primary/10 text-primary flex items-center justify-center">
              <Icon name="map" fill size={22} />
            </div>
            <div>
              <h2 className="text-h3 text-primary">{labels.mapTitle}</h2>
              <p className="text-caption text-on-surface-variant">{labels.mapHint}</p>
            </div>
          </header>

          <LocationPickerMapClient
            latitude={latitude}
            longitude={longitude}
            onChange={handleMapChange}
            mapTitle={labels.mapTitle}
          />

          {errors.location && (
            <p className="text-caption text-error flex items-center gap-xs" role="alert">
              <Icon name="error" size={14} />
              {errors.location}
            </p>
          )}
        </section>

        {/* ── RIGHT: details ──────────────────────────────────── */}
        <aside className="lg:col-span-5 bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md flex flex-col gap-md lg:sticky lg:top-24 h-full">
          <header className="flex items-center gap-sm border-b border-outline-variant pb-sm">
            <Icon name="edit_document" size={22} className="text-primary" />
            <h2 className="text-h3 text-primary">{labels.panelTitle}</h2>
          </header>

          <FormField id="car" label={labels.carLabel} required error={errors.car}>
            {cars.length === 0 ? (
              <div className="rounded-md border border-dashed border-outline-variant p-md text-center text-caption text-on-surface-variant flex flex-col gap-sm">
                <span>{labels.carEmpty}</span>
                <LocalizedLink
                  href="/mobility/cars"
                  className="text-secondary hover:underline inline-flex items-center justify-center gap-xs"
                >
                  <Icon name="directions_car" size={16} />
                  {labels.carManage}
                </LocalizedLink>
              </div>
            ) : (
              <div className="relative">
                <select
                  id="car"
                  value={effectiveCarId}
                  onChange={(e) => {
                    setCarId(e.target.value);
                    setErrors((p) => ({ ...p, car: '' }));
                  }}
                  className={`pr-xl ${INPUT_CLS}`}
                >
                  <option value="">{labels.carPlaceholder}</option>
                  {cars.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.licensePlate}
                      {c.nickname ? ` — ${c.nickname}` : ''}
                    </option>
                  ))}
                </select>
                <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                  <Icon name="expand_more" size={20} />
                </span>
              </div>
            )}
          </FormField>

          <div className="flex flex-col gap-xs">
            <DurationSlider
              value={duration}
              onChange={setDuration}
              min={PARKING_MIN_MINUTES}
              max={PARKING_MAX_MINUTES}
              step={PARKING_STEP_MINUTES}
              label={labels.durationLabel}
              hint={labels.durationHint}
              quickPicksLabel={labels.quickPicks}
              formatDuration={formatDurationMinutes}
            />
            {errors.duration && (
              <p className="text-caption text-error flex items-center gap-xs" role="alert">
                <Icon name="error" size={14} />
                {errors.duration}
              </p>
            )}
          </div>

          <div className="rounded-md bg-surface-container p-md border border-outline-variant text-body-md flex flex-col gap-xs">
            <p className="text-caption text-on-surface-variant uppercase tracking-wide">
              {labels.summaryTitle}
            </p>
            <div className="flex justify-between gap-sm">
              <span className="text-on-surface-variant">{labels.summaryDuration}</span>
              <span className="text-primary font-semibold tabular-nums">
                {formatDurationMinutes(duration)}
              </span>
            </div>
            <div className="flex justify-between gap-sm">
              <span className="text-on-surface-variant">{labels.summaryCar}</span>
              <span className="text-primary font-semibold tracking-wide">
                {selectedCar?.licensePlate ?? '—'}
              </span>
            </div>
            <div className="flex justify-between gap-sm border-t border-outline-variant pt-xs mt-xs">
              <span className="text-on-surface-variant">{labels.summaryPrice}</span>
              <span className="text-secondary font-bold tabular-nums">
                {formatParkingPrice(duration)}
              </span>
            </div>
          </div>

          <Button
            type="submit"
            variant="primary"
            disabled={cars.length === 0}
            className="self-center"
          >
            <Icon name="payments" size={20} />
            {labels.submit}
          </Button>
        </aside>
      </form>

      {/* Stripe Embedded Checkout modal */}
      <Modal
        open={checkoutOpen}
        onClose={() => setCheckoutOpen(false)}
        title={labels.stripeModalTitle}
        closeLabel={labels.stripeModalClose}
        panelClassName="w-[min(36rem,calc(100vw-2rem))]"
        bodyClassName="flex-1"
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
