import Icon from '@modelcity/core/components/atoms/Icon';
import Link from 'next/link';

/**
 * Molecule: FilterChips
 *
 * Generic chip-based filter component. Can render as buttons (with onClick)
 * or as links (with href). Used across event and location filters.
 *
 * @param {{
 *   options: Array<{ value: string | null, label: string, icon?: string | null }>,
 *   activeValue: string | null,
 *   onSelect?: (value: string | null) => void,
 *   hrefBuilder?: (value: string | null) => string,
 *   ariaLabel?: string,
 * }} props
 */
export default function FilterChips({ options, activeValue, onSelect, hrefBuilder, ariaLabel }) {
  const isLink = !!hrefBuilder;

  return (
    <div role="group" aria-label={ariaLabel} className="flex flex-wrap gap-sm">
      {options.map((opt) => {
        const isActive = activeValue === opt.value;
        const content = (
          <>
            {opt.icon && <Icon name={opt.icon} size={16} className="shrink-0" />}
            <span>{opt.label}</span>
          </>
        );

        const chipClassName = [
          'inline-flex items-center justify-center gap-xs px-sm py-[2px] rounded-md text-label-md border transition-colors text-center leading-tight min-h-8',
          isActive
            ? 'bg-primary text-on-primary border-primary'
            : 'bg-surface-container-low text-on-surface-variant border-outline-variant hover:bg-surface-container',
        ].join(' ');

        if (isLink) {
          return (
            <Link
              key={opt.value ?? 'all'}
              href={hrefBuilder(opt.value)}
              scroll={false}
              aria-pressed={isActive}
              className={chipClassName}
            >
              {content}
            </Link>
          );
        }

        return (
          <button
            key={opt.value ?? 'all'}
            onClick={() => onSelect?.(opt.value)}
            aria-pressed={isActive}
            className={chipClassName}
          >
            {content}
          </button>
        );
      })}
    </div>
  );
}
