'use client';

import { useMemo, useState, useTransition } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import Modal from '@modelcity/core/components/molecules/Modal';
import {
  bookReservation,
  removeReservation,
} from '@modelcity/leisure/lib/actions/spaceReservations';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';

/** Booking window boundaries (minutes from midnight). */
const WINDOW_OPEN_MIN  = 9  * 60;   // 09:00 — earliest start
const WINDOW_CLOSE_MIN = 19 * 60;   // 19:00 — latest start allowed
const WINDOW_END_MIN   = 21 * 60;   // 21:00 — latest end allowed (19:00 + 2 h max)
/** Timeline display start — shown for context but not bookable before 09:00. */
const TIMELINE_OPEN_MIN = 7 * 60;   // 07:00 — earliest visible hour
/** Total visible minutes on the timeline (07:00 → 21:00). */
const TIMELINE_TOTAL_MIN = WINDOW_END_MIN - TIMELINE_OPEN_MIN; // 840
/** Maximum reservation duration in minutes. */
const MAX_DURATION_MIN = 2 * 60;

/** Hour labels shown on the timeline ruler (07:00 – 21:00). */
const HOUR_LABELS = [
  '07:00','08:00','09:00','10:00','11:00','12:00','13:00','14:00',
  '15:00','16:00','17:00','18:00','19:00','20:00','21:00',
];

/** Parses `HH:mm` (or `HH:mm:ss`) into total minutes from midnight. */
function timeToMinutes(time) {
  if (!time) return null;
  const [h, m] = time.split(':');
  const value = (parseInt(h, 10) || 0) * 60 + (parseInt(m, 10) || 0);
  return Number.isFinite(value) ? value : null;
}

/** Formats `HH:mm[:ss]` → `HH:mm`. */
function trimTime(time) {
  if (!time) return '';
  return time.slice(0, 5);
}

/** Returns tomorrow as `YYYY-MM-DD` (client-local). */
function tomorrowIso() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().slice(0, 10);
}

/**
 * Converts minutes-from-midnight to a CSS `top` percentage inside the
 * 07:00–21:00 timeline (0 % = 07:00, 100 % = 21:00).
 */
function toPercent(minutes) {
  return ((minutes - TIMELINE_OPEN_MIN) / TIMELINE_TOTAL_MIN) * 100;
}

/* ─────────────────────────────────────────────────────────────────────────
 * Timeline sub-component
 * ───────────────────────────────────────────────────────────────────── */

