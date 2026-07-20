'use client';

import { useId } from 'react';

/**
 * Atom: LicensePlateInput
 *
 * Visual recreation of an EU-style license plate input. The left strip mimics
 * the blue EU band with the country code; the right portion is a free-text
 * input that automatically uppercases its value. Purely presentational: the
 * value/onChange API mirrors a native <input>.
 *
 * @param {{
 *   value: string,
 *   onChange: (next: string) => void,
 *   placeholder?: string,
 *   countryCode?: string,
 *   maxLength?: number,
 *   disabled?: boolean,
 *   error?: boolean,
 *   id?: string,
 *   'aria-label'?: string,
 * }} props
 */
export default function LicensePlateInput({
  value,
  onChange,
  placeholder = '1234 ABC',
  countryCode = 'E',
  maxLength = 16,
  disabled = false,
  error = false,
  id,
  'aria-label': ariaLabel,
}) {
  const autoId = useId();
  const inputId = id ?? autoId;

  const borderCls = error
    ? 'border-error focus-within:border-error focus-within:ring-2 focus-within:ring-error/30'
    : 'border-outline-variant focus-within:border-secondary focus-within:ring-2 focus-within:ring-secondary/30';

  return (
    <div
      className={[
        'relative flex items-stretch h-12 rounded-md border transition-all overflow-hidden bg-surface-container-lowest',
        // Focus-visible ring on the wrapper itself — the inner <input> uses
        // `focus:outline-none` to keep the plate visually clean, but the
        // wrapper paints a high-contrast ring via `focus-within:` above.
        // WCAG 2.2 — 2.4.7, 2.4.11, 2.4.13.
        borderCls,
        disabled ? 'opacity-50' : '',
      ].join(' ')}
    >
      {/* EU country strip */}
      <div
        className="w-8 bg-blue-700 flex flex-col items-center justify-center gap-0.5 shrink-0 px-1"
        aria-hidden="true"
      >
        <span className="text-white text-[8px] font-bold leading-none">
          <abbr title="European Union" className="no-underline">EU</abbr>
        </span>
        <span className="text-white text-[11px] font-bold leading-none">{countryCode}</span>
      </div>

      <input
        id={inputId}
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value.toUpperCase())}
        placeholder={placeholder}
        maxLength={maxLength}
        disabled={disabled}
        autoComplete="off"
        autoCapitalize="characters"
        spellCheck={false}
        aria-label={ariaLabel}
        aria-invalid={error ? 'true' : undefined}
        className="flex-1 bg-transparent border-none focus:ring-0 focus:outline-none text-center text-h3 tracking-widest uppercase text-on-surface placeholder:text-outline placeholder:font-normal placeholder:tracking-normal"
      />
    </div>
  );
}

