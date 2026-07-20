import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { buildNavSections } from '@modelcity/core/lib/nav/sections';

/**
 * Organism: SideNavRail
 * Narrow icon-only sidebar shown on tablet (md–lg).
 *
 * Each icon links directly to the section root (`rootHref` from the nav
 * model) and exposes the section label as a native tooltip via `title`
 * plus `aria-label` for accessibility. No accordion, no sub-items: the
 * landing page of each section provides the rest of the navigation.
 *
 * Hidden on mobile (`<md`, replaced by BottomNavBar) and on desktop
 * (`≥lg`, replaced by the full SideNavBar).
 *
 * @param {{ lang: string }} props
 */
export default async function SideNavRail({ lang }) {
  const [dict, session] = await Promise.all([
    getDictionary(lang),
    auth0.getSession(),
  ]);
  const n = dict.nav;

  const sections = buildNavSections({ session, n });

  /** Renders a single rail icon link. */
  const railLink = ({ href, icon, label, danger = false }) => (
    <Link
      key={`${href}-${icon}`}
      href={`/${lang}${href}`}
      aria-label={label}
      className={[
        'flex items-center justify-center w-12 h-12 rounded-md transition-colors duration-150',
        danger
          ? 'text-error hover:bg-error-container/40'
          : 'text-on-surface-variant hover:bg-surface-variant/50',
      ].join(' ')}
    >
      <Icon name={icon} size={24} />
    </Link>
  );

  return (
    <aside
      aria-label={n.sidebarNav}
      className="hidden"
    >
      {railLink({ href: '/home', icon: 'home', label: n.home })}
      {sections.map((s) => railLink({ href: s.rootHref, icon: s.icon, label: s.label }))}

      <div className="mt-auto pt-md border-t border-outline-variant w-full flex justify-center">
        <a
          href="/auth/logout"
          aria-label={dict.auth.logout}
          className="flex items-center justify-center w-12 h-12 rounded-md text-error hover:bg-error-container/40 transition-colors duration-150"
        >
          <Icon name="logout" size={24} />
        </a>
      </div>
    </aside>
  );
}

