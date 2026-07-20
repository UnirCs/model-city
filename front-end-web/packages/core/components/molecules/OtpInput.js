'use client';

import { useRef } from 'react';

/**
 * Molecule: OtpInput
 *
 * A controlled row of individual digit boxes for one-time-password entry.
 * Handles keyboard navigation (arrows, backspace), paste, and auto-advance.
 * Calls `onComplete` as soon as all boxes are filled.
 *
 * Intentionally stateless — all digit state is managed by the parent.
 *
 * @param {{
 *   digits:      string[],
 *   onChange:    (digits: string[]) => void,
 *   onComplete:  (digits: string[]) => void,
 *   length?:     number,
 *   disabled?:   boolean,
 *   notice?:     string | null,
 * }} props
 */
export default function OtpInput({
  digits,
  onChange,
  onComplete,
  length = 6,
  disabled = false,
  notice = null,
}) {
  const refs = useRef([]);

  const focus = (index) =>
    refs.current[Math.max(0, Math.min(length - 1, index))]?.focus();

  const handleChange = (index, e) => {
    const char = e.target.value.replace(/\D/g, '');
    if (!char) return;

    const next = [...digits];
    next[index] = char.slice(-1);
    onChange(next);

    if (index < length - 1) focus(index + 1);
    if (next.every((d) => d !== '')) onComplete(next);
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace') {
      e.preventDefault();
      const next = [...digits];
      if (next[index]) {
        next[index] = '';
        onChange(next);
      } else if (index > 0) {
        next[index - 1] = '';
        onChange(next);
        focus(index - 1);
      }
    } else if (e.key === 'ArrowLeft')  focus(index - 1);
    else if  (e.key === 'ArrowRight') focus(index + 1);
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, length);
    if (!pasted) return;

    const next = Array(length).fill('');
    pasted.split('').forEach((ch, i) => { next[i] = ch; });
    onChange(next);
    focus(Math.min(pasted.length, length - 1));
    if (pasted.length === length) onComplete(next);
  };

  return (
    <div className="flex gap-xs mt-xs" onPaste={handlePaste}>
      {Array.from({ length }).map((_, i) => (
        <input
          key={i}
          ref={(el) => { refs.current[i] = el; }}
          type="text"
          inputMode="numeric"
          pattern="[0-9]"
          maxLength={2}
          value={digits[i]}
          disabled={disabled}
          autoComplete={i === 0 ? 'one-time-code' : 'off'}
          onChange={(e) => handleChange(i, e)}
          onKeyDown={(e) => handleKeyDown(i, e)}
          className={[
            'w-10 h-12 text-center text-h3 font-bold rounded-md border-2',
            'transition-colors bg-surface-container-lowest',
            // Focus-visible ring overrides the border styles below so the
            // focus indicator is never lost — WCAG 2.4.7 / 2.4.13.
            'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
            disabled ? 'opacity-50 cursor-not-allowed' : '',
            digits[i]
              ? notice
                ? 'border-orange-400 text-orange-700'
                : 'border-primary text-primary'
              : notice
                ? 'border-orange-300 text-on-surface focus:border-orange-500'
                : 'border-outline text-on-surface focus:border-secondary',
          ].join(' ')}
        />
      ))}
    </div>
  );
}

