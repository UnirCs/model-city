import { cookies } from 'next/headers';
import { notFound } from 'next/navigation';

import { getDictionary, SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';
import { SITE_URL } from '@modelcity/core/lib/seo/routes';
import { TranslationsProvider } from '@modelcity/core/lib/i18n/TranslationsProvider';
import { NavigationHistoryProvider } from '@modelcity/core/lib/nav/NavigationHistoryProvider';
import { ThemeProvider } from '@modelcity/core/components/providers/ThemeProvider';
import { AnnouncerProvider } from '@modelcity/core/lib/a11y/AnnouncerProvider';
import { AccessibilityProvider } from '@modelcity/core/lib/a11y/AccessibilityContext';
import SkipLink from '@modelcity/core/components/atoms/SkipLink';
import HtmlLang from '@modelcity/core/components/atoms/HtmlLang';

/** Generate static params for every supported locale. */
export async function generateStaticParams() {
  return SUPPORTED_LANGS.map((lang) => ({ lang }));
}

/**
 * Per-language metadata. The page title and description come from the
 * locale's `meta` namespace so they remain translatable. Per-route
 * `generateMetadata` exports can override the title via the configured
 * `template` (e.g. `"%s · Portal Ciudadano"`).
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export async function generateMetadata({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(SUPPORTED_LANGS.includes(lang) ? lang : 'es');
  const meta = dict.meta ?? {};
  return {
    metadataBase: new URL(SITE_URL),
    title: {
      default:  meta.title         ?? '',
      template: meta.titleTemplate ?? '%s',
    },
    description: meta.description  ?? '',
  };
}

/**
 * Root layout for the entire app. Lives under `[lang]/` so the `lang`
 * attribute on `<html>` is always correct — WCAG 2.2 SC 3.1.1.
 *
 * Responsibilities:
 *  - Render the HTML shell (`<html lang>`, `<body>`).
 *  - Apply the saved theme from the cookie before the first paint (no FOUC).
 *  - Provide translations, theme state and the accessibility announcer.
 *  - Render the skip-link as the first focusable element — SC 2.4.1.
 *
 * Authenticated pages render an `AppShell` containing the `<main id="main">`
 * landmark the skip-link targets. Public/onboarding pages must provide
 * their own `<main id="main" tabIndex={-1}>` element.
 *
 * @param {{ children: React.ReactNode, params: Promise<{ lang: string }> }} props
 */
export default async function RootLangLayout({ children, params }) {
  const { lang } = await params;

  if (!SUPPORTED_LANGS.includes(lang)) {
    notFound();
  }

  const [cookieStore, dict] = await Promise.all([
    cookies(),
    getDictionary(lang),
  ]);

  const a11y = dict.a11y ?? {};

  return (
    <>
      <HtmlLang lang={lang} />
      <SkipLink>{a11y.skipToContent}</SkipLink>
      <ThemeProvider>
        <AccessibilityProvider>
          <AnnouncerProvider>
            <TranslationsProvider lang={lang} dict={dict}>
              <NavigationHistoryProvider>
                {children}
              </NavigationHistoryProvider>
            </TranslationsProvider>
          </AnnouncerProvider>
        </AccessibilityProvider>
      </ThemeProvider>
    </>
  );
}


