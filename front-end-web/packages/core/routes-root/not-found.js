import { cookies } from 'next/headers';
import { getDictionary, SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';
import EmptyState from '@modelcity/core/components/molecules/EmptyState';

/**
 * Global not-found page
 * Shown when a user navigates to a route that does not exist.
 *
 * The copy is themed around the Prince's Gardens of Aranjuez —
 * a famous maze-like garden where paths can lead to dead ends,
 * just like this non-existent page.
 */
export default async function NotFound() {
  const cookieStore = await cookies();
  const langCookie = cookieStore.get('NEXT_LANG')?.value;
  const lang = SUPPORTED_LANGS.includes(langCookie) ? langCookie : 'es';
  const dict = await getDictionary(lang);
  const t = dict.notFound;

  return (
    <EmptyState
      icon="landscape"
      badgeIcon="search_off"
      title={t.title}
      subtitle={t.subtitle}
      body={t.body}
      hint={t.hint}
      hintIcon="nature"
      actionLabel={t.backHome}
      actionHref={`/${lang}`}
      actionIcon="home"
    />
  );
}
