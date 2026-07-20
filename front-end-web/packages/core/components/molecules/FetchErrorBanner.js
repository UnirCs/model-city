import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: FetchErrorBanner
 *
 * Card-framed error banner shown on list pages when a server fetch fails.
 *
 * @param {{ message: string, className?: string, bare?: boolean }} props
 *   `className` is appended to the outer card wrapper for per-page spacing.
 *   `bare` renders just the coloured banner without the surrounding card
 *   (for use inside an existing card/section).
 */
export default function FetchErrorBanner({ message, className = '', bare = false }) {
  const banner = (
    <div className="p-md bg-error-container text-on-error-container rounded-md flex items-center gap-sm">
      <Icon name="error_outline" size={20} />
      <span className="text-body-md">{message}</span>
    </div>
  );

  if (bare) return banner;

  return (
    <div className={`bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md ${className}`.trim()}>
      {banner}
    </div>
  );
}
