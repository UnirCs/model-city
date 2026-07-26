---
title: Override the AppShell template
sidebar_label: Override the AppShell
sidebar_position: 5
---

# Override the AppShell template

**Goal:** change the authenticated layout chrome — here add a city announcement bar
above the content — by overriding the
[`AppShell`](../../atomic-design/templates/core/AppShell.md) template.

- **Override file:** `overrides/core/components/templates/AppShell.js`
- **Regen needed:** no.

`AppShell` is the authenticated scaffold rendered by the `(app)` gate. Keep the
**same default export and props** (`children`, `lang`, `sessionExpired`) and reuse
the upstream nav organisms so navigation keeps working.

## Recipe

```js
// overrides/core/components/templates/AppShell.js
import TopNavBar from '@modelcity/core/components/organisms/TopNavBar';
import SideNavBar from '@modelcity/core/components/organisms/SideNavBar';
import SideNavRail from '@modelcity/core/components/organisms/SideNavRail';
import MobileNav from '@modelcity/core/components/organisms/MobileNav';
import MobileSectionTabs from '@modelcity/core/components/organisms/MobileSectionTabs';
import ScrollToTop from '@modelcity/core/components/atoms/ScrollToTop';
import SessionExpiredModal from '@modelcity/core/components/molecules/SessionExpiredModal';

export default function AppShell({ children, lang, sessionExpired = false }) {
  return (
    <div className="flex flex-col min-h-screen">
      <ScrollToTop />
      <TopNavBar lang={lang} />
      {/* City announcement banner — city-only addition */}
      <div className="bg-secondary-container text-on-secondary-container text-caption text-center py-xs px-md">
        🎉 Fiestas de San Fernando — 30 May. Programa completo en Agenda.
      </div>
      <div className="flex flex-1 pt-16 md:pt-20 min-w-0">
        <SideNavRail lang={lang} />
        <SideNavBar lang={lang} />
        <div className="flex-1 min-w-0 flex flex-col">
          <MobileSectionTabs lang={lang} />
          <main id="main" tabIndex={-1} className="flex-1 min-w-0 p-gutter md:p-lg max-w-container-max mx-auto w-full pb-20 lg:pb-lg focus:outline-none">
            {children}
          </main>
        </div>
      </div>
      <MobileNav lang={lang} />
      <SessionExpiredModal open={sessionExpired} lang={lang} />
    </div>
  );
}
```

:::caution[Keep the `<main id="main">` landmark]

The `SkipLink` targets `#main` (WCAG 2.4.1) and the empty/error surfaces assume a
focusable main. Keep `id="main" tabIndex={-1}` on the content region.

:::

## Verify

`npm run dev`, log in and confirm the banner shows on every authenticated page while
nav, skip-link and the session-expired modal still work.
