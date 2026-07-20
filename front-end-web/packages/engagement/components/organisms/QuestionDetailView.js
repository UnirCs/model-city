import Icon from '@modelcity/core/components/atoms/Icon';
import VotingZone from '@modelcity/engagement/components/molecules/VotingZone';
import CitizenOnlyNotice from '@modelcity/engagement/components/molecules/CitizenOnlyNotice';
import QuestionStatusActions from '@modelcity/engagement/components/molecules/QuestionStatusActions';
import EditQuestionButton from '@modelcity/engagement/components/molecules/EditQuestionButton';
import AdminSection from '@modelcity/core/components/molecules/AdminSection';

/** Civic consultation validation threshold (votes needed to be considered). */
const THRESHOLD = 10_000;

/**
 * Derives the consultation status from its open/close dates relative to today.
 * @param {string} openDate   – ISO date string e.g. "2026-01-10"
 * @param {string} closeDate  – ISO date string e.g. "2026-03-15"
 * @returns {'active' | 'future' | 'past'}
 */
function deriveStatus(openDate, closeDate) {
  const now = new Date();
  const open = new Date(openDate);
  const close = new Date(closeDate);
  // Move close date to end of day so the last day counts as active
  close.setHours(23, 59, 59, 999);
  if (now < open) return 'future';
  if (now > close) return 'past';
  return 'active';
}

/**
 * Formats a date string using the current locale.
 * @param {string} dateStr
 * @param {string} lang
 */
