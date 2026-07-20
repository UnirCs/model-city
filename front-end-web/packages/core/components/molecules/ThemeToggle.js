'use client';

import { useTheme } from '@modelcity/core/components/providers/ThemeProvider';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: ThemeToggle (client)
 * Button that toggles between light and dark mode.
 * Uses the current dictionary for accessible labels.
 */
export default function ThemeToggle({ className = '' }) {
  const { theme, toggle } = useTheme();
  const { dict } = useTranslations();
  const isDark = theme === 'dark';

  return (
    <button
      onClick={toggle}
      aria-label={isDark ? dict.theme.activateLight : dict.theme.activateDark}
      className={[
        'flex items-center gap-xs p-xs rounded-md text-label-sm text-on-surface-variant hover:bg-surface-container hover:text-on-surface transition-colors',
        className,
      ].join(' ')}
    >
      <Icon name={isDark ? 'light_mode' : 'dark_mode'} size={22} />
    </button>
  );
}
