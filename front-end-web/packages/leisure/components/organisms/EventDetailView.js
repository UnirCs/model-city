import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import Badge from '@modelcity/core/components/atoms/Badge';
import DeleteEntityButton from '@modelcity/leisure/components/molecules/DeleteEntityButton';
import AdminSection from '@modelcity/core/components/molecules/AdminSection';
import PhotoGallery from '@modelcity/leisure/components/molecules/PhotoGallery';
import BuyTicketButton from '@modelcity/leisure/components/molecules/BuyTicketButton';
import { removeEvent } from '@modelcity/leisure/lib/actions/events';
import {
  eventTypeIcon,
  eventTypeLabel,
  formatEventDateTime,
  formatPrice,
} from '@modelcity/leisure/lib/utils/format';

/**
 * Organism: EventDetailView
 *
 * Renders the body of an event detail page: banner (or title fallback),
 * description and photo gallery, the schedule/place/ticketing aside and the
 * staff administration controls.
 *
 * The back button is intentionally NOT rendered here so the page owns it.
 *
 * @param {{
 *   event: object,                     // event payload from the leisure service
 *   place: object | null,              // bound city place (soft-failed) or null
 *   t: object,                         // leisure.eventDetail dictionary
 *   tAdmin: object,                    // leisure.eventAdmin dictionary
 *   tBuy: object,                      // leisure.buyTicket dictionary
 *   typeLabels: object,                // leisure.eventTypes dictionary
 *   lang: string,
 *   canEdit: boolean,
 *   canDelete: boolean,
 *   canViewTickets: boolean,
 *   canBuy: boolean,
 * }} props
 */
