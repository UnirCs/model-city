'use client';

import { useRef, useEffect } from 'react';
import { usePathname } from 'next/navigation';
import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/**
 * Molecule: SectionTabsBar (client)
 * Sticky horizontal pill-navigation shown on mobile when the current URL is
 * inside a nav section that has more than one navigable sub-item.
 *
 * Design:
 * - Pill shape (rounded-full) matching the active-state pattern of the app.
 * - Active pill: light teal background (`secondary-container/60`) + bold label.
 * - Inactive pill: ghost, hover adds a surface tint.
 * - Scrollbar is hidden via the `scrollbar-none` utility defined in globals.css.
 * - On every path change the active pill is automatically scrolled into the
 *   centre of the rail via `scrollIntoView({ inline: 'center' })`.
 *
 * Hidden on `md+` (desktop/tablet use the SideNavBar accordion).
 * Sticks below TopNavBar (`top-16`) without affecting the layout flow.
 *
 * @param {{
 *   sections: import('@modelcity/core/lib/nav/sections').NavSection[],
 * }} props
 */
export default function SectionTabsBar({ sections }) {
  const pathname = usePathname();
  const { lang } = useTranslations();
  const navRef = useRef(null);

  // Identify which top-level section the current URL belongs to.
  const activeSection = sections.find((s) => {
    const paths = [s.sectionPath, ...(s.sectionPaths ?? [])];
    return paths.some((p) => pathname.startsWith(`/${lang}${p}`));
  });

  // Only keep items with a valid href.
  const items = activeSection?.items.filter((i) => i.href) ?? [];

  // Auto-scroll the active pill into view on every navigation.
  useEffect(() => {
    const nav = navRef.current;
    if (!nav) return;
    const activeEl = nav.querySelector('[aria-current="page"]');
    if (activeEl) {
      activeEl.scrollIntoView({
        behavior: 'smooth',
        inline: 'center',
        block: 'nearest',
      });
    }
  }, [pathname]);

  // No bar needed for zero or one item.
  if (!activeSection || items.length <= 1) return null;

  return (
    <div className="lg:hidden sticky top-16 md:top-24 z-30 bg-surface-container-low border-b border-outline-variant shrink-0 py-xs">
      <nav
        ref={navRef}
        aria-label={activeSection.label}
        className="flex gap-xs px-sm py-xs overflow-x-auto scrollbar-none"
      >
        {items.map((item) => {
          const localizedHref = `/${lang}${item.href}`;
          const active =
            pathname === localizedHref ||
            pathname.startsWith(`${localizedHref}/`);

          return (
            <LocalizedLink
              key={item.href}
              href={item.href}
              prefetch={false}
              aria-current={active ? 'page' : undefined}
              className={[
                // Pill base
                'shrink-0 flex items-center gap-xs px-md py-xs rounded-full',
                'text-label-md whitespace-nowrap transition-colors duration-150',
                active
                  ? 'bg-secondary-container/60 text-secondary font-semibold'
                  : 'text-on-surface-variant hover:bg-surface-container-high',
              ].join(' ')}
            >
              {item.icon && (
                <span
                  className="material-symbols-outlined shrink-0"
                  style={{
                    fontSize: 15,
                    fontVariationSettings: `'FILL' ${active ? 1 : 0}, 'wght' 300`,
                  }}
                  aria-hidden="true"
                >
                  {item.icon}
                </span>
              )}
              {item.label}
            </LocalizedLink>
          );
        })}
      </nav>
    </div>
  );
}