function ReservationsTimeline({ reservations: rawReservations, canManage, t, onDeleteClick, date, onDateChange }) {
  const reservations = Array.isArray(rawReservations) ? rawReservations : [];
  const sorted = useMemo(
    () => [...reservations].sort(
      (a, b) => (a.startTime ?? '').localeCompare(b.startTime ?? ''),
    ),
    [reservations],
  );

  return (
    <section className="flex flex-col gap-sm">
      <div className="flex items-center gap-md flex-wrap">
        <h3 className="text-h3 text-primary flex items-center gap-sm">
          <Icon name="schedule" fill size={20} className="text-secondary" />
          {t.timelineTitle}
        </h3>
        <input
          type="date"
          value={date}
          onChange={onDateChange}
          className={`${INPUT_CLS} max-w-56`}
        />
      </div>

      {/* Ruler + timeline body side by side */}
      <div className="flex gap-sm" style={{ minHeight: '700px' }}>
        {/* Hour ruler — one label per hour mark */}
        <div
          className="shrink-0 flex flex-col justify-between text-right"
          style={{ width: '3.5rem' }}
          aria-hidden="true"
        >
          {HOUR_LABELS.map((label) => (
            <span key={label} className="text-caption tabular-nums text-on-surface-variant leading-none">
              {label}
            </span>
          ))}
        </div>

        {/* Body */}
        <div className="relative flex-1 rounded-md border border-outline-variant bg-surface-container-lowest overflow-hidden">
          {/* Horizontal hour grid lines — one per hour (14 gaps for 14 hours) */}
          {HOUR_LABELS.map((label, i) => (
            <div
              key={label}
              aria-hidden="true"
              className={`absolute inset-x-0 border-t ${
                i === 0
                  ? 'border-outline-variant'
                  : i === 2 || i === 12  /* 09:00 first / 19:00 last valid start */
                    ? 'border-secondary/60 border-dashed'
                    : 'border-outline-variant/40'
              }`}
              style={{ top: `${(i / 14) * 100}%` }}
            />
          ))}

          {/* Shaded "no-booking" zone (07:00 – 09:00) — no reservations allowed */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 bg-outline-variant/10"
            style={{ top: 0, height: `${(2 / 14) * 100}%` }}
          />

          {/* Shaded "no-start" zone (19:00 – 21:00) — bookings can END here but not START */}
          <div
            aria-hidden="true"
            className="absolute inset-x-0 bg-outline-variant/10"
            style={{ top: `${(12 / 14) * 100}%`, bottom: 0 }}
          />

          {/* Empty state — full-height centred message */}
          {sorted.length === 0 && (
            <div className="absolute inset-0 flex flex-col items-center justify-center gap-sm text-center px-md">
              <Icon name="check_circle" size={36} className="text-secondary opacity-50" />
              <p className="text-body-md text-on-surface-variant">{t.empty}</p>
            </div>
          )}

          {/* Reservation blocks — positioned using 07:00–21:00 coordinate space */}
          {sorted.map((r) => {
            const startMin = timeToMinutes(r.startTime);
            const endMin   = timeToMinutes(r.endTime);
            if (startMin == null || endMin == null) return null;
            const topPct    = toPercent(startMin);
            const heightPct = ((endMin - startMin) / TIMELINE_TOTAL_MIN) * 100;

            return (
              <div
                key={r.id}
                className="absolute inset-x-2 rounded-md px-sm py-xs bg-primary/15 border border-primary/50 flex flex-col justify-between overflow-hidden group"
                style={{ top: `${topPct}%`, height: `${heightPct}%`, minHeight: '1.75rem' }}
              >
                <div className="flex items-start justify-between gap-xs">
                  <p className="text-caption font-semibold tabular-nums text-primary leading-tight truncate">
                    {trimTime(r.startTime)} – {trimTime(r.endTime)}
                  </p>
                  {canManage && (
                    <button
                      type="button"
                      onClick={() => onDeleteClick(r)}
                      className="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity rounded-md p-0.5 hover:bg-error/20 text-error"
                      aria-label={t.deleteReservation}
                    >
                      <Icon name="close" size={14} />
                    </button>
                  )}
                </div>
                {canManage && r.citizenSub && (
                  <p className="text-caption text-primary/70 truncate">
                    {r.citizenName || t.anonymousCitizen}
                  </p>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

/* ─────────────────────────────────────────────────────────────────────────
 * Booking form sub-component
 * ───────────────────────────────────────────────────────────────────── */

function BookingForm({ spaceId, resourceId, date, tForm, onBooked }) {
  const tomorrow = tomorrowIso();

  const [bookDate,  setBookDate]  = useState(date >= tomorrow ? date : tomorrow);
  const [startTime, setStartTime] = useState('09:00');
  const [endTime,   setEndTime]   = useState('11:00');
  const [submitting, setSubmitting] = useState(false);
  const [error,   setError]   = useState('');
  const [success, setSuccess] = useState(false);

  function clientValidate() {
    if (bookDate < tomorrow) return tForm.errors.tooSoon;
    const start = timeToMinutes(startTime);
    const end   = timeToMinutes(endTime);
    if (start == null || end == null) return tForm.errors.required;
    // Start must be within the booking window; end can reach 21:00 (start + max 2 h).
    if (start < WINDOW_OPEN_MIN || start >= WINDOW_CLOSE_MIN) return tForm.errors.outsideWindow;
    if (end > WINDOW_END_MIN)          return tForm.errors.outsideWindow;
    if (end <= start)                  return tForm.errors.invalidRange;
    if (end - start > MAX_DURATION_MIN) return tForm.errors.tooLong;
    return null;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSuccess(false);
    const clientError = clientValidate();
    if (clientError) { setError(clientError); return; }
    setError('');
    setSubmitting(true);

    const result = await bookReservation({
      spaceId,
      resourceId,
      reservationDate: bookDate,
      startTime,
      endTime,
    });
    setSubmitting(false);

    if (result?.error) {
      const map = {
        too_soon:       tForm.errors.tooSoon,
        conflict:       tForm.errors.conflict,
        outside_window: tForm.errors.outsideWindow,
        too_long:       tForm.errors.tooLong,
        invalid_range:  tForm.errors.invalidRange,
        forbidden:      tForm.errors.forbidden,
      };
      setError(map[result.error] ?? tForm.errors.serverError);
      return;
    }
    setSuccess(true);
    onBooked();
  }

  return (
    <form
      onSubmit={handleSubmit}
      noValidate
      className="flex flex-col gap-md lg:sticky lg:top-4"
    >
      <div>
        <h3 className="text-h3 text-primary flex items-center gap-sm">
          <Icon name="add_circle" fill size={20} className="text-secondary" />
          {tForm.title}
        </h3>
        <p className="text-caption text-on-surface-variant">{tForm.subtitle}</p>
      </div>

      <div className="flex flex-col gap-xs">
        <label htmlFor="book-date" className="text-label-md text-on-surface font-medium">
          {tForm.dateLabel}
        </label>
        <input
          id="book-date"
          type="date"
          min={tomorrow}
          value={bookDate}
          onChange={(e) => { setBookDate(e.target.value); setSuccess(false); setError(''); }}
          className={INPUT_CLS}
        />
      </div>

      <div className="flex flex-col gap-xs">
        <label htmlFor="reservation-start" className="text-label-md text-on-surface font-medium">
          {tForm.startLabel}
        </label>
        <input
          id="reservation-start"
          type="time"
          min="09:00"
          max="19:00"
          step="900"
          value={startTime}
          onChange={(e) => setStartTime(e.target.value)}
          className={INPUT_CLS}
        />
      </div>

      <div className="flex flex-col gap-xs">
        <label htmlFor="reservation-end" className="text-label-md text-on-surface font-medium">
          {tForm.endLabel}
        </label>
        <input
          id="reservation-end"
          type="time"
          min="09:00"
          max="21:00"
          step="900"
          value={endTime}
          onChange={(e) => setEndTime(e.target.value)}
          className={INPUT_CLS}
        />
      </div>

      {error && (
        <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
          <Icon name="error" size={18} className="shrink-0" />
          {error}
        </div>
      )}
      {success && !error && (
        <div className="flex items-center gap-sm px-md py-sm rounded-md bg-tertiary-container text-on-tertiary-container text-body-sm">
          <Icon name="check_circle" size={18} className="shrink-0" />
          {tForm.success}
        </div>
      )}

      <Button type="submit" variant="primary" disabled={submitting} className="w-full">
        {submitting ? tForm.submitting : tForm.submit}
      </Button>
    </form>
  );
}

/* ─────────────────────────────────────────────────────────────────────────
 * Main export
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Organism: ResourceReservationsPanel
 *
 * Layout (desktop): 2/3 timeline | 1/3 booking form for citizens — stacked on mobile.
 * Full-width timeline for admins (canBook=false).
 * The timeline renders the 09:00–19:00 window with absolute-positioned
 * blocks for each reservation. Operators/admins can hover any block to
 * reveal a delete button. Citizens get the booking form panel on the right.
 *
 * @param {{
 *   spaceId:      string | number,
 *   resourceId:   string | number,
 *   date:         string,               // ISO YYYY-MM-DD (timeline date)
 *   reservations: Array<object> | null, // null on fetch error
 *   canManage:    boolean,
 *   canBook:      boolean,
 *   t:            object,               // leisure.reservations
 *   tForm:        object,               // leisure.reservationForm
 *   spaceName:    string,
 *   resourceName: string,
 *   resourceType: string,
 *   resourceDescription?: string,
 *   tTypes:       object,               // leisure.resourceTypes
 * }} props
 */
export default function ResourceReservationsPanel({
  spaceId,
  resourceId,
  date,
  reservations,
  canManage,
  canBook,
  t,
  tForm,
  spaceName,
  resourceName,
  resourceType,
  resourceDescription,
  tTypes,
}) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  /* ── Timeline date picker ───────────────────────────────── */
  function onDateChange(e) {
    const next = e.target.value;
    if (!next) return;
    const qs = new URLSearchParams(searchParams.toString());
    qs.set('date', next);
    router.push(`${pathname}?${qs.toString()}`);
  }

  /* ── Delete reservation (staff) ────────────────────────── */
  const [deleting, setDeleting] = useState(null);
  const [deleteError, setDeleteError] = useState('');
  const [pending, startTransition] = useTransition();

  function confirmDelete() {
    if (!deleting) return;
    setDeleteError('');
    startTransition(async () => {
      const result = await removeReservation({ spaceId, resourceId, reservationId: deleting.id });
      if (result?.error) { setDeleteError(t.deleteError); return; }
      setDeleting(null);
      router.refresh();
    });
  }

  return (
    <section className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md pr-lg pb-lg flex flex-col gap-md">
      {/* Header */}
      <header className="flex flex-col gap-xs">
        <h1 className="text-h2 text-primary font-semibold flex items-center gap-sm">
          <Icon name="event_available" fill size={24} className="text-secondary w-6" />
          {spaceName}
        </h1>
        <h2 className="text-h3 text-primary font-medium flex items-center gap-sm">
          <Icon name="category" size={20} className="text-secondary w-6" />
          {resourceName}
        </h2>
        <p className="text-body-sm text-on-surface-variant flex items-center gap-xs pl-9">
          {tTypes[resourceType] ?? resourceType}
          {resourceDescription && (
            <>
              <span>·</span>
              <span className="text-body-md">{resourceDescription}</span>
            </>
          )}
        </p>
      </header>

      {/* Error banner */}
      {reservations === null ? (
        <div className="p-md rounded-md bg-error-container text-on-error-container text-body-sm flex items-center gap-sm">
          <Icon name="error_outline" size={18} />
          {t.loadError}
        </div>
      ) : (
        /* Full width for admins, 2/3 timeline | 1/3 form for citizens */
        <div className={`grid gap-lg items-baseline ${canBook ? 'grid-cols-1 lg:grid-cols-3' : 'grid-cols-1'}`}>
          <div className={canBook ? 'lg:col-span-2' : ''}>
            <ReservationsTimeline
              reservations={reservations}
              canManage={canManage}
              t={t}
              onDeleteClick={(r) => { setDeleteError(''); setDeleting(r); }}
              date={date}
              onDateChange={onDateChange}
            />
          </div>

          {canBook && (
            <div className="lg:col-span-1">
              <BookingForm
                key={date}
                spaceId={spaceId}
                resourceId={resourceId}
                date={date}
                tForm={tForm}
                onBooked={() => router.refresh()}
              />
            </div>
          )}
        </div>
      )}

      {/* Delete confirmation modal */}
      <Modal
        open={!!deleting}
        onClose={() => (pending ? null : setDeleting(null))}
        title={t.deleteConfirmTitle}
        closeLabel={tForm.cancel}
      >
        <div className="flex flex-col gap-md">
          <p className="text-body-md text-on-surface-variant">{t.deleteConfirmBody}</p>
          {deleteError && (
            <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
              <Icon name="error" size={18} className="shrink-0" />
              {deleteError}
            </div>
          )}
          <div className="flex items-center justify-end gap-md">
            <Button type="button" variant="outline-error" onClick={() => setDeleting(null)} disabled={pending}>
              {tForm.cancel}
            </Button>
            <Button type="button" variant="error" onClick={confirmDelete} disabled={pending}>
              {pending ? t.deleting : t.deleteReservation}
            </Button>
          </div>
        </div>
      </Modal>
    </section>
  );
}









