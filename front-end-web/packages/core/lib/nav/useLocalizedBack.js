'use client';

import { useCallback } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import { relocalizePath } from '@modelcity/core/lib/i18n/localizePath';
import { useNavigationHistory } from '@modelcity/core/lib/nav/NavigationHistoryProvider';

/**
 * Returns the parent of an in-app path (its path minus the last segment).
 * Used as the locale-safe fallback when there is no in-app history to go back
 * to (e.g. the page was opened from a deep link or a fresh tab).
 *
 * @param {string} path  e.g. `/es/events/1`.
 * @param {string} lang  Current locale, used when the path has no parent.
 * @returns {string}     e.g. `/es/events`; `/{lang}` for top-level paths.
 */
function parentPath(path, lang) {
  const segments = path.split('/').filter(Boolean); // ['es', 'events', '1']
  segments.pop();
  return segments.length > 0 ? `/${segments.join('/')}` : `/${lang}`;
}

/**
 * Locale-aware "back" navigation, shared by the {@link BackButton} atom and the
 * "cancel" controls of the form organisms.
 *
 * Resolves where the previous in-app navigation came from and re-localises that
 * path to the locale of the page the user is currently on, so a back action
 * never reverts an earlier language switch (on `/es/events/1` it lands on
 * `/es/events`, never `/en/events`). The browser History API never exposes the
 * URL a back action would land on, so the previous path comes from
 * {@link NavigationHistoryProvider}.
 *
 *  - Previous entry already in the current locale → native `router.back()`,
 *    which preserves the browser forward entry and scroll restoration.
 *  - Previous entry in a different locale → `router.push()` to the re-localised
 *    path, so the current locale wins.
 *  - No in-app history (deep link) → falls back to the current path's parent.
 *
 * @returns {() => void} A stable callback that performs the back navigation.
 */
export function useLocalizedBack() {
  const router = useRouter();
  const pathname = usePathname();
  const { lang } = useTranslations();
  const { getPreviousPath } = useNavigationHistory();

  return useCallback(() => {
    const previous = getPreviousPath();
    if (!previous) {
      router.push(parentPath(pathname, lang));
      return;
    }
    const target = relocalizePath(previous, lang);
    if (target === previous) {
      router.back();
    } else {
      router.push(target);
    }
  }, [router, pathname, lang, getPreviousPath]);
}
