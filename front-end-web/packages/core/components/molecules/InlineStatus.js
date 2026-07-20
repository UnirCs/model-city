import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: InlineStatus
 *
 * Centered icon + message block used for in-page load-error and not-found
 * states on detail/sub pages (as opposed to the full-screen {@link EmptyState}).
 *
 * @param {{
 *   icon: string,                 // Material icon name (decorative, aria-hidden)
 *   message: string,              // user-facing status text
 *   tone?: 'error' | 'muted',     // icon colour; defaults to 'muted'
 * }} props
 */
export default function InlineStatus({ icon, message, tone = 'muted' }) {
  const iconColor = tone === 'error' ? 'text-error opacity-60' : 'text-outline opacity-60';

  return (
    <div className="py-xl flex flex-col items-center gap-md text-center">
      <Icon name={icon} size={48} className={iconColor} />
      <p className="text-body-lg text-on-surface-variant">{message}</p>
    </div>
  );
}
