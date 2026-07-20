import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageEvents } from '@modelcity/leisure/lib/auth/roles';
import { getEvent, getAllCityPlaces } from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import EventForm from '@modelcity/leisure/components/organisms/EventForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.eventsCreate');

export const dynamic = 'force-dynamic';

/**
 * Edit event page — `/{lang}/events/{id}/edit`. Backoffice / admin only.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function EditEventPage({ params }) {
  const { lang, id } = await params;
  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.eventForm;
  const typeLabels = dict.leisure.eventTypes;

  const session = await auth0.getSession();
  if (!canManageEvents(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const [evRes, places] = await Promise.all([
    getEvent(id, accessToken, true),
    getAllCityPlaces(accessToken),
  ]);

  if ('notFound' in evRes) {
    return <InlineStatus icon="search_off" message={t.notFound} />;
  }
  if ('error' in evRes) {
    return <InlineStatus icon="error_outline" tone="error" message={t.loadError} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />
      <IconPageHeader icon="edit_calendar" title={t.pageTitleEdit} subtitle={t.pageSubtitleEdit} />

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <EventForm
          mode="edit"
          event={evRes.data}
          places={places}
          t={t}
          typeLabels={typeLabels}
          lang={lang}
        />
      </div>
    </div>
  );
}

