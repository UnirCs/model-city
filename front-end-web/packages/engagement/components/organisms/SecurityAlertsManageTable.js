'use client';

import { useState, useTransition } from 'react';
import { useRouter } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Modal from '@modelcity/core/components/molecules/Modal';
import Button from '@modelcity/core/components/atoms/Button';
import { severityVisual } from '@modelcity/engagement/lib/security/severity';
import { removeSecurityAlert } from '@modelcity/engagement/lib/actions/securityAlerts';

/**
 * Formats an ISO date-time using the user's locale.
 */
function formatDateTime(iso, lang) {
  try {
    return new Intl.DateTimeFormat(lang, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

/**
 * Organism: SecurityAlertsManageTable
 *
 * Client island that renders the alerts table for backoffice / admin users.
 * Each row opens a detail modal on click. A separate "delete" button per row
 * fires the destructive Server Action behind a confirmation modal.
 *
 * @param {{
 *   alerts: Array<object>,
 *   areas:  Record<number, { zoneName: string | null, neighbourhoodName: string | null }>,
 *   t:      object,
 *   tSeverity: Record<string, string>,
 *   tCenter:   object,
 *   lang:   string,
 * }} props
 */
export default function SecurityAlertsManageTable({
  alerts,
  areas,
  t,
  tSeverity,
  tCenter,
  lang,
}) {
  const router = useRouter();
  const [detail, setDetail]       = useState(null);
  const [toDelete, setToDelete]   = useState(null);
  const [error, setError]         = useState('');
  const [pending, startTransition] = useTransition();

  function confirmDelete() {
    if (!toDelete) return;
    setError('');
    startTransition(async () => {
      const result = await removeSecurityAlert(toDelete.id);
      if (result?.error) {
        setError(t.deleteError);
        return;
      }
      setToDelete(null);
      router.refresh();
    });
  }

  return (
    <>
      <div className="rounded-md border border-outline-variant overflow-hidden">
        <table className="w-full text-left border-collapse table-fixed">
            <thead>
              <tr className="bg-surface-container text-on-surface-variant text-label-md border-b border-outline-variant">
                <th className="p-md font-semibold w-24">{t.colId}</th>
                <th className="p-md font-semibold w-38">{t.colSeverity}</th>
                <th className="p-md font-semibold">{t.colTitle}</th>
                <th className="p-md font-semibold text-right w-30">{t.colActions}</th>
              </tr>
            </thead>
            <tbody className="text-body-md divide-y divide-outline-variant">
              {alerts.map((alert) => {
                const visual = severityVisual(alert.severity);
                const area = areas[alert.id];
                const areaText = area?.neighbourhoodName
                  ? `${area.zoneName ?? tCenter.unknownZone} · ${area.neighbourhoodName}`
                  : (area?.zoneName ?? tCenter.unknownZone);
                return (
                  <tr
                    key={alert.id}
                    onClick={() => setDetail(alert)}
                    className="hover:bg-surface-container-low transition-colors cursor-pointer"
                  >
                    <td className="p-md text-on-surface-variant tabular-nums align-top">#{alert.id}</td>
                    <td className="p-md align-top">
                      <span className={`inline-flex items-center gap-xs px-sm py-xs rounded-full text-caption font-bold uppercase tracking-wide ${visual.chipClass}`}>
                        <Icon name={visual.icon} size={14} />
                        {tSeverity[alert.severity] ?? alert.severity}
                      </span>
                    </td>
                    <td className="p-md">
                      {/* Top sub-row: title */}
                      <p className="font-medium text-on-surface truncate">{alert.title}</p>
                      {/* Bottom sub-row: area + expiry */}
                      <div className="flex flex-wrap items-center gap-md mt-xs text-caption text-on-surface-variant">
                        <span className="flex items-center gap-xs">
                          <Icon name="location_on" size={12} />
                          {areaText}
                        </span>
                        <span className="flex items-center gap-xs">
                          <Icon name="event" size={12} />
                          {t.colExpires}: {formatDateTime(alert.expiresAt, lang)}
                        </span>
                      </div>
                    </td>
                    <td className="p-md text-right whitespace-nowrap align-top">
                      <button
                        type="button"
                        onClick={(e) => { e.stopPropagation(); setDetail(alert); }}
                        className="p-xs text-on-surface-variant hover:text-secondary transition-colors"
                        aria-label={t.viewDetails}
                      >
                        <Icon name="visibility" size={20} />
                      </button>
                      <button
                        type="button"
                        onClick={(e) => { e.stopPropagation(); setError(''); setToDelete(alert); }}
                        className="p-xs text-on-surface-variant hover:text-error transition-colors ml-xs"
                        aria-label={t.deleteAlert}
                      >
                        <Icon name="delete" size={20} />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
      </div>

      {/* ── Detail modal ──────────────────────────────────────── */}
      <Modal
        open={!!detail}
        onClose={() => setDetail(null)}
        title={t.modalTitle}
        closeLabel={t.modalClose}
      >
        {detail && (
          <div className="flex flex-col gap-md text-body-md">
            <div className="flex items-center gap-sm">
              <span className={`inline-flex items-center gap-xs px-sm py-xs rounded-full text-caption font-bold uppercase tracking-wide ${severityVisual(detail.severity).chipClass}`}>
                <Icon name={severityVisual(detail.severity).icon} size={14} />
                {tSeverity[detail.severity] ?? detail.severity}
              </span>
              <span className="text-on-surface-variant text-caption">#{detail.id}</span>
            </div>

            <h3 className="text-h3 text-primary font-semibold leading-snug break-words">
              {detail.title}
            </h3>

            <p className="text-on-surface break-words whitespace-pre-wrap">{detail.description}</p>

            <dl className="grid grid-cols-1 sm:grid-cols-2 gap-sm text-body-sm">
              <div>
                <dt className="text-label-md text-on-surface font-medium">{t.modalZone}</dt>
                <dd className="text-on-surface-variant">
                  {areas[detail.id]?.zoneName ?? tCenter.unknownZone}
                </dd>
              </div>
              <div>
                <dt className="text-label-md text-on-surface font-medium">{t.modalNeighbourhood}</dt>
                <dd className="text-on-surface-variant">
                  {areas[detail.id]?.neighbourhoodName ?? tCenter.wholeZone}
                </dd>
              </div>
              <div>
                <dt className="text-label-md text-on-surface font-medium">{t.modalCoordinates}</dt>
                <dd className="text-on-surface-variant tabular-nums">
                  {detail.latitude.toFixed(6)}, {detail.longitude.toFixed(6)}
                </dd>
              </div>
              <div>
                <dt className="text-label-md text-on-surface font-medium">{t.modalCreatedAt}</dt>
                <dd className="text-on-surface-variant">{formatDateTime(detail.createdAt, lang)}</dd>
              </div>
              <div className="sm:col-span-2">
                <dt className="text-label-md text-on-surface font-medium">{t.modalExpiresAt}</dt>
                <dd className="text-on-surface-variant">{formatDateTime(detail.expiresAt, lang)}</dd>
              </div>
            </dl>
          </div>
        )}
      </Modal>

      {/* ── Delete confirmation modal ─────────────────────────── */}
      <Modal
        open={!!toDelete}
        onClose={() => (pending ? null : setToDelete(null))}
        title={t.deleteConfirmTitle}
        closeLabel={t.deleteCancel}
      >
        <div className="flex flex-col gap-md">
          <p className="text-body-md text-on-surface-variant">{t.deleteConfirmBody}</p>

          {error && (
            <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
              <Icon name="error" size={18} className="shrink-0" />
              {error}
            </div>
          )}

          <div className="flex items-center justify-end gap-md">
            <Button
              type="button"
              variant="outline-error"
              onClick={() => setToDelete(null)}
              disabled={pending}
            >
              {t.deleteCancel}
            </Button>
            <Button
              type="button"
              variant="error"
              onClick={confirmDelete}
              disabled={pending}
            >
              {pending ? t.deleting : t.deleteConfirm}
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}










