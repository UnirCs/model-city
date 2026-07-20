'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import EditQuestionModal from '@modelcity/engagement/components/molecules/EditQuestionModal';

/**
 * Molecule: EditQuestionButton
 *
 * Renders the "Edit consultation" button and conditionally mounts the
 * {@link EditQuestionModal}. Kept as a separate client component so the
 * parent page can remain a server component.
 *
 * Only renders for future consultations (the parent page is responsible
 * for the visibility condition).
 *
 * @param {{
 *   question: object,
 *   t:        object,
 *   lang:     string,
 * }} props
 */
export default function EditQuestionButton({ question, t }) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="flex items-center justify-center gap-sm w-full px-md py-sm rounded-md border border-primary text-primary bg-surface text-label-md font-semibold hover:bg-primary/5 transition-all active:scale-95"
      >
        <Icon name="edit" size={18} />
        {t.buttonLabel}
      </button>

      {open && (
        <EditQuestionModal
          question={question}
          t={t}
          onClose={() => setOpen(false)}
        />
      )}
    </>
  );
}



