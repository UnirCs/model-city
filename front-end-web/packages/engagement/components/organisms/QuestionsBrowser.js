import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import CatalogueCard from '@modelcity/core/components/molecules/CatalogueCard';
import CollapsibleHeader from '@modelcity/core/components/molecules/CollapsibleHeader';
import PageHeader from '@modelcity/core/components/molecules/PageHeader';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';

/** Formats a consultation date using the current locale (long form). */
function formatDate(dateStr, lang) {
  return new Intl.DateTimeFormat(lang, { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(dateStr));
}

/**
 * Organism: QuestionsBrowser
 *
 * Renders the civic-consultations browse UI: page header, optional fetch-error
 * banner, the active section (card grid with an optional admin "create" card),
 * collapsible upcoming and finished sections, and an empty state. Each section
 * has independent pagination driven by URL search params.
 *
 * @param {{
 *   lang: string,
 *   t: object,                 // participation.questions dictionary
 *   fetchError: boolean,
 *   canCreate: boolean,
 *   sections: {
 *     active: { questions: object[], currentPage: number, totalPages: number },
 *     future: { questions: object[], currentPage: number, totalPages: number },
 *     past:   { questions: object[], currentPage: number, totalPages: number },
 *   },
 * }} props
 */
export default function QuestionsBrowser({ lang, t, fetchError, canCreate, sections }) {
  const { active, future, past } = sections;
  const paginationBase = `/${lang}/participation/questions`;
  const pagerLabels = { page: t.page, of: t.of, prev: t.prev, next: t.next };

  const createUrl = (section, pageNum) => {
    const params = new URLSearchParams({
      activePage: section === 'active' ? pageNum : active.currentPage,
      futurePage: section === 'future' ? pageNum : future.currentPage,
      pastPage: section === 'past' ? pageNum : past.currentPage,
    });
    return `${paginationBase}?${params.toString()}`;
  };

  return (
    <>
      <PageHeader icon="campaign" title={t.bannerTitle} subtitle={t.bannerSubtitle} />

      {fetchError && <FetchErrorBanner message={t.loadError} className="mb-lg" />}

      {/* ── Active questions (card grid — always visible) ──── */}
      {(active.questions.length > 0 || canCreate) && (
        <section
          className="mb-lg bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
          aria-labelledby="questions-active-heading"
        >
          <div className="flex items-center gap-sm mb-md">
            <Icon name="campaign" fill size={20} className="text-secondary" />
            <h2 id="questions-active-heading" className="text-h3 text-primary">
              {t.sectionActive}
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
            {/* ── Admin create card — always first ── */}
            {canCreate && (
              <Link
                href={`/${lang}/participation/questions/new`}
                className="group rounded-md border-2 border-dashed border-secondary/40 bg-surface-container-lowest hover:border-secondary hover:bg-secondary/5 transition-all flex flex-col items-center justify-center gap-sm py-lg px-md text-center min-h-40"
              >
                <span className="w-12 h-12 rounded-full bg-secondary/10 group-hover:bg-secondary/20 flex items-center justify-center transition-colors">
                  <Icon name="add" size={24} className="text-secondary" />
                </span>
                <span className="text-label-md text-secondary font-semibold">{t.createNew}</span>
                <span className="text-caption text-on-surface-variant">{t.createNewHint}</span>
              </Link>
            )}

            {active.questions.map((question) => (
              <CatalogueCard
                key={question.id}
                href={`/${lang}/participation/questions/${question.id}`}
                imageUrl={question.imageUrl}
                imageAlt={question.title}
                fallbackIcon="campaign"
                title={question.title}
                extras={[{
                  icon: 'event_available',
                  value: `${t.closes}: ${formatDate(question.closeDate, lang)}`,
                }]}
                viewDetailsLabel={t.viewDetails}
              />
            ))}
          </div>

          <SectionPager
            currentPage={active.currentPage}
            totalPages={active.totalPages}
            hrefBuilder={(n) => createUrl('active', n)}
            labels={pagerLabels}
            ariaLabel="active pagination"
          />
        </section>
      )}

      {/* ── Upcoming questions (card grid) ──── */}
      {future.questions.length > 0 && (
        <section
          className="mb-lg bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
          aria-labelledby="questions-future-heading"
        >
          <CollapsibleHeader
            icon="schedule"
            iconClassName="text-tertiary"
            title={t.sectionUpcoming}
            defaultOpen={false}
          >
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
              {future.questions.map((question) => (
                <CatalogueCard
                  key={question.id}
                  href={`/${lang}/participation/questions/${question.id}`}
                  imageUrl={question.imageUrl}
                  imageAlt={question.title}
                  fallbackIcon="schedule"
                  title={question.title}
                  extras={[{
                    icon: 'event_available',
                    value: `${t.opens}: ${formatDate(question.openDate, lang)}`,
                  }]}
                  viewDetailsLabel={t.viewDetails}
                />
              ))}
            </div>

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

      {/* ── Finished questions (card grid) ──── */}
      {past.questions.length > 0 && (
        <section
          className="mb-lg bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md"
          aria-labelledby="questions-past-heading"
        >
          <CollapsibleHeader
            icon="history"
            iconClassName="text-outline"
            title={t.sectionFinished}
            defaultOpen={false}
          >
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
              {past.questions.map((question) => (
                <CatalogueCard
                  key={question.id}
                  href={`/${lang}/participation/questions/${question.id}`}
                  imageUrl={question.imageUrl}
                  imageAlt={question.title}
                  fallbackIcon="history"
                  title={question.title}
                  extras={[{
                    icon: 'event_available',
                    value: `${t.closedOn}: ${formatDate(question.closeDate, lang)}`,
                  }]}
                  viewDetailsLabel={t.viewDetails}
                  grayscale={true}
                />
              ))}
            </div>

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

      {/* ── Empty state ───────────────────────────────────── */}
      {!fetchError && !canCreate && active.questions.length === 0 && future.questions.length === 0 && past.questions.length === 0 && (
        <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
          <div className="py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
            <Icon name="inbox" size={48} className="opacity-40" />
            <p className="text-body-lg">{t.noResults}</p>
          </div>
        </div>
      )}
    </>
  );
}
