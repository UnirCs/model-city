'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: NavGroup
 * Accordion-style sidebar navigation group for a citizen service section.
 *
 * Can work in two modes:
 * - **Uncontrolled** (default): manages its own open/close state with an
 *   auto-expand when the current path is inside the section.
 * - **Controlled**: when `isOpen` + `onToggle` props are provided the parent
 *   owns the state (e.g. SideNavAccordion for mutual-exclusion behaviour).
 *
 * When rendered inside `SideNavAccordion`, the parent pre-computes `isActive`
 * (accounting for extra `sectionPaths`) and passes it as a prop so the header
 * highlights correctly even for paths outside the primary `sectionPath`.
 *
 * @param {{
 *   sectionPath: string,
 *   icon: string,
 *   label: string,
 *   items: Array<{ href: string | null, label: string }>,
 *   isOpen?: boolean,
 *   isActive?: boolean,
 *   onToggle?: () => void,
 * }} props
 */
export default function NavGroup({ sectionPath, icon, label, items = [], isOpen: isOpenProp, isActive: isActiveProp, onToggle }) {
  const { lang } = useTranslations();
  const pathname = usePathname();

  const localizedSection = `/${lang}${sectionPath}`;
  // When the parent passes isActive (e.g. SideNavAccordion with extra paths),
  // honour it; otherwise fall back to the local startsWith check.
  const isActive = isActiveProp !== undefined
    ? isActiveProp
    : pathname.startsWith(localizedSection);

  // Uncontrolled internal state — only used when parent does not control open/close.
  const [isOpenInternal, setIsOpenInternal] = useState(isActive);
  useEffect(() => {
    if (isActive) setIsOpenInternal(true);
  }, [isActive]);

  // Honour controlled props when provided, otherwise fall back to internal state.
  const controlled = isOpenProp !== undefined && onToggle !== undefined;
  const isOpen   = controlled ? isOpenProp : isOpenInternal;
  const handleToggle = controlled ? onToggle : () => setIsOpenInternal((prev) => !prev);

  const parentBase =
    'flex items-center gap-md px-sm py-sm rounded-md transition-colors duration-150 text-body-md w-full text-left cursor-pointer';
  const parentColor = isActive
    ? 'bg-secondary-container/40 text-secondary font-semibold'
    : 'text-on-surface-variant hover:bg-surface-variant/50';

  return (
    <div className="flex flex-col">
      {/* Parent toggle button — no navigation, just expand/collapse */}
      <button
        type="button"
        onClick={handleToggle}
        className={[parentBase, parentColor].join(' ')}
        aria-expanded={isOpen}
      >
        <Icon name={icon} fill={isActive} size={22} />
        <span className="flex-1">{label}</span>
        {/* Chevron rotates 180° when open */}
        <span
          className="transition-transform duration-300 material-symbols-outlined"
          style={{
            fontSize: 18,
            fontVariationSettings: "'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 18",
            transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)',
          }}
          aria-hidden="true"
        >
          expand_more
        </span>
      </button>

      {/* Animated container — CSS grid-row trick for perfectly smooth expand */}
      <div
        className="transition-[grid-template-rows] duration-300 ease-in-out"
        style={{ display: 'grid', gridTemplateRows: isOpen ? '1fr' : '0fr' }}
      >
        {/* Inner wrapper must have overflow-hidden for the animation to clip */}
        <div className="overflow-hidden">
          {/* Sub-items: left border accent + right padding (citizen-services-rules) */}
          <div className="ml-3 mt-2 mb-1 flex flex-col gap-2 border-l-[3px] border-secondary-container pl-sm pr-xs">
            {(() => {
              // Find the single most-specific item href that matches the current path.
              // This prevents a shorter href (e.g. /questions) from also being
              // marked active when a longer sibling (e.g. /questions/new) matches.
              const activeHref = items
                .filter((i) => i.href)
                .map((i) => `/${lang}${i.href}`)
                .filter((h) => pathname === h || pathname.startsWith(`${h}/`))
                .sort((a, b) => b.length - a.length)[0]; // longest match wins

              return items.map((item, idx) => {
                if (!item.href) {
                  return (
                    <span
                      key={idx}
                      className="px-sm py-base rounded-md text-label-md text-on-surface-variant/40 cursor-not-allowed select-none flex items-center gap-xs"
                    >
                      {item.icon && (
                        <span
                          className="material-symbols-outlined shrink-0"
                          style={{ fontSize: 14, fontVariationSettings: "'FILL' 0, 'wght' 300" }}
                          aria-hidden="true"
                        >
                          {item.icon}
                        </span>
                      )}
                      {item.label}
                    </span>
                  );
                }

                const itemHref = `/${lang}${item.href}`;
                const itemActive = itemHref === activeHref;

                return (
                  <Link
                    key={item.href}
                    href={itemHref}
                    aria-current={itemActive ? 'page' : undefined}
                    className={[
                      'px-sm py-base rounded-md text-label-md transition-colors duration-150 flex items-center gap-xs',
                      itemActive
                        ? 'bg-surface-container-high text-on-surface font-semibold'
                        : 'text-on-surface-variant hover:bg-surface-container-high',
                    ].join(' ')}
                  >
                    {item.icon && (
                      <span
                        className="material-symbols-outlined shrink-0"
                        style={{ fontSize: 14, fontVariationSettings: `'FILL' ${itemActive ? 1 : 0}, 'wght' 300` }}
                        aria-hidden="true"
                      >
                        {item.icon}
                      </span>
                    )}
                    {item.label}
                  </Link>
                );
              });
            })()}
          </div>
        </div>
      </div>
    </div>
  );
}
