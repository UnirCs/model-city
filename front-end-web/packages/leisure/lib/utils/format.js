/**
 * Formatting & presentation utilities for the Leisure module.
 *
 * Framework-agnostic helpers shared across events/ticketing and tourism
 * (routes & city places) — icon mappings, label lookups, price/date/duration
 * formatting. Usable from both Server Components and UI molecules.
 */

/** Material Symbols icon name for an event type. */
export function eventTypeIcon(type) {
  switch (type) {
    case 'MUSIC':           return 'music_note';
    case 'NIGHTLIFE':       return 'nightlife';
    case 'PERFORMING_ARTS': return 'theater_comedy';
    case 'HOBBIES':         return 'extension';
    case 'BUSINESS':        return 'business_center';
    case 'FOOD_AND_DRINK':  return 'restaurant';
    case 'OTHER':
    default:                return 'event';
  }
}

/** Translates an event type via a labels map, falling back to the raw code. */
export function eventTypeLabel(type, labels) {
  return labels?.[type] ?? type;
}

/** Translates a ticket status via a labels map, falling back to the raw code. */
export function ticketStatusLabel(status, labels) {
  return labels?.[status] ?? status;
}

/**
 * Maps a ticket status to a Badge component status keyword.
 *
 * @param {string} status
 * @returns {'active' | 'pending' | 'inactive' | 'error'}
 */
export function ticketStatusBadge(status) {
  switch (status) {
    case 'VALID':
    case 'PAID':
    case 'ACTIVE':
      return 'active';
    case 'PENDING':
      return 'pending';
    case 'REFUNDED':
    case 'CANCELLED':
      return 'inactive';
    default:
      return 'error';
  }
}

/**
 * Formats a price + currency using `Intl.NumberFormat`. Returns the localised
 * "free" label when the event is not paid (or the price is zero).
 *
 * @param {number | string | null | undefined} amount
 * @param {string} currency  ISO 4217 currency code (e.g. "EUR")
 * @param {string} lang
 * @param {string} freeLabel
 */
export function formatPrice(amount, currency, lang, freeLabel) {
  const num = Number(amount);
  if (!Number.isFinite(num) || num <= 0) return freeLabel;
  try {
    return new Intl.NumberFormat(lang, {
      style: 'currency',
      currency: currency || 'EUR',
    }).format(num);
  } catch {
    return `${num.toFixed(2)} ${currency || ''}`.trim();
  }
}

/**
 * Formats an ISO date-time string for the given locale (medium date + short
 * time). Returns an empty string for missing values.
 *
 * @param {string | null | undefined} iso
 * @param {string} lang
 */
export function formatEventDateTime(iso, lang) {
  if (!iso) return '';
  try {
    const d = new Date(iso);
    return new Intl.DateTimeFormat(lang, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(d);
  } catch {
    return iso;
  }
}

/**
 * Formats a duration expressed in minutes as "Xh Ymin" (or "Ymin" / "Xh").
 *
 * @param {number | null | undefined} minutes
 * @param {{ hours: string, minutes: string }} units  – localised unit suffixes
 * @returns {string}
 */
export function formatDuration(minutes, units) {
  if (minutes == null || Number.isNaN(minutes)) return '—';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  if (h > 0 && m > 0) return `${h}${units.hours} ${m}${units.minutes}`;
  if (h > 0) return `${h}${units.hours}`;
  return `${m}${units.minutes}`;
}

/**
 * Returns the localised label for a city-place category, falling back to
 * the raw code when no translation is defined.
 *
 * @param {string | null | undefined} code
 * @param {Record<string, string>} labels
 * @returns {string}
 */
export function categoryLabel(code, labels) {
  if (!code) return '—';
  return labels[code] ?? code;
}

/**
 * Maps a city-place category code to a representative Material Symbol name.
 *
 * @param {string | null | undefined} code
 * @returns {string}
 */
export function categoryIcon(code) {
  switch (code) {
    case 'MONUMENT':   return 'account_balance';
    case 'PARK':       return 'park';
    case 'MUSEUM':     return 'museum';
    case 'CHURCH':     return 'church';
    case 'VIEWPOINT':  return 'visibility';
    case 'RESTAURANT': return 'restaurant';
    case 'SQUARE':     return 'location_city';
    case 'STATION':    return 'train';
    default:           return 'place';
  }
}
