import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: EmptyState
 *
 * Reusable empty/error state component with a centered card layout.
 * Used for not-found pages, unauthorized views, and other empty states.
 *
 * @param {{
 *   title: string,
 *   subtitle?: string,
 *   body: string,
 *   actionLabel: string,
 *   actionHref: string,
 *   actionIcon?: string,
 * }} props
 */
export default function EmptyState({
  title,
  subtitle,
  body,
  actionLabel,
  actionHref,
  actionIcon = 'home',
}) {
  return (
    <div className="min-h-screen bg-surface flex flex-col items-center justify-center px-md py-xl">
      <main
        id="main"
        tabIndex={-1}
        className="w-full bg-surface-container-lowest rounded-md shadow-md p-xl focus:outline-none flex flex-col gap-lg text-center"
        style={{ maxWidth: '520px' }}
      >
        <div className="flex flex-col gap-xs">
          <h1 className="text-h2 text-primary font-semibold">{title}</h1>
          {subtitle && <p className="text-body-lg text-on-surface-variant">{subtitle}</p>}
        </div>

        <p className="text-body-md text-on-surface-variant leading-relaxed">{body}</p>

        {/* Action button */}
        <Link
          href={actionHref}
          className="inline-flex items-center justify-center gap-sm px-lg py-md rounded-md bg-primary text-on-primary text-label-md font-semibold hover:opacity-90 transition-opacity"
        >
          <Icon name={actionIcon} size={18} className="text-on-primary" />
          {actionLabel}
        </Link>
      </main>
    </div>
  );
}
