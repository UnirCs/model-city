import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import Icon from '@modelcity/core/components/atoms/Icon';
import SecurityAlertsManageTable from '@modelcity/engagement/components/organisms/SecurityAlertsManageTable';

/**
 * Organism: SecurityAlertsManageView
 *
 * Management body for security alerts: an empty state when there are none, or
 * the summary table (with click-through detail modal and per-row delete) plus
 * pagination.
 *
 * @param {{
 *   lang: string,
 *   t: object,                 // security.manage dictionary
 *   tSeverity: object,         // security.severity dictionary
 *   tCenter: object,           // security.alertCenter dictionary (pagination labels)
 *   alerts: object[],
 *   areas: Record<string, { zoneName: string, neighbourhoodName: string }>,
 *   page: number,
 *   totalPages: number,
 * }} props
 */
export default function SecurityAlertsManageView({ lang, t, tSeverity, tCenter, alerts, areas, page, totalPages }) {
  if (alerts.length === 0) {
    return (
      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
          <Icon name="inbox" size={48} className="opacity-40" />
          <p className="text-body-lg">{t.noResults}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
      <div className="flex items-center gap-sm mb-md">
        <Icon name="format_list_bulleted" size={20} className="text-secondary" />
        <h2 className="text-h3 text-primary">{t.tableTitle}</h2>
      </div>
      <SecurityAlertsManageTable
        alerts={alerts}
        areas={areas}
        t={t}
        tSeverity={tSeverity}
        tCenter={tCenter}
        lang={lang}
      />

      {/* ── Pagination ─────────────────────────────────────── */}
      {totalPages > 1 && (
        <nav
          className="grid grid-cols-3 items-center gap-sm mt-md"
          aria-label="alerts pagination"
        >
          <div className="flex justify-start">
            {page === 0 ? (
              <span className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-outline opacity-50 text-label-md cursor-not-allowed select-none">
                <Icon name="chevron_left" size={18} />
                <span className="hidden sm:inline">{tCenter.prev}</span>
              </span>
            ) : (
              <LocalizedLink
                href={`/security/alerts/manage?page=${page - 1}`}
                className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md"
              >
                <Icon name="chevron_left" size={18} />
                <span className="hidden sm:inline">{tCenter.prev}</span>
              </LocalizedLink>
            )}
          </div>
          <span className="text-body-md text-on-surface-variant text-center">
            {tCenter.page} {page + 1} {tCenter.of} {totalPages}
          </span>
          <div className="flex justify-end">
            {page >= totalPages - 1 ? (
              <span className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-outline opacity-50 text-label-md cursor-not-allowed select-none">
                <span className="hidden sm:inline">{tCenter.next}</span>
                <Icon name="chevron_right" size={18} />
              </span>
            ) : (
              <LocalizedLink
                href={`/security/alerts/manage?page=${page + 1}`}
                className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md"
              >
                <span className="hidden sm:inline">{tCenter.next}</span>
                <Icon name="chevron_right" size={18} />
              </LocalizedLink>
            )}
          </div>
        </nav>
      )}
    </div>
  );
}
