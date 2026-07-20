import Link from 'next/link';
import Avatar from '@modelcity/core/components/atoms/Avatar';
import Icon from '@modelcity/core/components/atoms/Icon';
import { formatDateTime } from '@modelcity/core/lib/format';

/**
 * Sub-component: a single read-only profile field row.
 */
function Field({ icon, label, value }) {
  return (
    <div className="flex items-start gap-md px-md py-sm">
      <Icon name={icon} size={18} className="text-secondary mt-xs shrink-0" />
      <div className="min-w-0">
        <dt className="text-caption text-on-surface-variant uppercase tracking-wide">{label}</dt>
        <dd className="text-body-md text-on-surface wrap-break-word">{value}</dd>
      </div>
    </div>
  );
}

/**
 * Organism: UserDetailView
 *
 * Admin detail layout for a citizen or worker: a back link, a read-only profile
 * card and a "System events" section. The system-trail panel itself (filters,
 * list and pager) is composed by the page and passed as `children`, keeping
 * this organism agnostic of how the events are fetched.
 *
 * @param {{
 *   profile: {
 *     id: string, name: string, email: string, createdAt?: string,
 *     role: string, status: string,
 *     neighbourhood?: { displayName?: string } | null,
 *   },
 *   lang: string,
 *   backHref: string,             // already lang-prefixed
 *   detailLabels: object,         // admin.userDetail
 *   commonLabels: object,         // admin.common (roleLabels, statusActive/Disabled)
 *   children: React.ReactNode,    // system-trail panel
 * }} props
 */
export default function UserDetailView({ profile, lang, backHref, detailLabels, commonLabels, children }) {
  const disabled = profile.status === 'DISABLED';
  const roleLabel = commonLabels.roleLabels?.[profile.role] ?? profile.role;
  const statusLabel = disabled ? commonLabels.statusDisabled : commonLabels.statusActive;

  return (
    <div className="flex flex-col gap-lg">
      <Link
        href={backHref}
        className="inline-flex items-center gap-xs text-label-md text-on-surface-variant hover:text-primary transition-colors self-start"
      >
        <Icon name="arrow_back" size={18} />
        {detailLabels.back}
      </Link>

      {/* Profile card */}
      <section className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm overflow-hidden">
        <div className="flex items-center gap-md p-lg border-b border-outline-variant">
          <Avatar name={profile.name} size="lg" className={disabled ? 'grayscale opacity-70' : ''} />
          <div className="min-w-0 flex-1">
            <h1 className="text-h2 text-primary leading-tight truncate">{profile.name}</h1>
            <p className="text-body-md text-on-surface-variant truncate">{profile.email}</p>
          </div>
          <span
            className={[
              'shrink-0 inline-flex items-center gap-xs rounded-full px-sm py-[2px] text-caption font-medium',
              disabled
                ? 'bg-error-container text-on-error-container'
                : 'bg-secondary-container text-on-secondary-container',
            ].join(' ')}
          >
            <Icon name={disabled ? 'block' : 'check_circle'} size={12} fill />
            {statusLabel}
          </span>
        </div>

        <div className="px-md py-sm bg-surface-container-low border-b border-outline-variant">
          <h2 className="text-h3 text-primary">{detailLabels.profileTitle}</h2>
        </div>
        <dl className="divide-y divide-outline-variant">
          <Field icon="person" label={detailLabels.fieldName} value={profile.name} />
          <Field icon="email" label={detailLabels.fieldEmail} value={profile.email} />
          <Field icon="badge" label={detailLabels.fieldRole} value={roleLabel} />
          <Field icon="toggle_on" label={detailLabels.fieldStatus} value={statusLabel} />
          <Field
            icon="location_city"
            label={detailLabels.fieldNeighbourhood}
            value={profile.neighbourhood?.displayName ?? '—'}
          />
          <Field
            icon="calendar_today"
            label={detailLabels.fieldSince}
            value={formatDateTime(profile.createdAt, lang)}
          />
        </dl>
      </section>

      {/* System events */}
      {children}
    </div>
  );
}
