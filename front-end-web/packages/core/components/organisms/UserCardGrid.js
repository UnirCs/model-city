'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import UserMiniCard from '@modelcity/core/components/molecules/UserMiniCard';

/**
 * Organism: UserCardGrid (client)
 *
 * Responsive grid of {@link UserMiniCard} (5 per row on desktop) for the
 * Administration → Citizens / Workers subsections. Owns the optimistic account
 * status of each card and invokes the `onToggleStatus` server action, surfacing
 * any failure as an inline banner.
 *
 * @param {{
 *   users: Array<{ id: string, name: string, email: string, role: string, status: string, neighbourhoodName?: string }>,
 *   lang: string,
 *   mode: 'citizens' | 'workers',
 *   detailBasePath: string,        // lang-relative, e.g. '/administration/citizens'
 *   onToggleStatus: (userId: string, status: 'ACTIVE'|'DISABLED') => Promise<{ ok: true } | { error: string, status?: number }>,
 *   labels: {
 *     actionView: string, actionDisable: string, actionEnable: string,
 *     statusActive: string, statusDisabled: string,
 *     toggleError: string, forbiddenAdmin: string,
 *     empty: string,
 *     roleLabels: Record<string, string>,
 *   },
 * }} props
 */
export default function UserCardGrid({ users, lang, mode, detailBasePath, onToggleStatus, labels }) {
  // Status overrides keyed by user id (only changed cards are stored).
  const [statuses, setStatuses] = useState({});
  const [busyId, setBusyId]     = useState(null);
  const [error, setError]       = useState(null);

  const statusOf = (u) => statuses[u.id] ?? u.status;

  async function handleToggle(user) {
    const current = statusOf(user);
    const next = current === 'DISABLED' ? 'ACTIVE' : 'DISABLED';
    setBusyId(user.id);
    setError(null);
    const res = await onToggleStatus(user.id, next);
    setBusyId(null);
    if (res && 'ok' in res) {
      setStatuses((prev) => ({ ...prev, [user.id]: next }));
    } else {
      setError(res?.status === 403 ? labels.forbiddenAdmin : labels.toggleError);
    }
  }

  return (
    <div className="flex flex-col gap-md">
      {error && (
        <div
          role="alert"
          className="flex items-start gap-sm rounded-md bg-error-container text-on-error-container px-md py-sm text-body-sm border border-error/20"
        >
          <Icon name="report" size={18} className="shrink-0 mt-0.5" />
          <p>{error}</p>
        </div>
      )}

      <div className="bg-surface-container-low rounded-md border border-outline-variant shadow-sm p-md">
        {users.length === 0 ? (
          <p className="text-body-md text-on-surface-variant text-center py-lg">{labels.empty}</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-md">
            {users.map((user) => {
              const secondary =
                mode === 'workers'
                  ? labels.roleLabels?.[user.role] ?? user.role
                  : user.neighbourhoodName;
              return (
                <UserMiniCard
                  key={user.id}
                  name={user.name}
                  email={user.email}
                  status={statusOf(user)}
                  secondary={secondary}
                  secondaryIcon={mode === 'workers' ? 'badge' : 'location_city'}
                  detailHref={`/${lang}${detailBasePath}/${encodeURIComponent(user.id)}`}
                  busy={busyId === user.id}
                  onToggleStatus={() => handleToggle(user)}
                  labels={labels}
                />
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
