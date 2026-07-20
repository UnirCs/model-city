'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import Badge from '@modelcity/core/components/atoms/Badge';
import {
  formatDateTime,
  formatDurationMinutes,
  isReservationActive,
  minutesUntil,
} from '@modelcity/mobility/lib/utils/format';

/**
 * Live status cell: derives the current reservation status from
 * `expiresAt` / `active` and refreshes every minute so the operator can see
 * tickets flip from "active" to "expired" in real time.
 */
function StatusCell({ reservation, labels }) {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 60_000);
    return () => clearInterval(id);
  }, []);

  const active = isReservationActive(reservation, now);
  const diffMin = minutesUntil(reservation.expiresAt, now);

  if (active) {
    return (
      <Badge
        status="active"
        label={labels.remainingShort.replace('{value}', formatDurationMinutes(diffMin))}
      />
    );
  }
  return (
    <Badge
      status="error"
      label={labels.expiredShort.replace('{value}', formatDurationMinutes(Math.abs(diffMin)))}
    />
  );
}

/**
 * Organism: TicketsTable
 *
 * Read-only staff card grid listing street reservations. Each card exposes a
 * "View on map" link and an "Issue sanction" button.
 *
 * @param {{
 *   reservations: Array<object>,
 *   labels: object,
 *   lang: string,
 * }} props
 */
export default function TicketsTable({ reservations, labels, lang }) {
  if (reservations.length === 0) {
    return (
      <div className="py-2xl text-center text-on-surface-variant flex flex-col items-center gap-md">
        <Icon name="inbox" size={48} className="opacity-40" />
        <p className="text-body-lg">{labels.empty}</p>
      </div>
    );
  }

  return (
    <ul className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
      {reservations.map((r) => (
        <li key={r.id}>
          <article className="bg-surface-container-lowest rounded-md shadow-sm hover:shadow-md border border-outline-variant overflow-hidden flex flex-col transition-all duration-200 hover:-translate-y-1 h-full">
            <div className="px-md pt-md pb-0 flex-1 flex flex-col gap-xs">
              <header className="flex items-start gap-sm">
                <div className="w-10 h-10 rounded-md bg-primary/10 text-primary flex items-center justify-center shrink-0">
                  <Icon name="local_parking" fill size={20} />
                </div>
                <div className="min-w-0">
                  <h3 className="text-body-md text-primary line-clamp-2 font-semibold">
                    {r.licensePlate}
                  </h3>
                  <p className="text-caption text-on-surface-variant">
                    {r.carNickname ?? '—'}
                  </p>
                </div>
              </header>

              <p className="text-caption text-on-surface-variant flex items-center gap-xs flex-wrap">
                <span className="inline-flex items-center gap-xs">
                  <Icon name="schedule" size={13} className="text-secondary shrink-0" />
                  <span>{formatDateTime(r.createdAt, lang)}</span>
                </span>
                <span aria-hidden="true" className="opacity-40">·</span>
                <span className="inline-flex items-center gap-xs">
                  <Icon name="event" size={13} className="text-secondary shrink-0" />
                  <span>{formatDateTime(r.expiresAt, lang)}</span>
                </span>
              </p>

              <div className="flex items-center gap-xs">
                <StatusCell reservation={r} labels={labels} />
              </div>
            </div>

            <div className="mt-auto pt-sm flex gap-xs">
              <Link
                href={`https://www.google.com/maps?q=${r.latitude},${r.longitude}`}
                target="_blank"
                rel="noreferrer noopener"
                className="flex-1 text-center py-sm text-label-md font-medium bg-primary text-on-primary hover:bg-secondary hover:text-on-secondary transition-colors cursor-pointer inline-flex items-center justify-center gap-xs"
              >
                <Icon name="map" size={16} />
                {labels.viewOnMap}
              </Link>
              <Link
                href={`/${lang}/mobility/sanctions/new?licensePlate=${encodeURIComponent(r.licensePlate)}&latitude=${r.latitude}&longitude=${r.longitude}`}
                className="flex-1 text-center py-sm text-label-md font-medium bg-error text-on-error hover:bg-error-container hover:text-error transition-colors cursor-pointer inline-flex items-center justify-center gap-xs"
              >
                <Icon name="gavel" size={16} />
                {labels.actionIssue}
              </Link>
            </div>
          </article>
        </li>
      ))}
    </ul>
  );
}

