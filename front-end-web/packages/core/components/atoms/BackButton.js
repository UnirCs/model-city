'use client';

import Icon from '@modelcity/core/components/atoms/Icon';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';

/**
 * Atom: BackButton
 * Locale-aware "back" control. Instead of a blind `router.back()`, it resolves
 * the path the previous in-app navigation came from and re-localises it to the
 * locale of the page the user is currently on, so a back action never reverts
 * an earlier language switch — on `/es/events/1` it lands on `/es/events`, never
 * `/en/events`. The navigation logic lives in the shared `useLocalizedBack`
 * hook (also used by the form "cancel" controls).
 *
 * The label always comes from the shared `common.back` translation, so call
 * sites never need to pass it.
 *
 * @param {{ className?: string }} props
 */
export default function BackButton({ className = '' }) {
  const { dict } = useTranslations();
  const goBack = useLocalizedBack();
  const label = dict?.common?.back ?? 'Volver';

  return (

    <button
      type="button"
      onClick={goBack}
      className={[
        'group inline-flex items-center gap-xs',
        'text-label-md text-secondary hover:text-primary',
        'transition-colors cursor-pointer',
        className,
      ].join(' ')}
    >
      <Icon
        name="arrow_back"
        size={18}
        className="transition-transform group-hover:-translate-x-1 text-secondary group-hover:text-primary"
      />
      {label}
    </button>
  );
}
