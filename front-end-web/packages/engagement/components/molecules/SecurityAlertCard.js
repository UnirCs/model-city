'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import Modal from '@modelcity/core/components/molecules/Modal';
import { severityVisual } from '@modelcity/engagement/lib/security/severity';

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
 * Molecule: SecurityAlertCard
 *
 * Compact static card. Title shown in full, wrapping as needed.
 * Clicking opens a detail modal.
 */
export default function SecurityAlertCard({
  alert,
  zoneName,
  neighbourhoodName,
  t,
  tSeverity,
  lang,
}) {
  const [open, setOpen] = useState(false);

  const visual = severityVisual(alert.severity);
  const severityLabel = tSeverity[alert.severity] ?? alert.severity;
  const areaText = neighbourhoodName
    ? `${zoneName ?? t.unknownZone} · ${neighbourhoodName}`
    : (zoneName ?? t.unknownZone);

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        className={`w-full text-left bg-surface-container-lowest rounded-xl border-2 shadow-sm py-sm px-md flex items-start gap-sm cursor-pointer hover:opacity-90 transition-opacity ${visual.borderClass}`}
      >
        <div className={`p-xs rounded-full shrink-0 aspect-square flex items-center justify-center ${visual.bubbleClass}`}>
          <Icon name={visual.icon} size={18} />
        </div>

        <div className="flex-1 min-w-0">
          <p className="text-label-md text-on-surface font-semibold leading-snug truncate">
            {alert.title}
          </p>

          <div className="flex items-center gap-sm mt-xs text-caption text-on-surface-variant overflow-hidden">
            <span className="flex items-center gap-xs min-w-0 shrink truncate">
              <Icon name="location_on" size={12} className="shrink-0" />
              <span className="truncate">{areaText}</span>
            </span>
            <span className="flex items-center gap-xs shrink-0 whitespace-nowrap">
              <Icon name="schedule" size={12} />
              {formatDateTime(alert.createdAt, lang)}
            </span>
          </div>
        </div>

        <span className={`shrink-0 self-start px-xs py-xs rounded-full text-caption font-bold uppercase tracking-wide ${visual.chipClass}`}>
          {severityLabel}
        </span>
      </button>

      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title={severityLabel}
        closeLabel={t.closeModal ?? 'Cerrar'}
      >
        <div className="flex flex-col gap-md">
          <span className={`self-start inline-flex items-center gap-xs px-sm py-xs rounded-full text-caption font-bold uppercase tracking-wide ${visual.chipClass}`}>
            <Icon name={visual.icon} size={14} />
            {severityLabel}
          </span>
          <h3 className="text-h3 text-primary font-semibold leading-snug [overflow-wrap:anywhere]">
            {alert.title}
          </h3>
          <p className="text-body-md text-on-surface [overflow-wrap:anywhere] whitespace-pre-wrap">
            {alert.description}
          </p>

          <dl className="grid grid-cols-1 sm:grid-cols-2 gap-sm text-body-sm border-t border-outline-variant pt-md">
            <div>
              <dt className="text-label-md text-on-surface font-medium">{t.zoneLabel}</dt>
              <dd className="text-on-surface-variant">{zoneName ?? t.unknownZone}</dd>
            </div>
            <div>
              <dt className="text-label-md text-on-surface font-medium">{t.neighbourhoodLabel}</dt>
              <dd className="text-on-surface-variant">{neighbourhoodName ?? t.wholeZone}</dd>
            </div>
            <div className="sm:col-span-2">
              <dt className="text-label-md text-on-surface font-medium">{t.createdAtLabel}</dt>
              <dd className="text-on-surface-variant">{formatDateTime(alert.createdAt, lang)}</dd>
            </div>
          </dl>
        </div>
      </Modal>
    </>
  );
}
