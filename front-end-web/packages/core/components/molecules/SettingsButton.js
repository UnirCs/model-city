'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import SettingsModal from '@modelcity/core/components/molecules/SettingsModal';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/**
 * Molecule: SettingsButton
 *
 * Button that opens the settings modal.
 * Placed in the top navigation bar.
 *
 * @param {{ accessToken: string | null }} props
 */
export default function SettingsButton({ accessToken }) {
  const { dict } = useTranslations();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <button
        onClick={() => setIsOpen(true)}
        aria-label={dict?.a11y?.settingsMenuLabel || 'Open settings'}
        className="flex items-center gap-xs p-xs rounded-md text-label-sm text-on-surface-variant hover:bg-surface-container hover:text-on-surface transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
      >
        <Icon name="settings" size={22} />
      </button>

      <SettingsModal open={isOpen} onClose={() => setIsOpen(false)} accessToken={accessToken} />
    </>
  );
}
