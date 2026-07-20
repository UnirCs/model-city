'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import { openQuestion, closeQuestion } from '@modelcity/engagement/lib/actions/questions';
import { useAnnounce } from '@modelcity/core/lib/a11y/AnnouncerProvider';

/**
 * Molecule: QuestionStatusActions
 *
 * Renders admin / backoffice action buttons that change a consultation status:
 *   - "Start survey"  — future consultations only  → PATCH openDate=today
 *   - "Close survey"  — active consultations only  → PATCH closeDate=today
 *
 * @param {{
 *   questionId: string | number,
 *   status:     'active' | 'future' | 'past',
 *   t:          object,   – dict.participation.questionDetail.adminActions
 * }} props
 */
export default function QuestionStatusActions({ questionId, status, t }) {
  const router = useRouter();
  const announce = useAnnounce();
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');

  const canOpen  = status === 'future';
  const canClose = status === 'active';

  if (!canOpen && !canClose) return null;

  async function handleAction(action) {
    setLoading(true);
    setError('');
    try {
      const result = action === 'open'
        ? await openQuestion(questionId)
        : await closeQuestion(questionId);

      if (result?.error) {
        setError(t.actionError);
        announce(t.actionError, 'assertive');
        return;
      }
      router.refresh();
    } catch {
      setError(t.actionError);
      announce(t.actionError, 'assertive');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex flex-col gap-sm">
      {canOpen && (
        <button
          type="button"
          disabled={loading}
          onClick={() => handleAction('open')}
          className="flex items-center justify-center gap-sm w-full px-md py-sm rounded-md bg-secondary text-on-secondary text-label-md font-semibold hover:shadow-md transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading
            ? <Icon name="progress_activity" size={18} className="animate-spin" />
            : <Icon name="play_arrow" fill size={18} />
          }
          {t.startSurvey}
        </button>
      )}

      {canClose && (
        <button
          type="button"
          disabled={loading}
          onClick={() => handleAction('close')}
          className="flex items-center justify-center gap-sm w-full px-md py-sm rounded-md bg-error text-on-error text-label-md font-semibold hover:shadow-md transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading
            ? <Icon name="progress_activity" size={18} className="animate-spin" />
            : <Icon name="stop_circle" fill size={18} />
          }
          {t.closeSurvey}
        </button>
      )}

      {error && (
        <p className="text-caption text-error flex items-center gap-xs">
          <Icon name="error" size={14} />
          {error}
        </p>
      )}
    </div>
  );
}

