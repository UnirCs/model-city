import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: PageHeader
 *
 * Standard list-page header: a filled section icon, the page `<h1>` and an
 * optional subtitle paragraph.
 *
 * @param {{
 *   icon: string,
 *   title: string,
 *   subtitle?: string,
 *   iconClassName?: string,
 *   className?: string,        // wrapper classes (controls bottom spacing)
 * }} props
 */
export default function PageHeader({ icon, title, subtitle, iconClassName = 'text-secondary', className = 'mb-lg' }) {
  return (
    <header className={className}>
      <div className="flex items-center gap-sm mb-xs">
        <Icon name={icon} fill size={24} className={iconClassName} />
        <h1 className="text-h2 text-primary">{title}</h1>
      </div>
      {subtitle && (
        <p className="text-body-md text-on-surface-variant">{subtitle}</p>
      )}
    </header>
  );
}
