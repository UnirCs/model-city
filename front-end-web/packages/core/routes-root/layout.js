import { Public_Sans } from 'next/font/google';
import { cookies } from 'next/headers';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';

// Shim preludes — side-effect statements the codegen injects at the top of the
// generated app-dir shim (`src/app/layout.js`), NOT executed from this core
// module. They belong to the orchestrator's app directory, not to core:
//   1. The generated composition root registers the enabled feature modules'
//      i18n loaders and nav contributors into core. It must run before any
//      getDictionary call, so it is the shim's first statement (ES imports are
//      evaluated in source order, before this module is even imported).
//   2. The generated Tailwind manifest: it imports the (possibly
//      city-overridden) core stylesheet and adds one `@source` per enabled
//      feature module. Importing it from the app directory keeps global CSS on
//      Next's supported path; the underlying stylesheet lives in core/styles.
// @shim-prelude: import '@/lib/composition/registerModules';
// @shim-prelude: import '@/styles/modules.css';

/**
 * Public Sans — loaded at the root so every segment (including not-found.js)
 * inherits the font variable.
 */
const publicSans = Public_Sans({
  subsets: ['latin'],
  weight: ['400', '600', '700'],
  variable: '--font-public-sans',
  display: 'swap',
});

/**
 * True root layout — renders the HTML shell shared by every route,
 * including special files (not-found.js, error.js) that live outside
 * the [lang] segment and therefore cannot rely on [lang]/layout.js.
 *
 * Responsibilities here are intentionally minimal:
 *  - Import the global stylesheet.
 *  - Load the font and expose its CSS variable.
 *  - Apply the saved theme cookie to avoid FOUC.
 *  - Render the Material Symbols icon font link.
 *
 * Everything language-specific (lang attribute, providers, skip-link)
 * is handled by the nested [lang]/layout.js.
 */
export default async function RootLayout({ children }) {
  const cookieStore = await cookies();
  const isDark = cookieStore.get('MODEL-CITY-THEME')?.value === 'dark';
  const lang = cookieStore.get('NEXT_LANG')?.value ?? 'es';
  const dict = await getDictionary(lang);

  return (
    <html
      lang={lang}
      className={[publicSans.variable, isDark ? 'dark' : ''].join(' ').trim()}
      suppressHydrationWarning
    >
      {/* eslint-disable-next-line @next/next/no-head-element */}
      <head>
        <title>{dict.meta.title}</title>
        <link
          rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"
        />
      </head>
      <body className="min-h-screen flex flex-col font-sans">
        {children}
      </body>
    </html>
  );
}
