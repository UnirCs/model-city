'use client';

import { useEffect, useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import Badge from '@modelcity/core/components/atoms/Badge';
import {
  formatDurationMinutes,
  formatDateTime,
  formatTime,
  minutesUntil,
  isReservationActive,
} from '@modelcity/mobility/lib/utils/format';

/**
 * Builds a Google Maps URL that drops a pin at the given coordinates.
 */
function externalMapUrl(lat, lng) {
  return `https://www.google.com/maps?q=${lat},${lng}`;
}

/**
 * Molecule: StayCard
 *
 * Renders a single street reservation. Two visual variants:
 *   - active  → secondary accent, live remaining-time counter, renew CTA;
 *   - past    → muted, no counter, no renew CTA.
 *
 * The countdown is computed client-side from `expiresAt` and refreshed every
 * 30 s. The actual mutation is delegated to the parent via `onRenew`.
 *
 * @param {{
 *   reservation: {
 *     id: number,
 *     licensePlate: string,
 *     carNickname?: string,
 *     latitude: number,
 *     longitude: number,
 *     createdAt: string,
 *     expiresAt: string,
 *     active: boolean,
 *     renewedFromId?: number | null,
 *   },
 *   labels: object,
 *   lang: string,
 *   onRenew?: (reservation: object) => void,
 * }} props
 */
export default function StayCard({ reservation, labels, lang, onRenew }) {
  const [now, setNow] = useState(() => new Date());

  const active = isReservationActive(reservation, now);

  // Tick every 30 s while the card is visible so the countdown stays fresh.
  useEffect(() => {
    if (!active) return undefined;
    const id = setInterval(() => setNow(new Date()), 30_000);
    return () => clearInterval(id);
  }, [active]);

  const remaining = active ? minutesUntil(reservation.expiresAt, now) : 0;

  return (
    <article
      className={[
        'rounded-md shadow-sm hover:shadow-md border overflow-hidden flex flex-col transition-all duration-200 hover:-translate-y-1 h-full',
        active
          ? 'bg-surface-container-lowest border-secondary/40'
          : 'bg-surface-container-lowest border-outline-variant opacity-90',
      ].join(' ')}
    >
      <div className="px-md pt-md pb-0 flex-1 flex flex-col gap-xs">
        <header className="flex items-start justify-between gap-sm">
          <div className="flex items-start gap-sm min-w-0">
            <div
              className={[
                'w-10 h-10 rounded-md flex items-center justify-center shrink-0',
                active ? 'bg-secondary-container text-on-secondary-container' : 'bg-surface-container text-on-surface-variant',
              ].join(' ')}
            >
              <Icon name="local_parking" fill size={20} />
            </div>
            <div className="min-w-0">
              <h3 className="text-body-md text-primary line-clamp-2 font-semibold">
                {reservation.licensePlate}
              </h3>
              {reservation.carNickname && (
                <p className="text-caption text-on-surface-variant truncate">
                  {reservation.carNickname}
                </p>
              )}
            </div>
          </div>
          {active ? (
            <Badge status="active" label={labels.active} />
          ) : (
            <Badge status="inactive" label={labels.expired} />
          )}
        </header>

        <p className="text-caption text-on-surface-variant flex items-center gap-xs flex-wrap">
          <span className="inline-flex items-center gap-xs">
            <Icon name="schedule" size={13} className="text-secondary shrink-0" />
            <span>{formatDateTime(reservation.createdAt, lang)}</span>
          </span>
          <span aria-hidden="true" className="opacity-40">·</span>
          <span className="inline-flex items-center gap-xs">
            <Icon name="event" size={13} className="text-secondary shrink-0" />
            <span>{formatTime(reservation.expiresAt, lang)}</span>
          </span>
        </p>

        {active && (
          <div className="bg-secondary-container/40 rounded-md p-sm flex items-center justify-between">
            <span className="text-caption text-on-secondary-container uppercase tracking-wide">
              {labels.remaining}
            </span>
            <span className="text-h3 text-secondary tabular-nums">
              {formatDurationMinutes(remaining)}
            </span>
          </div>
        )}

        {reservation.renewedFromId && (
          <p className="text-caption text-on-surface-variant flex items-center gap-xs">
            <Icon name="autorenew" size={14} />
            {labels.renewedFrom.replace('{id}', String(reservation.renewedFromId))}
          </p>
        )}
      </div>

      <div className="mt-auto pt-sm flex">
        <Button
          variant="primary"
          size="md"
          onClick={() =>
            window.open(
              externalMapUrl(reservation.latitude, reservation.longitude),
              '_blank',
              'noreferrer noopener',
            )
          }
          className={[
            'flex-1',
            'rounded-tl-none rounded-tr-none',
            active && onRenew
              ? 'rounded-bl-md rounded-br-none'
              : 'rounded-bl-md rounded-br-md',
          ].join(' ')}
        >
          <Icon name="map" size={16} />
          {labels.viewOnMap}
        </Button>
        {active && onRenew && (
          <Button
            variant="secondary"
            size="md"
            onClick={() => onRenew(reservation)}
            className="flex-1 rounded-tl-none rounded-tr-none rounded-bl-none rounded-br-md"
          >
            <Icon name="autorenew" size={16} />
            {labels.renew}
          </Button>
        )}
      </div>
    </article>
  );
}


