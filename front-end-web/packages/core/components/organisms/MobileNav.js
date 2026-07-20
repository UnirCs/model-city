import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { buildNavSections } from '@modelcity/core/lib/nav/sections';
import BottomNavBar from '@modelcity/core/components/organisms/BottomNavBar';

/**
 * Organism: MobileNav (server wrapper)
 * Resolves the role-gated nav sections on the server and hands them
 * down to the client `BottomNavBar` (which owns the FAB sheet state).
 *
 * Only rendered for the `<md` breakpoint by AppShell; the inner
 * component applies its own `md:hidden` so it collapses cleanly
 * if mounted at larger viewports.
 *
 * @param {{ lang: string }} props
 */
export default async function MobileNav({ lang }) {
  const [dict, session] = await Promise.all([
    getDictionary(lang),
    auth0.getSession(),
  ]);
  const n = dict.nav;
  const sections = buildNavSections({ session, n });

  return (
    <BottomNavBar
      sections={sections}
      labels={{
        home: n.home,
        services: n.mobileNav.services,
        profile: dict.auth.myProfile,
        servicesTitle: n.mobileNav.servicesTitle,
        servicesClose: n.mobileNav.servicesClose,
        mobileNav: dict.a11y.mobileNav,
      }}
    />
  );
}
