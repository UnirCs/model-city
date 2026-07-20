'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import StayCard from '@modelcity/mobility/components/molecules/StayCard';
import RenewStayModal from '@modelcity/mobility/components/molecules/RenewStayModal';
import { isReservationActive } from '@modelcity/mobility/lib/utils/format';

/**
 * Organism: StaysPanel
 *
 * Splits the user's reservations into "active" and "past" buckets and
 * renders each group as a responsive card grid. Owns the renewal modal so
 * any active card can trigger it without lifting state to the page.
 *
 * @param {{
 *   reservations: Array<object>,
 *   labels: object,
 *   renewLabels: object,
 *   lang: string,
 * }} props
 */
export default function StaysPanel({ reservations, labels, renewLabels, lang }) {
  const [renewTarget, setRenewTarget] = useState(null);

  const { active, past } = useMemo(() => {
    const now = new Date();
    const a = [];
    const p = [];
    for (const r of reservations) {
      if (isReservationActive(r, now)) a.push(r);
      else p.push(r);
    }
    // Most recent first inside each bucket.
    a.sort((x, y) => new Date(y.createdAt) - new Date(x.createdAt));
    p.sort((x, y) => new Date(y.createdAt) - new Date(x.createdAt));
    return { active: a, past: p };
  }, [reservations]);

  return (
    <div className="flex flex-col gap-xl">
      <section aria-labelledby="active-stays-title">
        <header className="flex items-center gap-sm mb-md">
          <Icon name="timer" fill size={22} className="text-secondary" />
          <h2 id="active-stays-title" className="text-h3 text-primary">
            {labels.activeTitle}
          </h2>
          <span className="text-caption text-on-surface-variant ml-xs">({active.length})</span>
        </header>

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
          {/* ── "Nueva estancia" add-card — always first ── */}
          <Link
            href={`/${lang}/mobility/reserve`}
            className="group rounded-md border-2 border-dashed border-secondary/40 bg-surface-container-lowest hover:border-secondary hover:bg-secondary/5 transition-all flex flex-col items-center justify-center gap-sm py-lg px-md text-center min-h-40"
          >
            <span className="w-12 h-12 rounded-full bg-secondary/10 group-hover:bg-secondary/20 flex items-center justify-center transition-colors">
              <Icon name="add" size={24} className="text-secondary" />
            </span>
            <span className="text-label-md text-secondary font-semibold">{labels.newStay}</span>
          </Link>

          {active.length === 0 ? (
            <div className="md:col-span-1 lg:col-span-2 flex items-center px-md text-body-md text-on-surface-variant">
              {labels.emptyActive}
            </div>
          ) : (
            active.map((r) => (
              <StayCard
                key={r.id}
                reservation={r}
                labels={labels}
                lang={lang}
                onRenew={setRenewTarget}
              />
            ))
          )}
        </div>
      </section>

      <section aria-labelledby="past-stays-title">
        <header className="flex items-center gap-sm mb-md">
          <Icon name="history" size={22} className="text-on-surface-variant" />
          <h2 id="past-stays-title" className="text-h3 text-primary">
            {labels.pastTitle}
          </h2>
          <span className="text-caption text-on-surface-variant ml-xs">({past.length})</span>
        </header>
        {past.length === 0 ? (
          <p className="text-body-md text-on-surface-variant">{labels.emptyPast}</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
            {past.map((r) => (
              <StayCard
                key={r.id}
                reservation={r}
                labels={labels}
                lang={lang}
              />
            ))}
          </div>
        )}
      </section>

      <RenewStayModal
        key={renewTarget?.id ?? 'closed'}
        open={renewTarget != null}
        reservation={renewTarget}
        labels={renewLabels}
        stayLabels={labels}
        lang={lang}
        onClose={() => setRenewTarget(null)}
      />
    </div>
  );
}

