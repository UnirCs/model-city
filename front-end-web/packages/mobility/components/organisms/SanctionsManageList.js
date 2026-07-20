'use client';

import { useState } from 'react';
import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import SanctionDetailModal from '@modelcity/mobility/components/molecules/SanctionDetailModal';
import { formatDateTime } from '@modelcity/mobility/lib/utils/format';
import { fetchSanctionDetail } from '@modelcity/mobility/lib/actions/mobility';

/**
 * Organism: SanctionsManageList
 *
 * Staff list of every registered sanction. Renders the rows as a responsive
 * card grid (matching the design reference) and opens the lazy-loading
 * detail modal when a card is selected.
 *
 * @param {{
 *   sanctions: Array<object>,
 *   labels: object,
 *   detailLabels: object,
 *   lang: string,
 *   canCreate?: boolean,
 *   createButtonLabel?: string,
 *   createHref?: string,
 * }} props
 */
export default function SanctionsManageList({ sanctions, labels, detailLabels, lang, canCreate, createButtonLabel, createHref }) {
  const [activeId, setActiveId] = useState(null);

  if (sanctions.length === 0 && !canCreate) {
    return (
      <div className="py-2xl text-center text-on-surface-variant flex flex-col items-center gap-md">
        <Icon name="inbox" size={48} className="opacity-40" />
        <p className="text-body-lg">{labels.empty}</p>
      </div>
    );
  }

  return (
    <>
      <ul className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
        {canCreate && createHref && (
          <li>
            <Link
              href={createHref}
              className="group border-2 border-dashed border-error/40 bg-error-container/10 hover:border-error hover:bg-error-container/20 transition-all flex flex-col items-center justify-center gap-sm py-lg px-md text-center min-h-[221px] rounded-md"
            >
              <span className="w-12 h-12 rounded-full bg-error/10 group-hover:bg-error/20 flex items-center justify-center transition-colors">
                <Icon name="add" size={24} className="text-error" />
              </span>
              <span className="text-label-md text-error font-semibold">{createButtonLabel}</span>
            </Link>
          </li>
        )}
        {sanctions.map((s) => (
          <li key={s.id}>
            <article className="bg-surface-container-lowest rounded-md shadow-sm hover:shadow-md border border-outline-variant overflow-hidden flex flex-col transition-all duration-200 hover:-translate-y-1 h-full">
              <div className="px-md pt-md pb-0 flex-1 flex flex-col gap-xs">
                <header className="flex items-start gap-sm">
                  <div className="w-10 h-10 rounded-md bg-error-container text-on-error-container flex items-center justify-center shrink-0">
                    <Icon name="gavel" fill size={20} />
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-body-md text-primary line-clamp-2 font-semibold">
                      {s.licensePlate}
                    </h3>
                    <p className="text-caption text-on-surface-variant">
                      {labels.ref} #{s.id}
                    </p>
                  </div>
                </header>

                <p className="text-caption text-on-surface-variant flex items-center gap-xs flex-wrap">
                  <span className="inline-flex items-center gap-xs">
                    <Icon name="schedule" size={13} className="text-secondary shrink-0" />
                    <span>{formatDateTime(s.createdAt, lang)}</span>
                  </span>
                  <span aria-hidden="true" className="opacity-40">·</span>
                  <span className="inline-flex items-center gap-xs">
                    <Icon name="location_on" size={13} className="text-secondary shrink-0" />
                    <span>{s.latitude?.toFixed(4)}, {s.longitude?.toFixed(4)}</span>
                  </span>
                  <span aria-hidden="true" className="opacity-40">·</span>
                  <span className="inline-flex items-center gap-xs">
                    <Icon name="supervisor_account" size={13} className="text-secondary shrink-0" />
                    <span>{s.agentSub}</span>
                  </span>
                </p>
              </div>

              <div className="mt-auto pt-sm">
                <button
                  onClick={() => setActiveId(s.id)}
                  className="block w-full text-center py-sm text-label-md font-medium bg-primary text-on-primary hover:bg-secondary transition-colors cursor-pointer"
                >
                  {labels.viewDetail}
                </button>
              </div>
            </article>
          </li>
        ))}
      </ul>

      <SanctionDetailModal
        open={activeId != null}
        sanctionId={activeId}
        fetchDetail={fetchSanctionDetail}
        labels={detailLabels}
        lang={lang}
        onClose={() => setActiveId(null)}
      />
    </>
  );
}

