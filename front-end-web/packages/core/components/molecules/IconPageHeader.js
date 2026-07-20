import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: IconPageHeader
 *
 * Compact page header with a rounded icon badge, an `<h1>` title and an
 * optional subtitle. Used on detail/sub pages (e.g. my tickets, event tickets)
 * where a back button precedes the header.
 *
 * @param {{
 *   icon: string,
 *   title: string,
 *   subtitle?: string,
 *   iconWrapClassName?: string,   // badge background (defaults to primary tint)
 *   iconClassName?: string,        // icon colour (defaults to primary)
 * }} props
 */
export default function IconPageHeader({
  icon,
  title,
  subtitle,
  iconWrapClassName = 'bg-primary/10',
  iconClassName = 'text-primary',
}) {
  return (
    <div className="flex items-center gap-sm">
      <div className={`w-10 h-10 rounded-md ${iconWrapClassName} flex items-center justify-center shrink-0`}>
        <Icon name={icon} fill size={24} className={iconClassName} />
      </div>
      <div>
        <h1 className="text-h2 text-primary font-semibold">{title}</h1>
        {subtitle && (
          <p className="text-body-sm text-on-surface-variant">{subtitle}</p>
        )}
      </div>
    </div>
  );
}
