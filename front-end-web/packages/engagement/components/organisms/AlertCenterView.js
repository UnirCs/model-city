import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import Icon from '@modelcity/core/components/atoms/Icon';
import AlertsMapClient from '@modelcity/engagement/components/molecules/AlertsMapClient';
import SecurityAlertList from '@modelcity/engagement/components/organisms/SecurityAlertList';
import { getZoneName, getNeighbourhoodName } from '@modelcity/engagement/lib/security/geo';

/**
 * Organism: AlertCenterView
 *
 * Two-column alert centre body: the live alerts map on the left and the
 * paginated alerts list (with empty state) on the right.
 *
 * @param {{
 *   lang: string,
 *   t: object,                 // security.alertCenter dictionary
 *   tSeverity: object,         // security.severity dictionary
 *   canCreate: boolean,
 *   alerts: object[],
 *   page: number,
 *   totalPages: number,
 * }} props
 */
export default function AlertCenterView({ lang, t, tSeverity, canCreate, alerts, page, totalPages }) {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-xl items-start">
      {/* ── Map (left column) ─────────────────────────────────── */}
      <section aria-label={t.mapTitle} className="lg:col-span-1">
        <AlertsMapClient
          alerts={alerts}
          mapTitle={t.mapTitle}
          expandLabel={t.expandMap}
          closeLabel={t.closeMap}
        />
      </section>

      {/* ── Alerts list (right column) ─────────────────────────── */}
      <section className="lg:col-span-1 bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <div className="flex items-center gap-sm mb-md">
          <Icon name="notifications_active" fill size={20} className="text-secondary" />
          <h2 className="text-h3 text-primary">{t.listTitle}</h2>
        </div>
        {(alerts.length > 0 || canCreate) && (
          <>
            <SecurityAlertList
              alerts={alerts}
              areas={Object.fromEntries(
                alerts.map((a) => [a.id, {
                  zoneName: getZoneName(a.zoneId),
                  neighbourhoodName: getNeighbourhoodName(a.neighbourhoodId),
                }]),
              )}
              t={t}
              tSeverity={tSeverity}
              lang={lang}
              canCreate={canCreate}
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
                      <span className="hidden sm:inline">{t.prev}</span>
                    </span>
                  ) : (
                    <LocalizedLink
                      href={`/security/alerts?page=${page - 1}`}
                      className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md"
                    >
                      <Icon name="chevron_left" size={18} />
                      <span className="hidden sm:inline">{t.prev}</span>
                    </LocalizedLink>
                  )}
                </div>
                <span className="text-body-md text-on-surface-variant text-center">
                  {t.page} {page + 1} {t.of} {totalPages}
                </span>
                <div className="flex justify-end">
                  {page >= totalPages - 1 ? (
                    <span className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-outline opacity-50 text-label-md cursor-not-allowed select-none">
                      <span className="hidden sm:inline">{t.next}</span>
                      <Icon name="chevron_right" size={18} />
                    </span>
                  ) : (
                    <LocalizedLink
                      href={`/security/alerts?page=${page + 1}`}
                      className="flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md"
                    >
                      <span className="hidden sm:inline">{t.next}</span>
                      <Icon name="chevron_right" size={18} />
                    </LocalizedLink>
                  )}
                </div>
              </nav>
            )}
          </>
        )}

        {/* ── Empty state ─────────────────────────────────────── */}
        {alerts.length === 0 && !canCreate && (
          <div className="py-xl text-center text-on-surface-variant flex flex-col items-center gap-md">
            <Icon name="check_circle" size={48} className="opacity-40" />
            <p className="text-body-lg">{t.noResults}</p>
          </div>
        )}
      </section>
    </div>
  );
}
