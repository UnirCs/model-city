'use client';

import { useEffect } from 'react';
import { usePathname } from 'next/navigation';

/**
 * Atom: ScrollToTop
 *
 * Invisible client component that listens for pathname changes and
 * immediately scrolls the document to the top.  This is necessary
 * because Next.js App Router does not reliably restore the scroll
 * position to 0,0 on every client-side navigation, which causes the
 * page title / description to appear partially hidden — especially on
 * mobile and tablet where there is no side-navigation to force a full
 * re-render.
 *
 * Place this component once inside a layout (e.g. AppShell) so it is
 * always mounted regardless of which child page is active.
 *
 * Uses `behavior: 'instant'` so the jump is imperceptible and does not
 * conflict with the `prefers-reduced-motion` setting (no animation involved).
 */
export default function ScrollToTop() {
  const pathname = usePathname();

  useEffect(() => {
    // Scroll the document viewport to the very top without animation.
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
  }, [pathname]);

  // Renders nothing — side-effect only.
  return null;
}

