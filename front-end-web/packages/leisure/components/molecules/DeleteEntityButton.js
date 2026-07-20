'use client';

import { useState, useTransition } from 'react';
import { useRouter } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import Modal from '@modelcity/core/components/molecules/Modal';
import { useAnnounce } from '@modelcity/core/lib/a11y/AnnouncerProvider';

/**
 * Molecule: DeleteEntityButton
 *
 * Renders a destructive action button that, when clicked, opens a confirmation
 * modal and invokes the provided server action. On success, navigates to the
 * configured fallback URL.
 *
 * Generic enough to be reused for city routes, city places or any other
 * resource whose deletion requires explicit confirmation.
 *
 * @param {{
 *   id:           string | number,
 *   action:       (id: string | number) => Promise<{ ok?: true, error?: string }>,
 *   onSuccessHref: string,
 *   labels: {
 *     button:        string,
 *     confirmTitle:  string,
 *     confirmBody:   string,
 *     confirm:       string,
 *     cancel:        string,
 *     deleting:      string,
 *     error:         string,
 *   },
 * }} props
 */
export default function DeleteEntityButton({
  id,
  action,
  onSuccessHref,
  labels,
  className = '',
}) {
  const router = useRouter();
  const announce = useAnnounce();
  const [open, setOpen] = useState(false);
  const [error, setError] = useState('');
  const [pending, startTransition] = useTransition();

  function handleConfirm() {
    setError('');
    startTransition(async () => {
      const result = await action(id);
      if (result?.error) {
        setError(labels.error);
        announce(labels.error, 'assertive');
        return;
      }
      setOpen(false);
      router.replace(onSuccessHref);
      router.refresh();
    });
  }

  return (
    <>
      <button
        type="button"
        onClick={() => { setError(''); setOpen(true); }}
        className={`inline-flex items-center justify-center gap-sm px-md py-sm rounded-md border border-error text-error bg-surface text-label-md font-semibold hover:bg-error/5 transition-all active:scale-95 ${className}`}
      >
        <Icon name="delete" size={18} />
        {labels.button}
      </button>

      <Modal
        open={open}
        onClose={() => (pending ? null : setOpen(false))}
        title={labels.confirmTitle}
        closeLabel={labels.cancel}
      >
        <div className="flex flex-col gap-md">
          <p className="text-body-md text-on-surface-variant">
            {labels.confirmBody}
          </p>

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
              onClick={() => setOpen(false)}
              disabled={pending}
            >
              {labels.cancel}
            </Button>
            <Button
              type="button"
              variant="error"
              onClick={handleConfirm}
              disabled={pending}
            >
              {pending ? labels.deleting : labels.confirm}
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}


