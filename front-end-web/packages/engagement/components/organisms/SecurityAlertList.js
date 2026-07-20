'use client';

import SecurityAlertCard from '@modelcity/engagement/components/molecules/SecurityAlertCard';
import Icon from '@modelcity/core/components/atoms/Icon';
import Link from 'next/link';

/**
 * Organism: SecurityAlertList
 *
 * Renders the grid of security alert cards. Each card manages its own
 * detail-modal state independently. The create alert card is shown first
 * if the user has permission.
 *
 * @param {{
 *   alerts: Array<object>,
 *   areas:  Record<number, { zoneName: string | null, neighbourhoodName: string | null }>,
 *   t:      object,
 *   tSeverity: Record<string, string>,
 *   lang:   string,
 *   canCreate: boolean,
 * }} props
 */
export default function SecurityAlertList({ alerts, areas, t, tSeverity, lang, canCreate }) {
  return (
    <div className="grid grid-cols-1 gap-md items-start">
      {/* ── Admin create card — always first ── */}
      {canCreate && (
        <Link
          href={`/${lang}/security/alerts/new`}
          className="group rounded-md border-2 border-dashed border-error/40 bg-surface-container-lowest hover:border-error hover:bg-error/5 transition-all flex items-center gap-sm py-sm px-md"
        >
          <span className="p-xs rounded-full shrink-0 aspect-square flex items-center justify-center bg-error/10 group-hover:bg-error/20 transition-colors">
            <Icon name="add_alert" size={18} className="text-error" />
          </span>
          <div className="flex-1 min-w-0">
            <p className="text-label-md text-error font-semibold leading-snug">{t.createNew}</p>
            <p className="text-caption text-on-surface-variant mt-xs">{t.createNewHint}</p>
          </div>
        </Link>
      )}

      {alerts.map((alert) => (
        <SecurityAlertCard
          key={alert.id}
          alert={alert}
          zoneName={areas[alert.id]?.zoneName ?? null}
          neighbourhoodName={areas[alert.id]?.neighbourhoodName ?? null}
          t={t}
          tSeverity={tSeverity}
          lang={lang}
        />
      ))}
    </div>
  );
}



