'use client';

/**
 * Atom: DurationSlider
 *
 * Range slider with a numeric readout and quick-pick chips. Used by the
 * citizen reservation and renewal flows to pick a parking duration between
 * 20 and 150 minutes in 5-minute increments (1 cent per minute, max €1.50).
 *
 * @param {{
 *   value: number,
 *   onChange: (next: number) => void,
 *   min?: number,
 *   max?: number,
 *   step?: number,
 *   quickPicks?: number[],
 *   formatDuration: (minutes: number) => string,
 *   label?: string,
 *   hint?: string,
 *   quickPicksLabel?: string,
 *   disabled?: boolean,
 * }} props
 */
export default function DurationSlider({
  value,
  onChange,
  min = 20,
  max = 150,
  step = 5,
  quickPicks = [20, 60, 90, 150],
  formatDuration,
  label,
  hint,
  quickPicksLabel,
  disabled = false,
}) {
  /** Clamps the value into [min, max] before propagating. */
  function handleChange(next) {
    const n = Math.min(max, Math.max(min, Number(next)));
    onChange(n);
  }

  return (
    <div className="bg-surface-container rounded-md p-md border border-outline-variant flex flex-col gap-sm">
      <div className="flex justify-between items-end">
        {label && (
          <label className="text-label-md text-on-surface font-medium">{label}</label>
        )}
        <span className="text-h2 text-primary tabular-nums leading-none">
          {formatDuration(value)}
        </span>
      </div>

      <input
        type="range"
        value={value}
        min={min}
        max={max}
        step={step}
        disabled={disabled}
        onChange={(e) => handleChange(e.target.value)}
        className="duration-slider w-full"
        aria-label={label}
      />

      <div className="flex justify-between text-caption text-on-surface-variant px-1">
        <span>{formatDuration(min)}</span>
        <span>{formatDuration(Math.round((min + max) / 2))}</span>
        <span>{formatDuration(max)}</span>
      </div>

      {quickPicks.length > 0 && (
        <div className="mt-xs">
          {quickPicksLabel && (
            <p className="text-caption text-on-surface-variant mb-xs">{quickPicksLabel}</p>
          )}
          <div className="flex gap-xs flex-wrap">
            {quickPicks.map((pick) => {
              const selected = pick === value;
              return (
                <button
                  key={pick}
                  type="button"
                  disabled={disabled}
                  onClick={() => handleChange(pick)}
                  className={[
                    'px-sm py-xs rounded-full text-caption font-semibold transition-colors',
                    selected
                      ? 'bg-secondary-container border border-secondary text-on-secondary-container shadow-sm'
                      : 'border border-outline-variant text-on-surface-variant hover:bg-surface-variant',
                  ].join(' ')}
                >
                  {formatDuration(pick)}
                </button>
              );
            })}
          </div>
        </div>
      )}

      {hint && <p className="text-caption text-on-surface-variant">{hint}</p>}

      <style jsx>{`
        .duration-slider {
          -webkit-appearance: none;
          appearance: none;
          background: transparent;
          height: 24px;
        }
        .duration-slider::-webkit-slider-runnable-track {
          height: 4px;
          background: var(--color-outline-variant, #c4c6cf);
          border-radius: 2px;
        }
        .duration-slider::-webkit-slider-thumb {
          -webkit-appearance: none;
          appearance: none;
          height: 20px;
          width: 20px;
          border-radius: 50%;
          background: #13696a;
          cursor: pointer;
          margin-top: -8px;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
          border: 2px solid #ffffff;
        }
        .duration-slider::-moz-range-track {
          height: 4px;
          background: #c4c6cf;
          border-radius: 2px;
        }
        .duration-slider::-moz-range-thumb {
          height: 20px;
          width: 20px;
          border-radius: 50%;
          background: #13696a;
          cursor: pointer;
          border: 2px solid #ffffff;
        }
        .duration-slider:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      `}</style>
    </div>
  );
}