function formatDate(dateStr, lang) {
  return new Intl.DateTimeFormat(lang, {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(new Date(dateStr));
}

/**
 * Organism: QuestionDetailView
 *
 * Renders the body of a civic consultation detail page: banner with status badge,
 * description with validation-threshold footer, objectives, consultation period,
 * the voting zone (or staff / already-voted notice), vote statistics and the
 * staff administration controls.
 *
 * The back button is intentionally NOT rendered here so the page owns it.
 *
 * @param {{
 *   question: object,          // consultation payload from the engagement service
 *   t: object,                 // participation.questionDetail dictionary
 *   tOtp: object,              // otp dictionary (voting flow)
 *   tCert: object,             // certificate dictionary (voting flow)
 *   lang: string,
 *   staff: boolean,            // user is staff (not a citizen)
 *   canManage: boolean,        // user can manage consultations
 *   accessToken: string,       // Auth0 Bearer JWT for client-side cert verification
 * }} props
 */
export default function QuestionDetailView({ question, t, tOtp, tCert, lang, staff, canManage, accessToken }) {
  /* ── Derived values ─────────────────────────────────────── */
  const status = deriveStatus(question.openDate, question.closeDate);
  const isActive = status === 'active';
  const hasAdminActions = canManage && (status === 'active' || status === 'future');

  const totalVotes = (question.yesVotes ?? 0) + (question.noVotes ?? 0);
  const yesPercent = totalVotes > 0 ? Math.round((question.yesVotes / totalVotes) * 100) : 0;
  const noPercent = totalVotes > 0 ? 100 - yesPercent : 0;

  const thresholdProgress = Math.min(100, Math.round((totalVotes / THRESHOLD) * 100));
  const thresholdMet = totalVotes >= THRESHOLD;

  /* ── Status badge config ────────────────────────────────── */
  const statusConfig = {
    active: {
      label: t.statusActive,
      className: 'bg-secondary text-on-secondary',
      icon: 'campaign',
    },
    future: {
      label: t.statusFuture,
      className: 'bg-tertiary text-on-tertiary',
      icon: 'schedule',
    },
    past: {
      label: t.statusPast,
      className: 'bg-surface-container-high text-on-surface-variant',
      icon: 'history',
    },
  }[status];

  /* ── Date display ───────────────────────────────────────── */
  const openFmt = formatDate(question.openDate, lang);
  const closeFmt = formatDate(question.closeDate, lang);

  /* ── Sorted objectives ──────────────────────────────────── */
  const objectives = [...(question.objectives ?? [])].sort(
    (a, b) => a.sortOrder - b.sortOrder,
  );

  return (
    <div className="grid grid-cols-1 lg:grid-cols-10 gap-lg">

      {/* ── LEFT COLUMN: image, description, objectives ─────── */}
      <div className="lg:col-span-6 space-y-md">

        {/* Banner image with status badge + title overlay */}
        <div className="relative rounded-md overflow-hidden shadow-md h-64 md:h-80">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={question.imageUrl}
            alt={question.title}
            className="w-full h-full object-cover"
          />
          {/* Gradient overlay */}
          <div className="absolute inset-0 bg-linear-to-t from-primary/85 to-transparent" aria-hidden="true" />

          {/* Overlay content */}
          <div className="absolute bottom-md left-md right-md">
            <span
              className={[
                'inline-flex items-center gap-xs px-sm py-xs rounded-full text-caption font-semibold mb-xs',
                statusConfig.className,
              ].join(' ')}
            >
              <Icon name={statusConfig.icon} size={14} />
              {statusConfig.label}
            </span>
            <h1 className="text-h2 text-white leading-tight">{question.title}</h1>
          </div>
        </div>

        {/* Description */}
        <section
          aria-labelledby="detail-description-heading"
          className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
        >
          <h2
            id="detail-description-heading"
            className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant"
          >
            {t.descriptionTitle}
          </h2>
          <p className="text-body-md text-on-surface-variant">{question.description}</p>

          {/* Validation threshold — light, unobtrusive footer */}
          <div className="mt-md flex flex-col gap-xs">
            <div className="flex items-center gap-xs text-on-surface-variant">
              <Icon name="flag" size={16} className="shrink-0" />
              <p className="text-caption">{t.threshold.description}</p>
            </div>
            <div className="flex items-center gap-sm">
              <div className="flex-1 bg-surface-container rounded-full h-1.5 overflow-hidden">
                <div
                  className={[
                    'h-full rounded-full transition-all duration-700',
                    thresholdMet ? 'bg-secondary' : 'bg-outline',
                  ].join(' ')}
                  style={{ width: `${thresholdProgress}%` }}
                  aria-hidden="true"
                />
              </div>
              <span className={[
                'text-caption font-semibold shrink-0',
                thresholdMet ? 'text-secondary' : 'text-on-surface-variant',
              ].join(' ')}>
                {totalVotes.toLocaleString(lang)}&nbsp;/&nbsp;{t.threshold.target}
              </span>
            </div>
          </div>
        </section>

        {/* Objectives */}
        {objectives.length > 0 && (
          <section
            aria-labelledby="detail-objectives-heading"
            className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
          >
            <h2
              id="detail-objectives-heading"
              className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant"
            >
              {t.objectivesTitle}
            </h2>
            <ul className="space-y-sm">
              {objectives.map((obj) => (
                <li key={obj.id} className="flex items-start gap-sm">
                  <Icon name="check_circle" fill size={20} className="text-secondary mt-0.5 shrink-0" />
                  <span className="text-body-md text-on-surface">{obj.objective}</span>
                </li>
              ))}
            </ul>
          </section>
        )}
      </div>

      {/* ── RIGHT COLUMN: voting, stats, threshold ──────────── */}
      <div className="lg:col-span-4 space-y-md">

        {/* Consultation period */}
        <div className="flex items-center gap-xs text-on-surface-variant text-caption bg-surface-container rounded-md px-md py-sm border border-outline-variant">
          <Icon name="date_range" size={16} className="shrink-0" />
          <span>
            {t.openPeriod}&nbsp;
            <strong className="text-on-surface">{openFmt}</strong>
            &nbsp;{t.to}&nbsp;
            <strong className="text-on-surface">{closeFmt}</strong>
          </span>
        </div>

        {/* VotingZone / staff notice / already-voted / not-active */}
        {staff ? (
          <CitizenOnlyNotice t={t.citizenOnly} />
        ) : question.submittable === false ? (
          <div className="rounded-md bg-surface-container border border-outline-variant p-lg flex flex-col items-center gap-sm text-center shadow-sm">
            <Icon name="how_to_vote" fill size={40} className="text-secondary" />
            <p className="text-body-md text-on-surface-variant">{t.vote.alreadyVoted}</p>
          </div>
        ) : (
          <VotingZone t={t.vote} tOtp={tOtp} tCert={tCert} isActive={isActive} questionId={question.id} accessToken={accessToken} />
        )}

        {/* Vote stats */}
        <section
          aria-labelledby="detail-stats-heading"
          className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
        >
          <h2
            id="detail-stats-heading"
            className="text-h3 text-primary mb-md"
          >
            {t.stats.title}
          </h2>

          {/* Total votes */}
          <div className="flex justify-between items-center text-body-md mb-md pb-md border-b border-outline-variant">
            <span className="text-on-surface-variant">{t.stats.total}</span>
            <span className="text-h3 font-bold text-primary">
              {totalVotes.toLocaleString(lang)}
            </span>
          </div>

          {/* Yes votes */}
          <div className="space-y-xs mb-md">
            <div className="flex justify-between text-body-md">
              <span className="flex items-center gap-xs text-secondary font-semibold">
                <Icon name="thumb_up" fill size={16} />
                {t.stats.inFavor}
              </span>
              <span className="font-bold text-primary">
                {(question.yesVotes ?? 0).toLocaleString(lang)}&nbsp;
                <span className="text-on-surface-variant font-normal text-caption">({yesPercent}%)</span>
              </span>
            </div>
            <div className="w-full bg-surface-container rounded-full h-2 overflow-hidden">
              <div
                className="bg-secondary h-full rounded-full transition-all duration-500"
                style={{ width: `${yesPercent}%` }}
                aria-hidden="true"
              />
            </div>
          </div>

          {/* No votes */}
          <div className="space-y-xs">
            <div className="flex justify-between text-body-md">
              <span className="flex items-center gap-xs text-error font-semibold">
                <Icon name="thumb_down" fill size={16} />
                {t.stats.against}
              </span>
              <span className="font-bold text-primary">
                {(question.noVotes ?? 0).toLocaleString(lang)}&nbsp;
                <span className="text-on-surface-variant font-normal text-caption">({noPercent}%)</span>
              </span>
            </div>
            <div className="w-full bg-surface-container rounded-full h-2 overflow-hidden">
              <div
                className="bg-error h-full rounded-full transition-all duration-500"
                style={{ width: `${noPercent}%` }}
                aria-hidden="true"
              />
            </div>
          </div>
        </section>

        {/* Administration — start / close / edit survey */}
        {hasAdminActions && (
          <AdminSection title={t.adminActions.sectionTitle}>
            <QuestionStatusActions
              questionId={question.id}
              status={status}
              t={t.adminActions}
            />
            {status === 'future' && (
              <EditQuestionButton question={question} t={t.editQuestion} />
            )}
          </AdminSection>
        )}

      </div>
    </div>
  );
}
