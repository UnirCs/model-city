import Icon from '@modelcity/core/components/atoms/Icon';
import CatalogueCard from '@modelcity/core/components/molecules/CatalogueCard';
import CreateCard from '@modelcity/leisure/components/molecules/CreateCard';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';
import { getEvents } from '@modelcity/leisure/lib/api/client';
import { formatEventDateTime, formatPrice } from '@modelcity/leisure/lib/utils/format';

/**
 * Organism: EventsList (async)
 *
 * Fetches one page of events from the leisure microservice and renders the
 * card grid (with an optional staff "create" card), pagination, and the
 * fetch-error / empty states. Designed to be wrapped in a `<Suspense>` so the
 * filter controls render immediately.
 *
 * @param {{
 *   lang: string,
 *   page: number,
 *   eventType: string | null,
 *   paid: boolean | null,
 *   accessToken: string | undefined,
 *   canManage: boolean,
 *   t: object,                              // leisure.events dictionary
 *   pageHref: (pageNumber: number) => string,
 * }} props
 */
export default async function EventsList({ lang, page, eventType, paid, accessToken, canManage, t, pageHref }) {
  const data = await getEvents({ page, eventType, paid }, accessToken);
  const fetchError = !data;
  const events = data?.content ?? [];
  const totalPages = data?.page?.totalPages ?? 1;

  if (fetchError) {
    return <FetchErrorBanner message={t.loadError} />;
  }

  if (events.length === 0) {
    return (
      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
          <Icon name="inbox" size={48} className="opacity-40" />
          <p className="text-body-lg">{t.noResults}</p>
          {canManage && (
            <CreateCard
              href={`/${lang}/events/new`}
              label={t.createEvent}
              hint={t.createEventHint}
              className="w-80 max-w-full"
            />
          )}
        </div>
      </div>
    );
  }

  return (
    <section
      aria-labelledby="events-heading"
      className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
    >
      <div className="flex items-center gap-sm mb-md">
        <Icon name="event" fill size={20} className="text-secondary" />
        <h2 id="events-heading" className="text-h3 text-primary">{t.sectionTitle}</h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
        {canManage && (
          <CreateCard
            href={`/${lang}/events/new`}
            label={t.createEvent}
            hint={t.createEventHint}
          />
        )}
        {events.map((evt) => {
          const priceLabel = evt.paid
            ? formatPrice(evt.price, evt.currency, lang, t.free)
            : t.free;
          return (
            <CatalogueCard
              key={evt.id}
              href={`/${lang}/events/${evt.id}`}
              imageUrl={evt.photoUrl}
              imageAlt={evt.name}
              fallbackIcon="celebration"
              title={evt.name}
              extras={[
                { icon: 'schedule', value: formatEventDateTime(evt.startsAt, lang) },
                { icon: evt.paid ? 'payments' : 'redeem', value: priceLabel },
              ]}
              viewDetailsLabel={t.viewDetails}
            />
          );
        })}
      </div>

      <SectionPager
        currentPage={page}
        totalPages={totalPages}
        hrefBuilder={pageHref}
        labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }}
        ariaLabel="events pagination"
      />
    </section>
  );
}
