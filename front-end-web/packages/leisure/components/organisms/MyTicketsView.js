'use client';

import { useState } from 'react';
import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import CatalogueCard from '@modelcity/core/components/molecules/CatalogueCard';
import CollapsibleHeader from '@modelcity/core/components/molecules/CollapsibleHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import IconPageHeader from '@modelcity/core/components/molecules/IconPageHeader';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';
import TicketQrModal from '@modelcity/leisure/components/molecules/TicketQrModal';
import { formatEventDateTime, formatPrice } from '@modelcity/leisure/lib/utils/format';

/** Card grid for one ticket section. */
function TicketGrid({ tickets, lang, t, tBuy, fallbackIcon, grayscale = false, onQrClick }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
      {tickets.map((ticket) => (
        <CatalogueCard
          key={ticket.id}
          href={`/${lang}/events/${ticket.eventId}`}
          imageUrl={ticket.eventPhoto}
          imageAlt={ticket.eventName ?? ''}
          fallbackIcon={fallbackIcon}
          title={ticket.eventName ?? `${t.colTicket} #${ticket.id}`}
          extras={[
            ...(ticket.eventStart
              ? [{ icon: 'schedule', value: formatEventDateTime(ticket.eventStart, lang) }]
              : []),
            {
              icon: 'confirmation_number',
              value: formatPrice(ticket.pricePaid, ticket.currency, lang, tBuy.free),
            },
          ]}
          viewDetailsLabel={t.viewEvent}
          grayscale={grayscale}
          secondaryLabel={onQrClick ? t.qrButton : undefined}
          onSecondaryAction={onQrClick ? () => onQrClick(ticket) : undefined}
        />
      ))}
    </div>
  );
}

/**
 * Organism: MyTicketsView
 *
 * Renders the citizen's tickets: a header, optional fetch-error banner, the
 * always-visible "this week" section, collapsible upcoming and past sections
 * (each independently paginated via URL search params), and an empty state.
 *
 * The back button is intentionally NOT rendered here so the page owns it.
 *
 * @param {{
 *   lang: string,
 *   t: object,                 // leisure.myTickets dictionary
 *   tBuy: object,              // leisure.buyTicket dictionary
 *   fetchError: boolean,
 *   isEmpty: boolean,
 *   sections: {
 *     week:   { tickets: object[], currentPage: number, totalPages: number },
 *     future: { tickets: object[], currentPage: number, totalPages: number },
 *     past:   { tickets: object[], currentPage: number, totalPages: number },
 *   },
 * }} props
 */
export default function MyTicketsView({ lang, t, tBuy, fetchError, isEmpty, sections }) {
  const { week, future, past } = sections;
  const paginationBase = `/${lang}/events/my-tickets`;
  const pagerLabels = { page: t.page, of: t.of, prev: t.prev, next: t.next };

  const [activeTicket, setActiveTicket] = useState(null);
  const [qrOpen, setQrOpen] = useState(false);

  function handleOpenQr(ticket) {
    setActiveTicket(ticket);
    setQrOpen(true);
  }

  function handleCloseQr() {
    setQrOpen(false);
    setActiveTicket(null);
  }

  const createUrl = (section, pageNum) => {
    const qs = new URLSearchParams({
      weekPage: String(section === 'week' ? pageNum : week.currentPage),
      futurePage: String(section === 'future' ? pageNum : future.currentPage),
      pastPage: String(section === 'past' ? pageNum : past.currentPage),
    });
    return `${paginationBase}?${qs.toString()}`;
  };

  return (
    <>
      <IconPageHeader icon="confirmation_number" title={t.pageTitle} subtitle={t.pageSubtitle} />

      {fetchError && <FetchErrorBanner message={t.loadError} />}

      {/* ── This week (always visible) ──────────────────── */}
      {week.tickets.length > 0 && (
        <section
          aria-labelledby="tickets-week-heading"
          className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
        >
          <div className="flex items-center gap-sm mb-md">
            <Icon name="local_activity" fill size={20} className="text-secondary" />
            <h2 id="tickets-week-heading" className="text-h3 text-primary">{t.sectionThisWeek}</h2>
          </div>
          <TicketGrid tickets={week.tickets} lang={lang} t={t} tBuy={tBuy} fallbackIcon="local_activity" onQrClick={handleOpenQr} />
          <SectionPager
            currentPage={week.currentPage}
            totalPages={week.totalPages}
            hrefBuilder={(n) => createUrl('week', n)}
            labels={pagerLabels}
            ariaLabel="week pagination"
          />
        </section>
      )}

      {/* ── Upcoming events (collapsible) ───────────────── */}
      {future.tickets.length > 0 && (
        <section
          aria-labelledby="tickets-future-heading"
          className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
        >
          <CollapsibleHeader
            icon="schedule"
            iconClassName="text-tertiary"
            title={t.sectionUpcoming}
            defaultOpen={true}
          >
            <TicketGrid tickets={future.tickets} lang={lang} t={t} tBuy={tBuy} fallbackIcon="schedule" onQrClick={handleOpenQr} />
            <SectionPager
              currentPage={future.currentPage}
              totalPages={future.totalPages}
              hrefBuilder={(n) => createUrl('future', n)}
              labels={pagerLabels}
              ariaLabel="future pagination"
            />
          </CollapsibleHeader>
        </section>
      )}

      {/* ── Past events (collapsible, greyed) ───────────── */}
      {past.tickets.length > 0 && (
        <section
          aria-labelledby="tickets-past-heading"
          className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
        >
          <CollapsibleHeader
            icon="history"
            iconClassName="text-outline"
            title={t.sectionPast}
            defaultOpen={false}
          >
            <TicketGrid tickets={past.tickets} lang={lang} t={t} tBuy={tBuy} fallbackIcon="history" grayscale={true} />
            <SectionPager
              currentPage={past.currentPage}
              totalPages={past.totalPages}
              hrefBuilder={(n) => createUrl('past', n)}
              labels={pagerLabels}
              ariaLabel="past pagination"
            />
          </CollapsibleHeader>
        </section>
      )}

      {/* ── Empty state ─────────────────────────────────── */}
      {isEmpty && (
        <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
          <Icon name="inbox" size={48} className="opacity-40" />
          <p className="text-body-lg">{t.empty}</p>
          <Link
            href={`/${lang}/events`}
            className="inline-flex items-center gap-sm px-md py-sm rounded-md bg-primary text-on-primary text-label-md font-semibold hover:opacity-90 transition-all"
          >
            <Icon name="event" size={18} />
            {t.exploreEvents}
          </Link>
        </div>
      )}

      <TicketQrModal
        open={qrOpen}
        ticket={activeTicket}
        labels={t}
        onClose={handleCloseQr}
      />
    </>
  );
}
