import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { buildNavSections } from '@modelcity/core/lib/nav/sections';
import SectionTabsBar from '@modelcity/core/components/molecules/SectionTabsBar';

/**
 * Organism: MobileSectionTabs (async Server Component)
 * Resolves the role-gated nav sections on the server and renders the
 * client {@link SectionTabsBar} which displays sub-section tabs on mobile
 * when the user is inside a multi-item section.
 *
 * Sits between the TopNavBar and the main content in AppShell so the tabs
 * stick correctly below the top bar (`sticky top-16`).
 *
 * @param {{ lang: string }} props
 */
export default async function MobileSectionTabs({ lang }) {
  const [dict, session] = await Promise.all([
    getDictionary(lang),
    auth0.getSession(),
  ]);

  const sections = buildNavSections({ session, n: dict.nav });

  return <SectionTabsBar sections={sections} />;
}