export default function EventDetailView({
  event,
  place,
  t,
  tAdmin,
  tBuy,
  typeLabels,
  lang,
  canEdit,
  canDelete,
  canViewTickets,
  canBuy,
}) {
  const photos = event.photoUrls ?? [];
  const bannerImage = photos[0];
  const otherPhotos = photos.slice(1);

  const now = new Date();
  const eventEndsMs = event.endsAt ? new Date(event.endsAt).getTime() : 0;
  const isPast = eventEndsMs > 0 && eventEndsMs < now.getTime();
  const priceLabel = formatPrice(event.price, event.currency, lang, tBuy.free);

  return (
    <>
      {bannerImage ? (
        <section className="relative rounded-md overflow-hidden shadow-md h-64 md:h-80">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={bannerImage} alt={event.name} className="w-full h-full object-cover" />
          <div
            className="absolute inset-0 bg-linear-to-t from-primary/85 to-transparent"
            aria-hidden="true"
          />
          <div className="absolute bottom-md left-md right-md flex flex-wrap items-end justify-between gap-sm">
            <h1 className="text-h2 text-white leading-tight">{event.name}</h1>
            <span className="inline-flex items-center gap-xs px-sm py-xs rounded-full bg-surface/90 text-on-surface text-caption font-semibold">
              <Icon name={eventTypeIcon(event.eventType)} size={14} className="text-secondary" />
              {eventTypeLabel(event.eventType, typeLabels)}
            </span>
          </div>
        </section>
      ) : (
        <header className="flex flex-wrap items-center gap-sm">
          <h1 className="text-h2 text-primary leading-tight">{event.name}</h1>
          <Badge label={eventTypeLabel(event.eventType, typeLabels)} status="pending" />
        </header>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-10 gap-lg">
        {/* LEFT — description, schedule, gallery */}
        <div className="lg:col-span-6 space-y-md">
          <section
            aria-labelledby="event-description-heading"
            className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
          >
            <h2
              id="event-description-heading"
              className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant"
            >
              {t.descriptionTitle}
            </h2>
            <p className="text-body-md text-on-surface-variant whitespace-pre-line">
              {event.description}
            </p>
          </section>

          {otherPhotos.length > 0 && (
            <section
              aria-labelledby="event-gallery-heading"
              className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
            >
              <h2
                id="event-gallery-heading"
                className="text-h3 text-primary mb-md pb-xs border-b border-outline-variant flex items-center gap-sm"
              >
                <Icon name="photo_library" size={20} className="text-secondary" />
                {t.gallery}
              </h2>
              <PhotoGallery
                photos={otherPhotos}
                alt={event.name}
                closeLabel={t.galleryClose}
                prevLabel={t.galleryPrev}
                nextLabel={t.galleryNext}
              />
            </section>
          )}
        </div>

        {/* RIGHT — schedule + ticketing */}
        <aside className="lg:col-span-4 space-y-md">
          <section className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm">
            <h2 className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant flex items-center gap-sm">
              <Icon name="schedule" fill size={20} className="text-secondary" />
              {t.scheduleTitle}
            </h2>
            <dl className="grid grid-cols-[auto_1fr] gap-x-md gap-y-xs text-body-md">
              <dt className="text-on-surface-variant">{t.startsAt}</dt>
              <dd className="text-on-surface">{formatEventDateTime(event.startsAt, lang)}</dd>
              <dt className="text-on-surface-variant">{t.endsAt}</dt>
              <dd className="text-on-surface">{formatEventDateTime(event.endsAt, lang)}</dd>
              {event.capacity != null && (
                <>
                  <dt className="text-on-surface-variant">{t.capacity}</dt>
                  <dd className="text-on-surface">{event.capacity}</dd>
                </>
              )}
            </dl>
          </section>

          {place && (
            <section className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm">
              <h2 className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant flex items-center gap-sm">
                <Icon name="location_on" fill size={20} className="text-secondary" />
                {t.placeTitle}
              </h2>
              <p className="text-body-md text-on-surface mb-sm">{place.name}</p>
              {place.address && (
                <p className="text-body-sm text-on-surface-variant mb-sm">{place.address}</p>
              )}
              <Link
                href={`/${lang}/tourism/locations/${place.id}`}
                className="inline-flex items-center justify-center gap-sm px-md py-sm rounded-md bg-secondary text-on-secondary hover:opacity-90 transition-all shadow-sm font-semibold text-label-md"
              >
                <Icon name="info" size={18} />
                {t.viewPlace}
              </Link>
            </section>
          )}

          <section className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm">
            <h2 className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant flex items-center gap-sm">
              <Icon name="confirmation_number" fill size={20} className="text-secondary" />
              {t.ticketsTitle}
            </h2>

            <div className="flex items-center justify-between mb-md">
              <span className="text-body-md text-on-surface-variant">{t.price}</span>
              <span className="text-h3 text-primary font-bold tabular-nums">
                {event.paid ? priceLabel : tBuy.free}
              </span>
            </div>

            {!event.requiresTicket ? (
              <p className="flex items-center gap-xs text-body-sm text-on-surface-variant">
                <Icon name="info" size={16} />
                {t.noTicketRequired}
              </p>
            ) : isPast ? (
              <p className="flex items-center gap-xs text-body-sm text-on-surface-variant">
                <Icon name="event_busy" size={16} />
                {t.alreadyEnded}
              </p>
            ) : event.acquired ? (
              <p className="flex items-center gap-xs text-body-sm text-tertiary">
                <Icon name="check_circle" size={16} />
                {t.alreadyAcquired}
              </p>
            ) : canBuy ? (
              <BuyTicketButton
                eventId={event.id}
                paid={event.paid}
                disabled={isPast}
                lang={lang}
                labels={tBuy}
              />
            ) : (
              <p className="flex items-center gap-xs text-body-sm text-on-surface-variant">
                <Icon name="info" size={16} />
                {t.staffCannotBuy}
              </p>
            )}
          </section>
          {(canEdit || canDelete || canViewTickets) && (
            <AdminSection title={tAdmin.sectionTitle}>
              {canViewTickets && (
                <Link
                  href={`/${lang}/events/${event.id}/tickets`}
                  className="inline-flex items-center justify-center gap-sm px-md py-sm rounded-md border border-outline-variant text-on-surface bg-surface text-label-md font-semibold hover:bg-surface-container transition-all active:scale-95 w-full"
                >
                  <Icon name="receipt_long" size={18} />
                  {tAdmin.viewTickets}
                </Link>
              )}
              {canEdit && (
                <Link
                  href={`/${lang}/events/${event.id}/edit`}
                  className="inline-flex items-center justify-center gap-sm px-md py-sm rounded-md border border-primary text-primary bg-surface text-label-md font-semibold hover:bg-primary/5 transition-all active:scale-95 w-full"
                >
                  <Icon name="edit" size={18} />
                  {tAdmin.editEvent}
                </Link>
              )}
              {canDelete && (
                <DeleteEntityButton
                  id={event.id}
                  action={removeEvent}
                  onSuccessHref={`/${lang}/events`}
                  className="w-full"
                  labels={{
                    button:       tAdmin.deleteEvent,
                    confirmTitle: tAdmin.deleteConfirmTitle,
                    confirmBody:  tAdmin.deleteConfirmBody,
                    confirm:      tAdmin.confirm,
                    cancel:       tAdmin.cancel,
                    deleting:     tAdmin.deleting,
                    error:        tAdmin.deleteError,
                  }}
                />
              )}
            </AdminSection>
          )}
        </aside>
      </div>
    </>
  );
}
