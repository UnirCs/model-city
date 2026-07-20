'use client';

import { createContext, useContext, useEffect, useMemo, useRef, useCallback } from 'react';
import { usePathname } from 'next/navigation';

/**
 * @typedef {{ getPreviousPath: () => string | null }} NavigationHistoryValue
 * @type {React.Context<NavigationHistoryValue>}
 */
const NavigationHistoryContext = createContext({ getPreviousPath: () => null });

/**
 * Tracks the in-app navigation stack of pathnames so consumers (e.g.
 * {@link BackButton}) can resolve *where* a "back" action would land — something
 * the browser History API never exposes.
 *
 * Mounted once at the `[lang]` layout so it observes every client-side
 * navigation, including those starting on pages where the back control itself is
 * not rendered (a list page records its own path before the user opens a detail
 * page that hosts the BackButton).
 *
 * @param {{ children: React.ReactNode }} props
 */
export function NavigationHistoryProvider({ children }) {
  const pathname = usePathname();
  const stackRef = useRef([]);

  useEffect(() => {
    const stack = stackRef.current;
    if (stack[stack.length - 1] === pathname) return; // refresh / no-op re-render
    if (stack[stack.length - 2] === pathname) {
      stack.pop(); // user moved back: unwind the stack
    } else {
      stack.push(pathname); // forward navigation
    }
  }, [pathname]);

  // Read from the ref at call time so the value never triggers re-renders.
  const getPreviousPath = useCallback(() => {
    const stack = stackRef.current;
    return stack.length >= 2 ? stack[stack.length - 2] : null;
  }, []);

  const value = useMemo(() => ({ getPreviousPath }), [getPreviousPath]);

  return (
    <NavigationHistoryContext.Provider value={value}>
      {children}
    </NavigationHistoryContext.Provider>
  );
}

/**
 * Returns the navigation-history helpers for the current session.
 * Must be called from a Client Component inside a NavigationHistoryProvider.
 *
 * @returns {NavigationHistoryValue}
 */
export function useNavigationHistory() {
  return useContext(NavigationHistoryContext);
}
