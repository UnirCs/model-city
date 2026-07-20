import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: UserMiniCard
 *
 * Compact supervision card for the Administration → Citizens / Workers grids.
 * Smaller than {@link CatalogueCard}: it shows the name, email, a secondary line
 * (neighbourhood for citizens, role for workers) and a status badge, plus two
 * actions — view detail (link) and enable/disable (callback).
 *
 * Purely presentational: the parent grid owns the status state and the toggle
 * handler. Rendered inside a Client island, so the toggle button is wired
 * directly.
 *
 * @param {{
 *   name: string,
 *   email: string,
 *   status: 'ACTIVE' | 'DISABLED',
 *   secondary?: string,
 *   secondaryIcon?: string,
 *   detailHref: string,           // already lang-prefixed
 *   busy?: boolean,
 *   onToggleStatus: () => void,
 *   labels: {
 *     actionView: string,
 *     actionDisable: string,
 *     actionEnable: string,
 *     statusActive: string,
 *     statusDisabled: string,
 *   },
 * }} props
 */
export default function UserMiniCard({
  name,
  email,
  status,
  secondary,
  secondaryIcon = 'badge',
  detailHref,
  busy = false,
  onToggleStatus,
  labels,
}) {
  const disabled = status === 'DISABLED';

  return (
    <article className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm overflow-hidden flex flex-col h-full">
      {/* Body */}
      <div className="p-md flex flex-col gap-xs flex-1">
        <div className="min-w-0">
          <h3 className="text-body-md font-semibold text-primary truncate" title={name}>
            {name}
          </h3>
          <p className="text-caption text-on-surface-variant truncate" title={email}>
            {email}
          </p>
        </div>

        {secondary && (
          <p className="text-caption text-on-surface-variant flex items-center gap-xs min-w-0">
            <Icon name={secondaryIcon} size={13} className="text-secondary shrink-0" />
            <span className="truncate" title={secondary}>{secondary}</span>
          </p>
        )}

        <span
          className={[
            'mt-auto inline-flex items-center gap-xs self-start rounded-full px-sm py-[2px] text-caption font-medium',
            disabled
              ? 'bg-error-container text-on-error-container'
              : 'bg-secondary-container text-on-secondary-container',
          ].join(' ')}
        >
          <Icon name={disabled ? 'block' : 'check_circle'} size={12} fill />
          {disabled ? labels.statusDisabled : labels.statusActive}
        </span>
      </div>

      {/* Fused bottom actions */}
      <div className="flex border-t border-outline-variant">
        <Link
          href={detailHref}
          className="flex-1 inline-flex items-center justify-center gap-xs py-xs text-label-md font-medium text-primary hover:bg-surface-container transition-colors"
        >
          <Icon name="visibility" size={16} />
          {labels.actionView}
        </Link>
        <button
          type="button"
          onClick={onToggleStatus}
          disabled={busy}
          className={[
            'flex-1 inline-flex items-center justify-center gap-xs py-xs text-label-md font-medium border-l border-outline-variant transition-colors',
            'disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer',
            disabled
              ? 'text-secondary hover:bg-secondary/10'
              : 'text-error hover:bg-error-container/30',
          ].join(' ')}
        >
          {busy ? (
            <Icon name="progress_activity" size={16} className="animate-spin" />
          ) : (
            <Icon name={disabled ? 'lock_open' : 'block'} size={16} />
          )}
          {disabled ? labels.actionEnable : labels.actionDisable}
        </button>
      </div>
    </article>
  );
}
