'use client';

import { useState, useCallback } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import Modal from '@modelcity/core/components/molecules/Modal';
import Icon from '@modelcity/core/components/atoms/Icon';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import { SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';
import { relocalizePath } from '@modelcity/core/lib/i18n/localizePath';
import { useAccessibility } from '@modelcity/core/lib/a11y/AccessibilityContext';
import { verifyCertificate } from '@modelcity/core/lib/api/certClient';
import { useAnnounce } from '@modelcity/core/lib/a11y/AnnouncerProvider';

/** Cookie name that persists the user's language preference. */
const LANG_COOKIE = 'NEXT_LANG';
/** How long (ms) the colour feedback stays visible before resetting. */
const FEEDBACK_MS = 3000;

/**
 * Molecule: SettingsModal
 *
 * Modal dialog for app settings:
 * - Language preferences (Spanish, English, French)
 * - Certificate verification (FNMT)
 * - Accessibility options (high contrast, text size, cursor, links)
 *
 * @param {{ open: boolean, onClose: () => void, accessToken: string | null }} props
 */
export default function SettingsModal({ open, onClose, accessToken }) {
  const { dict, lang } = useTranslations();
  const pathname = usePathname();
  const router = useRouter();
  const { settings, updateSetting } = useAccessibility();
  const announce = useAnnounce();

  /** @type {'idle' | 'checking' | 'valid' | 'invalid'} */
  const [certStatus, setCertStatus] = useState('idle');
  const [a11yExpanded, setA11yExpanded] = useState(false);

  const switchLang = useCallback((newLang) => {
    // Persist the preference so it applies to paths without a locale (e.g. the
    // root) and to the next session. The URL locale stays authoritative for
    // the active language, so this is the only place that writes the cookie.
    document.cookie = `${LANG_COOKIE}=${newLang};path=/;max-age=31536000;SameSite=Lax`;
    // Re-localise the current page by swapping the [lang] segment. `replace`
    // (not `push`) updates the current entry in place instead of leaving a
    // stale duplicate in the old language behind it in the history stack.
    router.replace(relocalizePath(pathname, newLang));
  }, [pathname, router]);

  const handleVerify = useCallback(async () => {
    if (certStatus === 'checking') return;
    setCertStatus('checking');
    const certResult = await verifyCertificate(accessToken);
    const isValid = certResult.valid;
    const next = isValid ? 'valid' : 'invalid';
    setCertStatus(next);
    const t = dict?.certificate ?? {};
    announce(isValid ? t.valid ?? t.buttonLabel : t.invalid ?? t.buttonLabel, isValid ? 'polite' : 'assertive');
    setTimeout(() => setCertStatus('idle'), FEEDBACK_MS);
  }, [certStatus, announce, dict, accessToken]);

  const certColourClass = {
    idle:     'text-on-surface-variant hover:bg-surface-container',
    checking: 'text-on-surface-variant opacity-50 cursor-wait',
    valid:    'text-success',
    invalid:  'text-error',
  }[certStatus];

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={dict?.a11y?.settingsMenu || 'Settings'}
    >
      <div className="flex flex-col gap-lg">
        {/* Language Preferences */}
        <div className="flex flex-col gap-xs">
          <div className="flex items-center gap-sm mb-xs">
            <Icon name="language" size={20} />
            <span className="text-body-md text-on-surface font-medium">
              {dict?.a11y?.languagePreferences?.title || 'Language preferences'}
            </span>
          </div>
          <p className="text-caption text-on-surface-variant pl-[2rem] mb-sm">
            {dict?.a11y?.languagePreferences?.description || 'Select the interface language'}
          </p>
          <div className="flex gap-sm pl-[2rem]">
            {SUPPORTED_LANGS.map((l) => {
              const flags = { es: '🇪🇸', en: '🇬🇧', fr: '🇫🇷' };
              return (
                <button
                  key={l}
                  onClick={() => switchLang(l)}
                  aria-pressed={l === lang}
                  aria-label={dict?.lang?.[l] || l.toUpperCase()}
                  className={[
                    'flex-1 px-sm py-xs rounded-md text-label-md font-medium transition-colors',
                    'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
                    l === lang
                      ? 'bg-primary text-on-primary'
                      : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high',
                  ].join(' ')}
                >
                  <span className="text-xl">{flags[l]}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Certificate Verification */}
        <div className="flex flex-col gap-xs">
          <div className="flex items-center gap-sm mb-xs">
            <Icon name="verified_user" size={20} />
            <span className="text-body-md text-on-surface font-medium">
              {dict?.a11y?.certificateVerification?.title || 'FNMT certificate'}
            </span>
          </div>
          <p className="text-caption text-on-surface-variant pl-[2rem] mb-sm">
            {dict?.a11y?.certificateVerification?.description || 'Verify your digital certificate'}
          </p>
          <button
            type="button"
            onClick={handleVerify}
            disabled={certStatus === 'checking'}
            className={[
              'ml-[2rem] px-sm py-xs rounded-md text-label-sm font-medium transition-colors',
              'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
              certStatus === 'valid'
                ? 'bg-success text-on-success'
                : certStatus === 'invalid'
                ? 'bg-error text-on-error'
                : certStatus === 'checking'
                ? 'bg-surface-container text-on-surface-variant opacity-50 cursor-wait'
                : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high',
            ].join(' ')}
          >
            {certStatus === 'checking'
              ? (dict?.certificate?.checking || 'Checking…')
              : certStatus === 'valid'
              ? (dict?.certificate?.valid || 'Valid')
              : certStatus === 'invalid'
              ? (dict?.certificate?.invalid || 'Invalid')
              : (dict?.a11y?.certificateVerification?.buttonLabel || 'Validate certificate')}
          </button>
        </div>

        {/* Accessibility Preferences */}
        <div className="flex flex-col gap-md">
          <button
            onClick={() => setA11yExpanded(!a11yExpanded)}
            aria-expanded={a11yExpanded}
            aria-controls="a11y-content"
            className="flex items-center gap-sm mb-xs w-full text-left"
          >
            <Icon name="accessibility" size={20} />
            <span className="text-body-md text-on-surface font-medium">
              {dict?.a11y?.accessibilityPreferences?.title || 'Accessibility preferences'}
            </span>
            <Icon name={a11yExpanded ? 'expand_less' : 'expand_more'} size={20} className="ml-auto text-on-surface-variant" />
          </button>

          <div
            id="a11y-content"
            className="flex flex-col gap-md overflow-hidden transition-all duration-500 ease-in-out"
            style={{
              maxHeight: a11yExpanded ? '1000px' : '0',
              opacity: a11yExpanded ? '1' : '0',
            }}
          >
              {/* High Contrast */}
              <div className="flex flex-col gap-xs">
                <div className="flex items-center gap-sm">
                  <Icon name="contrast" size={20} />
                  <span className="text-body-md text-on-surface font-medium">
                    {dict?.a11y?.highContrast?.title || 'High contrast'}
                  </span>
                </div>
                <p className="text-caption text-on-surface-variant pl-[2rem] mb-sm">
                  {dict?.a11y?.highContrast?.description || 'Increases contrast between text and background for better readability'}
                </p>
                <button
                  onClick={() => updateSetting('highContrast', !settings.highContrast)}
                  aria-pressed={settings.highContrast}
                  className={[
                    'ml-[2rem] px-sm py-xs rounded-md text-label-sm font-medium transition-colors',
                    'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
                    settings.highContrast
                      ? 'bg-primary text-on-primary'
                      : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high',
                  ].join(' ')}
                >
                  {settings.highContrast
                    ? dict?.a11y?.highContrast?.on || 'On'
                    : dict?.a11y?.highContrast?.off || 'Off'}
                </button>
              </div>

              {/* Text Size */}
              <div className="flex flex-col gap-xs">
                <div className="flex items-center gap-sm mb-xs">
                  <Icon name="text_increase" size={20} />
                  <span className="text-body-md text-on-surface font-medium">
                    {dict?.a11y?.textSize?.title || 'Text size'}
                  </span>
                </div>
                <p className="text-caption text-on-surface-variant pl-[2rem] mb-sm">
                  {dict?.a11y?.textSize?.description || 'Adjust the interface text size'}
                </p>
                <div className="flex gap-sm pl-[2rem]">
                  <button
                    onClick={() => updateSetting('textSize', 'normal')}
                    aria-pressed={settings.textSize === 'normal'}
                    className={[
                      'flex-1 px-sm py-xs rounded-md text-label-sm font-medium transition-colors',
                      'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
                      settings.textSize === 'normal'
                        ? 'bg-primary text-on-primary'
                        : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high',
                    ].join(' ')}
                  >
                    {dict?.a11y?.textSize?.normal || 'Normal'}
                  </button>
                  <button
                    onClick={() => updateSetting('textSize', 'large')}
                    aria-pressed={settings.textSize === 'large'}
                    className={[
                      'flex-1 px-sm py-xs rounded-md text-label-sm font-medium transition-colors',
                      'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
                      settings.textSize === 'large'
                        ? 'bg-primary text-on-primary'
                        : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high',
                    ].join(' ')}
                  >
                    {dict?.a11y?.textSize?.large || 'Large'}
                  </button>
                </div>
              </div>

              {/* Cursor Size */}
              <div className="flex flex-col gap-xs">
                <div className="flex items-center gap-sm">
                  <Icon name="ads_click" size={20} />
                  <span className="text-body-md text-on-surface font-medium">
                    {dict?.a11y?.cursorSize?.title || 'Large cursor'}
                  </span>
                </div>
                <p className="text-caption text-on-surface-variant pl-[2rem] mb-sm">
                  {dict?.a11y?.cursorSize?.description || 'Increases cursor size for better visibility'}
                </p>
                <button
                  onClick={() => updateSetting('cursorSize', !settings.cursorSize)}
                  aria-pressed={settings.cursorSize}
                  className={[
                    'ml-[2rem] px-sm py-xs rounded-md text-label-sm font-medium transition-colors',
                    'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
                    settings.cursorSize
                      ? 'bg-primary text-on-primary'
                      : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high',
                  ].join(' ')}
                >
                  {settings.cursorSize
                    ? dict?.a11y?.cursorSize?.on || 'On'
                    : dict?.a11y?.cursorSize?.off || 'Off'}
                </button>
              </div>

              {/* Highlight Links */}
              <div className="flex flex-col gap-xs">
                <div className="flex items-center gap-sm">
                  <Icon name="link" size={20} />
                  <span className="text-body-md text-on-surface font-medium">
                    {dict?.a11y?.highlightLinks?.title || 'Highlight links'}
                  </span>
                </div>
                <p className="text-caption text-on-surface-variant pl-[2rem] mb-sm">
                  {dict?.a11y?.highlightLinks?.description || 'Adds visual highlighting to all links on the page'}
                </p>
                <button
                  onClick={() => updateSetting('highlightLinks', !settings.highlightLinks)}
                  aria-pressed={settings.highlightLinks}
                  className={[
                    'ml-[2rem] px-sm py-xs rounded-md text-label-sm font-medium transition-colors',
                    'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
                    settings.highlightLinks
                      ? 'bg-primary text-on-primary'
                      : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high',
                  ].join(' ')}
                >
                  {settings.highlightLinks
                    ? dict?.a11y?.highlightLinks?.on || 'On'
                    : dict?.a11y?.highlightLinks?.off || 'Off'}
                </button>
              </div>
            </div>
          </div>
        </div>
    </Modal>
  );
}
