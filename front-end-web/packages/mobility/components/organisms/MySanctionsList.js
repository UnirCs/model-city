'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import SanctionDetailModal from '@modelcity/mobility/components/molecules/SanctionDetailModal';
import { formatDateTime } from '@modelcity/mobility/lib/utils/format';
import { fetchMySanctionDetail } from '@modelcity/mobility/lib/actions/mobility';

/**
 * Organism: MySanctionsList
 *
 * Citizen-facing list of received sanctions. Tapping a row opens a modal
 * that lazily fetches the base64 evidence image through a Server Action.
 *
 * @param {{
 *   sanctions: Array<{
 *     id: number,
 *     licensePlate: string,
 *     latitude: number,
 *     longitude: number,
 *     agentSub?: string,
 *     createdAt: string,
 *   }>,
 *   labels: object,
 *   detailLabels: object,
 *   lang: string,
 * }} props
 */
export default function MySanctionsList({ sanctions, labels, detailLabels, lang }) {
  const [activeId, setActiveId] = useState(null);

  if (sanctions.length === 0) {
    return (
      <div className="py-2xl text-center text-on-surface-variant flex flex-col items-center gap-md">
        <Icon name="verified" size={48} className="opacity-40" />
        <p className="text-body-lg">{labels.empty}</p>
      </div>
    );
  }

  return (
    <>
      <ul className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
        {sanctions.map((s) => (
          <li key={s.id}>
            <article className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md flex flex-col gap-sm h-full">
              <header className="flex items-start gap-sm">
                <div className="w-10 h-10 rounded-md bg-error-container text-on-error-container flex items-center justify-center shrink-0">
                  <Icon name="gavel" fill size={20} />
                </div>
                <div className="min-w-0">
                  <h3 className="text-h3 text-primary tracking-wide truncate">{s.licensePlate}</h3>
                  <p className="text-caption text-on-surface-variant">
                    {labels.ref}: #{s.id}
                  </p>
                </div>
              </header>

              <dl className="text-caption flex flex-col gap-xs">
                <div className="flex items-center gap-xs text-on-surface-variant">
                  <Icon name="schedule" size={14} />
                  <span>{labels.createdAt}:</span>
                  <span className="tabular-nums text-on-surface">
                    {formatDateTime(s.createdAt, lang)}
                  </span>
                </div>
                <div className="flex items-center gap-xs text-on-surface-variant">
                  <Icon name="location_on" size={14} />
                  <span>{labels.location}:</span>
                  <span className="tabular-nums text-on-surface">
                    {s.latitude?.toFixed(4)}, {s.longitude?.toFixed(4)}
                  </span>
                </div>
              </dl>

              <footer className="mt-auto pt-sm border-t border-outline-variant">
                <Button variant="outline" size="sm" onClick={() => setActiveId(s.id)}>
                  <Icon name="photo_camera" size={16} />
                  {labels.viewEvidence}
                </Button>
              </footer>
            </article>
          </li>
        ))}
      </ul>

      <SanctionDetailModal
        open={activeId != null}
        sanctionId={activeId}
        fetchDetail={fetchMySanctionDetail}
        labels={detailLabels}
        lang={lang}
        onClose={() => setActiveId(null)}
      />
    </>
  );
}

