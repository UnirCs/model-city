'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import FormField from '@modelcity/core/components/atoms/FormField';
import barrios from '@modelcity/core/lib/config/neighbourhoods';
import { ROLES } from '@modelcity/core/lib/auth/roles';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border border-outline-variant text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary';

/** Worker roles available in the workers filter. */
const WORKER_ROLES = [ROLES.OPERATOR, ROLES.BACKOFFICE, ROLES.MOBILITY_AGENT];

/**
 * Molecule: AdminUserFilters (client)
 *
 * Filter bar that drives URL search params for the Administration → Citizens
 * and Workers grids (same SSR re-fetch pattern as MobilityFilters). Always
 * exposes a name search; adds a neighbourhood select in `citizens` mode and a
 * role select in `workers` mode. Resets `page` to 0 on every change.
 *
 * @param {{
 *   mode: 'citizens' | 'workers',
 *   labels: {
 *     filtersTitle: string, nameLabel: string, namePlaceholder: string,
 *     neighbourhoodLabel: string, neighbourhoodAny: string,
 *     roleLabel: string, roleAny: string, apply: string, reset: string,
 *     roleLabels: Record<string, string>,
 *   },
 *   initial: { name?: string, neighbourhoodId?: string, role?: string },
 * }} props
 */
export default function AdminUserFilters({ mode, labels, initial }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const [name, setName]                       = useState(initial.name ?? '');
  const [neighbourhoodId, setNeighbourhoodId] = useState(initial.neighbourhoodId ?? '');
  const [role, setRole]                       = useState(initial.role ?? '');

  // Re-sync local state when the URL changes externally (back/forward).
  useEffect(() => {
    /* eslint-disable react-hooks/set-state-in-effect */
    setName(initial.name ?? '');
    setNeighbourhoodId(initial.neighbourhoodId ?? '');
    setRole(initial.role ?? '');
    /* eslint-enable react-hooks/set-state-in-effect */
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  function navigate(nextParams) {
    const qs = new URLSearchParams();
    for (const [k, v] of Object.entries(nextParams)) {
      if (v != null && v !== '') qs.set(k, String(v));
    }
    const search = qs.toString();
    router.push(`${pathname}${search ? `?${search}` : ''}`);
  }

  function handleSubmit(event) {
    event.preventDefault();
    navigate({
      name: name.trim(),
      neighbourhoodId: mode === 'citizens' ? neighbourhoodId : '',
      role: mode === 'workers' ? role : '',
      page: 0,
    });
  }

  function handleReset() {
    setName('');
    setNeighbourhoodId('');
    setRole('');
    navigate({});
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md flex flex-col gap-md"
    >
      <header className="flex items-center gap-sm">
        <Icon name="filter_list" size={20} className="text-secondary" />
        <h2 className="text-label-md font-semibold text-primary uppercase tracking-wide">
          {labels.filtersTitle}
        </h2>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-md">
        <FormField id="user-name" label={labels.nameLabel}>
          <input
            id="user-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder={labels.namePlaceholder}
            className={INPUT_CLS}
          />
        </FormField>

        {mode === 'citizens' && (
          <FormField id="user-neighbourhood" label={labels.neighbourhoodLabel}>
            <div className="relative">
              <select
                id="user-neighbourhood"
                value={neighbourhoodId}
                onChange={(e) => setNeighbourhoodId(e.target.value)}
                className={`appearance-none pr-xl ${INPUT_CLS}`}
              >
                <option value="">{labels.neighbourhoodAny}</option>
                {barrios.map((zone) => (
                  <optgroup key={zone.zoneName} label={zone.zone}>
                    {zone.neighbourhoods.map((n) => (
                      <option key={n.id} value={n.id}>{n.displayName}</option>
                    ))}
                  </optgroup>
                ))}
              </select>
              <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                <Icon name="expand_more" size={20} />
              </span>
            </div>
          </FormField>
        )}

        {mode === 'workers' && (
          <FormField id="user-role" label={labels.roleLabel}>
            <div className="relative">
              <select
                id="user-role"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                className={`appearance-none pr-xl ${INPUT_CLS}`}
              >
                <option value="">{labels.roleAny}</option>
                {WORKER_ROLES.map((r) => (
                  <option key={r} value={r}>{labels.roleLabels?.[r] ?? r}</option>
                ))}
              </select>
              <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                <Icon name="expand_more" size={20} />
              </span>
            </div>
          </FormField>
        )}
      </div>

      <div className="flex justify-end gap-sm">
        <Button type="button" variant="outline" onClick={handleReset}>
          <Icon name="restart_alt" size={16} />
          {labels.reset}
        </Button>
        <Button type="submit" variant="primary">
          <Icon name="search" size={16} />
          {labels.apply}
        </Button>
      </div>
    </form>
  );
}
