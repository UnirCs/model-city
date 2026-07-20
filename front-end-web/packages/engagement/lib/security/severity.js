/**
 * Visual descriptors for each `AlertSeverity` value returned by the
 * citizen-engagement microservice. Centralised here so every UI surface
 * (cards, table rows, map markers) stays consistent.
 */

/** Known severities in display order — highest to lowest. */
export const SEVERITIES = ['IMPORTANT', 'MEDIUM', 'MILD'];

/**
 * @typedef {{
 *   icon:        string,
 *   accentClass: string,
 *   borderClass: string,
 *   chipClass:   string,
 *   bubbleClass: string,
 *   markerHex:   string,
 * }} SeverityVisual
 */

/** Tailwind-friendly visual hooks per severity. */
const VISUALS = {
  IMPORTANT: {
    icon:        'error',
    accentClass: 'bg-error',
    borderClass: 'border-error',
    chipClass:   'bg-error-container text-on-error-container border border-error/30',
    bubbleClass: 'bg-error-container text-error',
    markerHex:   '#ba1a1a',
  },
  MEDIUM: {
    icon:        'warning',
    accentClass: 'bg-[#f59e0b]',
    borderClass: 'border-[#f59e0b]',
    chipClass:   'bg-[#fef3c7] text-[#92400e] border border-[#f59e0b]/40',
    bubbleClass: 'bg-[#fef3c7] text-[#d97706]',
    markerHex:   '#f59e0b',
  },
  MILD: {
    icon:        'info',
    accentClass: 'bg-secondary',
    borderClass: 'border-secondary',
    chipClass:   'bg-secondary-container text-on-secondary-container border border-secondary/30',
    bubbleClass: 'bg-secondary-container text-on-secondary-container',
    markerHex:   '#89d3d4',
  },
};

/**
 * Returns visual descriptors for the given severity, falling back to MILD.
 *
 * @param {string | null | undefined} severity
 * @returns {SeverityVisual}
 */
export function severityVisual(severity) {
  return VISUALS[severity] ?? VISUALS.MILD;
}



