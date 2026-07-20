/**
 * Pure helpers shared by the mobility client components.
 * Duration formatting, time-remaining calculation, ticket status checks and
 * pricing utilities.
 */

/**
 * Parking pricing constants.
 * 1 cent per minute — minimum 20 min (€0.20), maximum 150 min (€1.50).
 */
export const PARKING_MIN_MINUTES = 20;
export const PARKING_MAX_MINUTES = 150;
export const PARKING_STEP_MINUTES = 5;
export const PARKING_CENTS_PER_MINUTE = 1;

/**
 * Formats a parking duration expressed in minutes as a EUR price string.
 * 60 min → "€0,60", 150 min → "€1,50".
 *
 * @param {number} minutes
 * @param {string} [locale]  BCP-47 locale tag (default "es-ES").
 * @returns {string}
 */
export function formatParkingPrice(minutes, locale = 'es-ES') {
  const cents = Math.round(minutes) * PARKING_CENTS_PER_MINUTE;
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
  }).format(cents / 100);
}

/**
 * Formats a duration expressed in minutes as a compact human-readable string.
 * 90 → "1h 30m", 45 → "45m", 120 → "2h", 5 → "5m".
 *
 * @param {number} minutes
 * @returns {string}
 */
export function formatDurationMinutes(minutes) {
  const safe = Math.max(0, Math.round(Number(minutes) || 0));
  const hours = Math.floor(safe / 60);
  const mins  = safe % 60;
  if (hours === 0) return `${mins}m`;
  if (mins  === 0) return `${hours}h`;
  return `${hours}h ${mins}m`;
}

/**
 * Computes the absolute number of minutes remaining between `now` and the
 * given ISO date string. Returns the signed value:
 *   - positive when the date is in the future,
 *   - negative when it is in the past.
 *
 * @param {string | Date} isoDate
 * @param {Date} [now]
 * @returns {number}
 */
export function minutesUntil(isoDate, now = new Date()) {
  if (!isoDate) return 0;
  const target = isoDate instanceof Date ? isoDate : new Date(isoDate);
  const diffMs = target.getTime() - now.getTime();
  return Math.round(diffMs / 60000);
}

/**
 * Returns true when the given reservation is currently active (not yet expired
 * and the backend `active` flag is true).
 *
 * @param {{ active?: boolean, expiresAt?: string }} reservation
 * @param {Date} [now]
 */
export function isReservationActive(reservation, now = new Date()) {
  if (!reservation) return false;
  if (reservation.active === false) return false;
  if (!reservation.expiresAt) return Boolean(reservation.active);
  return new Date(reservation.expiresAt).getTime() > now.getTime();
}

/**
 * Formats a Date as a short locale string (date + time). Used in tables and
 * cards. Falls back to the empty string for invalid input.
 *
 * @param {string | Date | null | undefined} isoDate
 * @param {string} locale
 */
export function formatDateTime(isoDate, locale = 'es') {
  if (!isoDate) return '';
  const d = isoDate instanceof Date ? isoDate : new Date(isoDate);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleString(locale, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Formats a Date as HH:mm only. Useful for compact table cells.
 *
 * @param {string | Date | null | undefined} isoDate
 * @param {string} locale
 */
export function formatTime(isoDate, locale = 'es') {
  if (!isoDate) return '';
  const d = isoDate instanceof Date ? isoDate : new Date(isoDate);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
}

/**
 * Converts a YYYY-MM-DD picker value into an ISO OffsetDateTime at the
 * start (00:00:00) or end (23:59:59) of the day in the user's local zone.
 *
 * @param {string | undefined | null} value
 * @param {'start' | 'end'} [bound]
 * @returns {string | null}
 */
export function dateToIso(value, bound = 'start') {
  if (!value) return null;
  const suffix = bound === 'end' ? 'T23:59:59' : 'T00:00:00';
  const d = new Date(`${value}${suffix}`);
  if (Number.isNaN(d.getTime())) return null;
  return d.toISOString();
}


