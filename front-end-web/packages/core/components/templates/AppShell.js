import TopNavBar from '@modelcity/core/components/organisms/TopNavBar';
import SideNavBar from '@modelcity/core/components/organisms/SideNavBar';
import SideNavRail from '@modelcity/core/components/organisms/SideNavRail';
import MobileNav from '@modelcity/core/components/organisms/MobileNav';
import MobileSectionTabs from '@modelcity/core/components/organisms/MobileSectionTabs';
import ScrollToTop from '@modelcity/core/components/atoms/ScrollToTop';
import SessionExpiredModal from '@modelcity/core/components/molecules/SessionExpiredModal';

/**
 * Template: AppShell
 * Base layout for authenticated pages.
 *
 * Composition per breakpoint:
 * - Mobile (`<md`):  TopNavBar + SectionTabsBar (when inside multi-item section)
 *                    + content (pb-20 to clear BottomNavBar) + MobileNav (FAB).
 * - Tablet (md–lg): TopNavBar + SideNavRail (w-16) + content.
 * - Desktop (`≥lg`): TopNavBar + SideNavBar (w-72) + content.
 *
 * @param {{ children: React.ReactNode, lang: string, sessionExpired?: boolean }} props
 */
export default function AppShell({ children, lang, sessionExpired = false }) {
  return (
    <div className="flex flex-col min-h-screen">
      {/* ── Subtle page backdrop ──────────────────
          Decorative, very low-opacity image covering the entire page
          including the top bar and sidebar.
          `fixed` + `-z-10` keeps it behind the content without affecting layout. */}
      <div
        aria-hidden="true"
        className="pointer-events-none select-none fixed top-0 left-0 right-0 bottom-0 -z-10 bg-center bg-cover bg-no-repeat opacity-[0.08]"
        style={{ backgroundImage: `url('${process.env.NEXT_PUBLIC_BACKGROUND_IMAGE_URL}')` }}
      />
      {/* Scroll to top on every client-side navigation. */}
      <ScrollToTop />
      <TopNavBar lang={lang} />
      {/* pt-16 matches TopNavBar h-16 on mobile, md:pt-20 matches h-20 from md+. */}
      <div className="flex flex-1 pt-16 md:pt-20 min-w-0">
        <SideNavRail lang={lang} />
        <SideNavBar lang={lang} />
        {/* Content column: section tabs (mobile) + main */}
        <div className="flex-1 min-w-0 flex flex-col">
          {/* Section sub-navigation tabs — mobile only, sticky below TopNavBar */}
          <MobileSectionTabs lang={lang} />
          {/* Main content area. id="main" is the SkipLink target (SC 2.4.1);
              tabIndex=-1 lets it receive programmatic focus when skipped to.
              `min-w-0` prevents flex items from overflowing the viewport. */}
          <main
            id="main"
            tabIndex={-1}
            className="flex-1 min-w-0 p-gutter md:p-lg max-w-container-max mx-auto w-full pb-20 lg:pb-lg focus:outline-none"
          >
            {children}
          </main>
        </div>
      </div>
      <MobileNav lang={lang} />
      <SessionExpiredModal open={sessionExpired} lang={lang} />
    </div>
  );
}
