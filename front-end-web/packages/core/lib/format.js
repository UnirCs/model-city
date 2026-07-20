/**
 * Cross-cutting formatting helpers for the core service.
 */

/**
 * Converts a `yyyy-mm-dd` value (from a native `<input type="date">`) into an
 * ISO-8601 instant, snapping to the start or end of that day. Returns `null`
 * for empty or invalid input so callers can omit the bound entirely.
 *
 * @param {string|undefined|null} value  – `yyyy-mm-dd`
 * @param {'start'|'end'} [bound='start']
 * @returns {string|null}
 */
export function dateToIso(value, bound = 'start') {
  if (!value) return null;
  const suffix = bound === 'end' ? 'T23:59:59' : 'T00:00:00';
  const d = new Date(`${value}${suffix}`);
  if (Number.isNaN(d.getTime())) return null;
  return d.toISOString();
}

/**
 * Formats an ISO-8601 instant as a localised date-time string, or returns a
 * dash for missing/invalid input.
 *
 * @param {string|null|undefined} iso
 * @param {string} lang
 * @returns {string}
 */
export function formatDateTime(iso, lang) {
  if (!iso) return '—';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '—';
  return d.toLocaleString(lang, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
