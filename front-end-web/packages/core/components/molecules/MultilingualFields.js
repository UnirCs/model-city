'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import { DEFAULT_LANG, SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';
import { translateText } from '@modelcity/core/lib/actions/translation';

/**
 * Translatable-field primitives shared by every create/edit form.
 *
 * A form declares its translatable fields **once** as a descriptor array and
 * renders them in two tiers:
 *
 *  - {@link DefaultLangFields} — the default language (Spanish) controls, shown
 *    inline with the rest of the common data.
 *  - {@link TranslationSections} — one collapsible section per secondary
 *    language (flag-delimited), placed after the common data. Every field
 *    there carries an "AI translate" button that fills it in from the Spanish
 *    source text via the `translateText` server action.
 *
 * @typedef {{
 *   key: string,
 *   label: string,
 *   hint?: string,
 *   placeholder?: string,
 *   error?: string,
 *   as?: 'input' | 'textarea',
 *   rows?: number,
 *   value: { es: string, en: string, fr: string },
 *   onChange: (lang: string, value: string) => void,
 * }} TranslatableField
 */

/** Flag glyphs per locale — mirrors the language switcher in SettingsModal. */
const FLAGS = { es: '🇪🇸', en: '🇬🇧', fr: '🇫🇷' };

const SECONDARY_LANGS = SUPPORTED_LANGS.filter((l) => l !== DEFAULT_LANG);

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';
const INPUT_ERROR_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-error';

/** Renders the markup for a single localized control (input or textarea). */
function Control({ field, lang, invalid }) {
  const { as = 'input', rows = 3, placeholder } = field;
  const value = field.value?.[lang] ?? '';
  const cls = invalid ? INPUT_ERROR_CLS : INPUT_CLS;

  if (as === 'textarea') {
    return (
      <textarea
        rows={rows}
        value={value}
        onChange={(e) => field.onChange(lang, e.target.value)}
        placeholder={placeholder}
        className={`resize-none ${cls}`}
      />
    );
  }
  return (
    <input
      type="text"
      value={value}
      onChange={(e) => field.onChange(lang, e.target.value)}
      placeholder={placeholder}
      className={cls}
    />
  );
}

/**
 * Tier 1 — default-language controls, rendered inline with the common data.
 *
 * @param {{ fields: TranslatableField[] }} props
 */
export function DefaultLangFields({ fields }) {
  return (
    <div className="flex flex-col gap-md">
      {fields.map((field) => (
        <div key={field.key} className="flex flex-col gap-xs">
          <label className="text-label-md text-on-surface font-medium">{field.label}</label>
          {field.hint && <p className="text-caption text-on-surface-variant">{field.hint}</p>}
          <Control field={field} lang={DEFAULT_LANG} invalid={Boolean(field.error)} />
          {field.error && (
            <p className="text-caption text-error flex items-center gap-xs">
              <Icon name="error" size={14} />
              {field.error}
            </p>
          )}
        </div>
      ))}
    </div>
  );
}

/**
 * AI-translate button. Translates `source` (the Spanish text) into
 * `targetLang` via the `translateText` server action and hands the result to
 * `onResult`. Self-contained: it owns its loading/error state so it can be
 * reused both by {@link SecondaryField} and by ad-hoc translatable lists (e.g.
 * a consultation's objectives).
 *
 * @param {{
 *   source: string,
 *   targetLang: string,
 *   onResult: (text: string) => void,
 *   ariaLabel?: string,
 *   onTranslate?: (input: { text: string, targetLang: string }) => Promise<object>,
 * }} props
 */
