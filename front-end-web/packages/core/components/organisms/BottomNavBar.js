'use client';

import { useState, useEffect, useId } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import Icon from '@modelcity/core/components/atoms/Icon';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/**
 * Organism: BottomNavBar
 * Fixed bottom navigation for mobile (`<md`).
 *
 * Layout: Home (left) · FAB (centre) · Profile (right).
 *
 * The FAB toggles a floating services sheet.  Animation is driven by a
 * four-state machine ('closed' | 'opening' | 'open' | 'closing') so both
 * the enter and exit transitions play using CSS @keyframes, which is more
 * reliable than CSS-transition class toggling (avoids the "snaps shut"
 * problem on iOS Safari and React batched renders).
 *
 * Sheet states → CSS:
 *   closed  → hidden in place (opacity-0, translate-y-full, pointer-events-none)
 *   opening → animate-[sheet-slide-in_...] – keyframe defined in globals.css
 *   open    → visible, interactive
 *   closing → animate-[sheet-slide-out_...] – keyframe defined in globals.css
 *
 * @param {{
 *   sections: import('@modelcity/core/lib/nav/sections').NavSection[],
 *   labels: {
 *     home: string,
 *     services: string,
 *     profile: string,
 *     servicesTitle: string,
 *     servicesClose: string,
 *     mobileNav: string,
 *   },
 * }} props
 */
