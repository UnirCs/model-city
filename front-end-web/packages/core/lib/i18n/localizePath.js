import { SUPPORTED_LANGS } from './dictionaries';

/**
 * Rewrites the leading `[lang]` segment of an in-app path to `lang`.
 *
 * If the path already starts with a supported locale, that segment is swapped;
 * otherwise the locale is prepended. Used to keep navigation within the locale
 * of the page the user is currently on, even when an earlier language switch
 * left mixed-locale entries in the history (e.g. `/en/events` → `/es/events`).
 *
 * @param {string} path  Absolute in-app path, e.g. `/en/events/1`.
 * @param {string} lang  Target locale code (`es` | `en` | `fr`).
 * @returns {string}     The path under `lang`, e.g. `/es/events/1`.
 */
export function relocalizePath(path, lang) {
  const segments = path.split('/'); // ['', 'en', 'events', '1']
  if (SUPPORTED_LANGS.includes(segments[1])) {
    segments[1] = lang;
    return segments.join('/');
  }
  return `/${lang}${path.startsWith('/') ? path : `/${path}`}`;
}