export function AiTranslateButton({
  source,
  targetLang,
  onResult,
  ariaLabel,
  onTranslate = translateText,
}) {
  const { dict } = useTranslations();
  const t = dict.aiTranslate;
  const [status, setStatus] = useState('idle'); // idle | loading | error
  const src = (source ?? '').trim();
  const canTranslate = src.length > 0 && status !== 'loading';

  async function handleTranslate() {
    if (!canTranslate) return;
    setStatus('loading');
    try {
      const result = await onTranslate({ text: src, targetLang });
      if (result?.text) {
        onResult(result.text);
        setStatus('idle');
      } else {
        setStatus('error');
      }
    } catch {
      setStatus('error');
    }
  }

  const title = status === 'error' ? t.error : src ? t.hint : t.emptySource;

  return (
    <button
      type="button"
      onClick={handleTranslate}
      disabled={!canTranslate}
      title={title}
      aria-label={ariaLabel ? `${t.button} (${ariaLabel})` : t.button}
      className="flex items-center gap-2xs shrink-0 rounded-full px-sm py-2xs text-label-sm text-primary hover:bg-primary/5 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
    >
      <Icon
        name={
          status === 'loading'
            ? 'progress_activity'
            : status === 'error'
              ? 'error'
              : 'auto_awesome'
        }
        size={16}
        className={
          status === 'loading'
            ? 'animate-spin'
            : status === 'error'
              ? 'text-error'
              : ''
        }
      />
      <span className="hidden sm:inline">
        {status === 'loading' ? t.translating : t.button}
      </span>
    </button>
  );
}

/**
 * One field inside a secondary-language section: label + AI-translate button +
 * control. The button translates the default-language (Spanish) source text.
 */
function SecondaryField({ field, lang, onTranslate }) {
  return (
    <div className="flex flex-col gap-2xs">
      <div className="flex items-center justify-between gap-sm">
        <span className="text-label-sm text-on-surface-variant font-medium">{field.label}</span>
        <AiTranslateButton
          source={field.value?.[DEFAULT_LANG]}
          targetLang={lang}
          onResult={(text) => field.onChange(lang, text)}
          ariaLabel={field.label}
          onTranslate={onTranslate}
        />
      </div>
      <Control field={field} lang={lang} />
    </div>
  );
}

/** One collapsible per-language section, delimited by the language flag. */
function LanguageSection({ lang, langLabel, defaultOpen, children }) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <div className="rounded-md border border-outline-variant bg-surface-container-low overflow-hidden">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
        className="w-full flex items-center justify-between gap-sm px-md py-sm text-on-surface hover:bg-surface-container transition-colors"
      >
        <span className="flex items-center gap-sm">
          <span className="text-xl leading-none" aria-hidden="true">{FLAGS[lang]}</span>
          <span className="text-label-md font-medium">{langLabel}</span>
        </span>
        <Icon name={open ? 'expand_less' : 'expand_more'} size={20} className="text-on-surface-variant" />
      </button>
      <div
        className="grid transition-all duration-300 ease-in-out"
        style={{ gridTemplateRows: open ? '1fr' : '0fr' }}
      >
        <div className="overflow-hidden">
          <div className="flex flex-col gap-md px-md pb-md pt-xs border-t border-outline-variant">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * Tier 2 — collapsible per-language sections with AI translation, placed after
 * the common data.
 *
 * @param {{
 *   fields: TranslatableField[],
 *   defaultOpen?: boolean,
 *   renderExtra?: (lang: string) => React.ReactNode,
 *   onTranslate?: (input: { text: string, targetLang: string }) => Promise<object>,
 * }} props
 */
export function TranslationSections({
  fields,
  defaultOpen = false,
  renderExtra,
  onTranslate = translateText,
}) {
  const { dict } = useTranslations();
  const langLabels = dict.lang;
  const t = dict.aiTranslate;

  return (
    <div className="flex flex-col gap-sm">
      <p className="text-caption text-on-surface-variant flex items-center gap-2xs">
        <Icon name="translate" size={14} />
        {t.sectionHint}
      </p>
      {SECONDARY_LANGS.map((lang) => (
        <LanguageSection
          key={lang}
          lang={lang}
          langLabel={langLabels[lang]}
          defaultOpen={defaultOpen}
        >
          {fields.map((field) => (
            <SecondaryField
              key={field.key}
              field={field}
              lang={lang}
              onTranslate={onTranslate}
            />
          ))}
          {renderExtra?.(lang)}
        </LanguageSection>
      ))}
    </div>
  );
}
