import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: FilterSection
 *
 * Reusable visual container for filter sections across the app.
 * Provides consistent styling: rounded container, header with icon,
 * and content area for chips/controls.
 *
 * @param {{
 *   title: string,
 *   icon?: string,
 *   children: React.ReactNode,
 *   ariaLabel?: string,
 *   className?: string,
 * }} props
 */
export default function FilterSection({ title, icon = 'filter_list', children, ariaLabel, className = '' }) {
  return (
    <nav
      aria-label={ariaLabel || 'filter'}
      className={`bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md flex flex-col gap-md ${className}`}
    >
      <header className="flex items-center gap-sm">
        <Icon name={icon} size={20} className="text-secondary" />
        <h2 className="text-label-md font-semibold text-primary uppercase tracking-wide">
          {title}
        </h2>
      </header>

      {children}
    </nav>
  );
}
