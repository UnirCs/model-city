import NavItem from '@modelcity/core/components/molecules/NavItem';
import SideNavAccordion from '@modelcity/core/components/organisms/SideNavAccordion';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { buildNavSections } from '@modelcity/core/lib/nav/sections';

/**
 * Organism: SideNavBar
 * Full-width sidebar navigation (desktop only, ≥lg).
 *
 * - Hidden on mobile (`<md`): replaced by the BottomNavBar/Drawer.
 * - Hidden on tablet (md–lg): replaced by the narrow SideNavRail.
 * - Visible on desktop (`≥lg`) as a 288 px sticky column with full accordion.
 *
 * Reads the Auth0 session server-side so role-based gating happens once.
 *
 * @param {{ lang: string }} props
 */
export default async function SideNavBar({ lang }) {
  const [dict, session] = await Promise.all([
    getDictionary(lang),
    auth0.getSession(),
  ]);
  const n = dict.nav;

  const sections = buildNavSections({ session, n });

  return (
    <aside
      aria-label={n.sidebarNav}
      className="hidden lg:flex flex-col h-[calc(100vh-80px)] w-72 sticky top-20 left-0 bg-surface-container-low/50 backdrop-blur-xs border-r border-outline-variant px-md py-md gap-base shrink-0 overflow-y-auto"
    >
      {/* Main navigation */}
      <nav className="flex-1 flex flex-col gap-xs" aria-label={n.sidebarNav}>
        {/* Home — direct link, no sub-sections */}
        <NavItem href="/home" icon="home" label={n.home} />

        {/* Grouped sections — accordion: only one open at a time */}
        <SideNavAccordion groups={sections} />
      </nav>

    </aside>
  );
}
