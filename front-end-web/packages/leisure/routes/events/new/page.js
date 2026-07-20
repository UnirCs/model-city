import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canManageEvents } from '@modelcity/leisure/lib/auth/roles';
import { getAllCityPlaces } from '@modelcity/leisure/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import Icon from '@modelcity/core/components/atoms/Icon';
import EventForm from '@modelcity/leisure/components/organisms/EventForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.eventsCreate');

export const dynamic = 'force-dynamic';

/**
 * Create event page — `/{lang}/events/new`. Backoffice / admin only.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function CreateEventPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.eventForm;
  const typeLabels = dict.leisure.eventTypes;

  const session = await auth0.getSession();
  if (!canManageEvents(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const accessToken = session?.tokenSet?.accessToken;
  const places = await getAllCityPlaces(accessToken);

  return (
    <div className="space-y-lg">
      <BackButton />
      <div className="flex items-center gap-sm">
        <div className="w-10 h-10 rounded-md bg-primary/10 flex items-center justify-center shrink-0">
          <Icon name="event_available" fill size={24} className="text-primary" />
        </div>
        <div>
          <h1 className="text-h2 text-primary font-semibold">{t.pageTitleCreate}</h1>
          <p className="text-body-sm text-on-surface-variant">{t.pageSubtitleCreate}</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <EventForm
          mode="create"
          places={places}
          t={t}
          typeLabels={typeLabels}
          lang={lang}
        />
      </div>
    </div>
  );
}

