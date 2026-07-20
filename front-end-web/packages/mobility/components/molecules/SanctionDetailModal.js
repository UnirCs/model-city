'use client';

import { useEffect, useState } from 'react';
import Modal from '@modelcity/core/components/molecules/Modal';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import { formatDateTime } from '@modelcity/mobility/lib/utils/format';

/**
 * Resolves the inferred MIME type of a base64 string by inspecting the first
 * few bytes. Falls back to JPEG when no signature matches.
 */
function guessMimeFromBase64(b64) {
  if (!b64) return 'image/jpeg';
  if (b64.startsWith('iVBOR')) return 'image/png';
  if (b64.startsWith('R0lGOD')) return 'image/gif';
  if (b64.startsWith('UklGR')) return 'image/webp';
  return 'image/jpeg';
}

/**
 * Inner body responsible for actually fetching the sanction detail. Mounted
 * once per `sanctionId` thanks to a `key` prop on the parent — that way the
 * loading state is initialised through `useState` without ever needing a
 * setState-in-effect synchronisation.
 */
function SanctionDetailBody({ sanctionId, fetchDetail, labels, lang }) {
  const [state, setState] = useState({ status: 'loading', data: null });

  useEffect(() => {
    let cancelled = false;
    fetchDetail(sanctionId)
      .then((res) => {
        if (cancelled) return;
        if (res?.data) setState({ status: 'ready', data: res.data });
        else setState({ status: 'error', data: null });
      })
      .catch(() => {
        if (cancelled) return;
        setState({ status: 'error', data: null });
      });
    return () => { cancelled = true; };
  }, [sanctionId, fetchDetail]);

  const data = state.data;
  const imgSrc = data?.imageBase64
    ? `data:${guessMimeFromBase64(data.imageBase64)};base64,${data.imageBase64}`
    : null;

  if (state.status === 'loading') {
    return (
      <div className="flex items-center justify-center gap-sm py-xl text-on-surface-variant">
        <Icon name="hourglass_top" size={20} />
        <span className="text-body-md">{labels.loading}</span>
      </div>
    );
  }

  if (state.status === 'error' || !data) {
    return (
      <div className="p-md bg-error-container text-on-error-container rounded-md flex items-center gap-sm">
        <Icon name="error_outline" size={20} />
        <span className="text-body-md">{labels.loadError}</span>
      </div>
    );
  }

  return (
    <>
      <dl className="grid grid-cols-2 gap-md text-caption">
        <div>
          <dt className="text-on-surface-variant uppercase tracking-wide">{labels.licensePlate}</dt>
          <dd className="text-h3 text-primary tracking-wide">{data.licensePlate}</dd>
        </div>
        <div>
          <dt className="text-on-surface-variant uppercase tracking-wide">{labels.createdAt}</dt>
          <dd className="text-body-md text-on-surface tabular-nums">
            {formatDateTime(data.createdAt, lang)}
          </dd>
        </div>
        <div className="col-span-2">
          <dt className="text-on-surface-variant uppercase tracking-wide">{labels.location}</dt>
          <dd className="text-body-md text-on-surface tabular-nums">
            {data.latitude?.toFixed(6)}, {data.longitude?.toFixed(6)}
          </dd>
        </div>
      </dl>

      <div>
        <p className="text-label-md text-on-surface mb-xs">{labels.evidence}</p>
        {imgSrc ? (
          /* eslint-disable-next-line @next/next/no-img-element */
          <img
            src={imgSrc}
            alt={labels.evidence}
            className="w-full max-h-[60vh] object-contain rounded-md border border-outline-variant bg-surface-container"
          />
        ) : (
          <div className="aspect-video bg-surface-container rounded-md border border-outline-variant flex items-center justify-center text-on-surface-variant">
            <Icon name="image_not_supported" size={40} className="opacity-40" />
          </div>
        )}
      </div>
    </>
  );
}

/**
 * Molecule: SanctionDetailModal
 *
 * Modal that displays the full detail of a sanction including its base64
 * evidence image. The image payload is requested lazily through `fetchDetail`
 * so the listing endpoint can stay lightweight.
 *
 * @param {{
 *   open: boolean,
 *   sanctionId: number | string | null,
 *   fetchDetail: (id: number | string) => Promise<
 *     { data: { id: number, licensePlate: string, latitude: number, longitude: number,
 *               agentSub?: string, createdAt: string, imageBase64?: string } } |
 *     { error: true }
 *   >,
 *   labels: object,
 *   lang: string,
 *   onClose: () => void,
 * }} props
 */
export default function SanctionDetailModal({ open, sanctionId, fetchDetail, labels, lang, onClose }) {
  return (
    <Modal open={open} onClose={onClose} title={labels.modalTitle} closeLabel={labels.close}>
      <div className="flex flex-col gap-md">
        {open && sanctionId != null && (
          <SanctionDetailBody
            key={sanctionId}
            sanctionId={sanctionId}
            fetchDetail={fetchDetail}
            labels={labels}
            lang={lang}
          />
        )}

        <div className="flex justify-end pt-sm border-t border-outline-variant">
          <Button variant="outline" onClick={onClose}>{labels.close}</Button>
        </div>
      </div>
    </Modal>
  );
}