export default function BottomNavBar({ sections, labels }) {
  const pathname = usePathname();
  const router = useRouter();
  const { lang } = useTranslations();

  // 'closed' | 'opening' | 'open' | 'closing'
  const [animState, setAnimState] = useState('closed');
  const dialogId = useId();
  const titleId = useId();

  const isVisible = animState !== 'closed';           // backdrop + aria visibility
  const isSheetOpen = animState === 'open' || animState === 'opening'; // FAB label / icon

  /** Transition to next stable state when the CSS animation ends. */
  function handleAnimationEnd() {
    if (animState === 'opening') setAnimState('open');
    else if (animState === 'closing') setAnimState('closed');
  }

  /** Close on any navigation change. */
  useEffect(() => {
    if (animState !== 'closed') setAnimState('closing');
    // animState intentionally omitted — we only want to react to pathname.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname]);

  /** ESC key closes the sheet. */
  useEffect(() => {
    if (!isSheetOpen) return;
    const handler = (e) => { if (e.key === 'Escape') setAnimState('closing'); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [isSheetOpen]);

  const isActive = (path) => {
    const localised = `/${lang}${path}`;
    return pathname === localised || pathname.startsWith(`${localised}/`);
  };

  const homeActive = isActive('/home');
  const profileActive = isActive('/profile');

  /** CSS class string for the sheet based on current animation state. */
  const sheetAnimClass = {
    closed:  'translate-y-full opacity-0 pointer-events-none',
    opening: 'animate-[sheet-slide-in_0.32s_cubic-bezier(0.34,1.56,0.64,1)_both] pointer-events-auto',
    open:    'translate-y-0 opacity-100 pointer-events-auto',
    closing: 'animate-[sheet-slide-out_0.24s_ease-in_both] pointer-events-none',
  }[animState];

  return (
    <>
      {/* ── Backdrop ────────────────────────────────────────────── */}
      <div
        aria-hidden="true"
        onClick={() => setAnimState('closing')}
        className={[
          'lg:hidden fixed inset-0 z-40 bg-scrim/50',
          'transition-opacity duration-300',
          isVisible ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none',
        ].join(' ')}
      />

      {/* ── Services sheet ──────────────────────────────────────── */}
      <div
        id={dialogId}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-hidden={!isSheetOpen}
        onAnimationEnd={handleAnimationEnd}
        className={[
          'lg:hidden fixed inset-x-4 bottom-18 z-50',
          'bg-surface-container-lowest rounded-md',
          'border border-outline-variant',
          'shadow-[0_8px_32px_color-mix(in_srgb,var(--ds-primary)_16%,transparent)]',
          'flex flex-col max-h-[72vh]',
          sheetAnimClass,
        ].join(' ')}
      >
        {/* Centred title */}
        <div className="px-md pt-xs pb-sm shrink-0">
          <h2 id={titleId} className="text-h4 text-on-surface font-semibold text-center">
            {labels.servicesTitle}
          </h2>
        </div>

        {/* Services grid — no Home tile */}
        <nav aria-label={labels.servicesTitle} className="overflow-y-auto px-md pb-md">
          <div className="grid grid-cols-3 gap-sm">
            {sections.map((section) => (
              <button
                key={section.sectionPath}
                type="button"
                onClick={() => {
                  setAnimState('closing');
                  router.push(`/${lang}${section.rootHref}`);
                }}
                className="flex flex-col items-center gap-xs p-sm rounded-md
                           hover:bg-surface-container-high active:bg-surface-container-highest
                           transition-colors text-center"
              >
                <div className="w-12 h-12 rounded-md bg-secondary-container/40 text-secondary flex items-center justify-center">
                  <Icon name={section.icon} fill size={24} />
                </div>
                <span className="text-caption text-on-surface-variant leading-tight">{section.label}</span>
              </button>
            ))}
          </div>
        </nav>
      </div>

      {/* ── Bottom navigation bar ───────────────────────────────── */}
      <nav
        aria-label={labels.mobileNav}
        className="lg:hidden fixed bottom-0 inset-x-0 z-50 h-16 bg-surface-container-low/50 backdrop-blur-md border-t border-outline-variant"
      >
        <div className="grid grid-cols-3 h-full items-center">

          {/* Home */}
          <LocalizedLink
            href="/home"
            prefetch={false}
            aria-label={labels.home}
            aria-current={homeActive ? 'page' : undefined}
            className={[
              'flex flex-col items-center justify-center gap-0.5 h-full transition-colors',
              homeActive ? 'text-secondary' : 'text-on-surface-variant',
            ].join(' ')}
          >
            <Icon name="home" fill={homeActive} size={22} />
            <span className="text-caption">{labels.home}</span>
          </LocalizedLink>

          {/* FAB — morphs to red close button while sheet is open */}
          <div className="flex items-center justify-center">
            <button
              type="button"
              onClick={() => setAnimState((s) =>
                s === 'closed' || s === 'closing' ? 'opening' : 'closing'
              )}
              aria-label={isSheetOpen ? labels.servicesClose : labels.services}
              aria-expanded={isSheetOpen}
              aria-haspopup="dialog"
              aria-controls={dialogId}
              className={[
                'w-14 h-14 rounded-md -translate-y-4',
                'flex items-center justify-center',
                'transition-[background-color,box-shadow] duration-300 ease-out',
                'active:scale-95',
                isSheetOpen
                  ? 'bg-error text-on-error shadow-[0_4px_16px_color-mix(in_srgb,var(--ds-error)_40%,transparent)]'
                  : 'bg-primary text-on-primary shadow-[0_4px_16px_color-mix(in_srgb,var(--ds-primary)_40%,transparent)]',
              ].join(' ')}
            >
              {/* Cross-fade between apps ↔ close icons */}
              <span className="relative w-7 h-7">
                <Icon
                  name="apps"
                  size={28}
                  className={[
                    'absolute inset-0 transition-[opacity,transform] duration-250',
                    isSheetOpen ? 'opacity-0 scale-50' : 'opacity-100 scale-100',
                  ].join(' ')}
                />
                <Icon
                  name="close"
                  size={28}
                  className={[
                    'absolute inset-0 transition-[opacity,transform] duration-250',
                    isSheetOpen ? 'opacity-100 scale-100' : 'opacity-0 scale-50',
                  ].join(' ')}
                />
              </span>
            </button>
          </div>

          {/* Profile */}
          <LocalizedLink
            href="/profile"
            prefetch={false}
            aria-label={labels.profile}
            aria-current={profileActive ? 'page' : undefined}
            className={[
              'flex flex-col items-center justify-center gap-0.5 h-full transition-colors',
              profileActive ? 'text-secondary' : 'text-on-surface-variant',
            ].join(' ')}
          >
            <Icon name="person" fill={profileActive} size={22} />
            <span className="text-caption">{labels.profile}</span>
          </LocalizedLink>

        </div>
      </nav>
    </>
  );
}



