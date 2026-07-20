'use client';

import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';

/**
 * Singleton announcer mounted at the root of the app. Renders two
 * visually-hidden live regions:
 *
 *   - `polite`     — `aria-live="polite"`     · routine status updates
 *   - `assertive`  — `aria-live="assertive"`  · errors and urgent alerts
 *
 * Components anywhere in the tree call `useAnnounce()` to push a message.
 * The provider clears the region between consecutive messages so identical
 * strings still trigger an announcement.
 *
 * WCAG 2.2 — 4.1.3 Status Messages (Level AA).
 */

/**
 * @typedef {'polite' | 'assertive'} AnnouncePriority
 * @typedef {(message: string, priority?: AnnouncePriority) => void} AnnounceFn
 */

/** @type {React.Context<AnnounceFn>} */
const AnnounceContext = createContext(() => {});

/**
 * Wraps the app and provides the announcer hook.
 *
 * @param {{ children: React.ReactNode }} props
 */
export function AnnouncerProvider({ children }) {
  const [polite, setPolite]       = useState('');
  const [assertive, setAssertive] = useState('');
  const timeouts = useRef({ polite: null, assertive: null });

  const announce = useCallback(
    /** @type {AnnounceFn} */
    (message, priority = 'polite') => {
      if (!message) return;

      const setter = priority === 'assertive' ? setAssertive : setPolite;

      // Clear first so identical consecutive messages are re-announced.
      setter('');
      clearTimeout(timeouts.current[priority]);
      timeouts.current[priority] = setTimeout(() => setter(message), 50);
    },
    [],
  );

  useEffect(() => () => {
    clearTimeout(timeouts.current.polite);
    clearTimeout(timeouts.current.assertive);
  }, []);

  return (
    <AnnounceContext.Provider value={announce}>
      {children}
      <div
        aria-live="polite"
        aria-atomic="true"
        role="status"
        className="sr-only"
      >
        {polite}
      </div>
      <div
        aria-live="assertive"
        aria-atomic="true"
        role="alert"
        className="sr-only"
      >
        {assertive}
      </div>
    </AnnounceContext.Provider>
  );
}

/**
 * Returns the `announce(message, priority?)` function provided by the
 * nearest {@link AnnouncerProvider}.
 *
 * @returns {AnnounceFn}
 */
export function useAnnounce() {
  return useContext(AnnounceContext);
}

