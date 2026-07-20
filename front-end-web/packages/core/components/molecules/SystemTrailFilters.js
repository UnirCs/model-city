'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import FormField from '@modelcity/core/components/atoms/FormField';
import { availableTrailModules, trailModuleById } from '@modelcity/core/lib/config/systemTrails';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border border-outline-variant text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary';

/**
 * Molecule: SystemTrailFilters (client)
 *
 * Drives the URL search params for the system-trail viewer (Registros and the
 * per-user activity panel). The admin picks **one module at a time** (only
 * enabled modules are listed); the event-type select is scoped to that module
 * and is cleared whenever the module changes. Optional date range; the
 * responsible-user input is only shown in the standalone Registros view
 * (`showResponsible`), since the per-user panel fixes it.
 *
 * Date inputs hold `yyyy-mm-dd`; the server page snaps them to ISO bounds.
 *
 * @param {{
 *   labels: object,
 *   showResponsible?: boolean,
 *   initial: { module?: string, eventType?: string, responsibleUserId?: string, from?: string, to?: string },
 * }} props
 */
export default function SystemTrailFilters({ labels, showResponsible = false, initial }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const modules = useMemo(() => availableTrailModules(), []);
  const defaultModule = modules[0]?.id ?? 'core';

  const [module, setModule]                     = useState(initial.module ?? defaultModule);
  const [eventType, setEventType]               = useState(initial.eventType ?? '');
  const [responsibleUserId, setResponsibleUserId] = useState(initial.responsibleUserId ?? '');
  const [from, setFrom]                         = useState(initial.from ?? '');
  const [to, setTo]                             = useState(initial.to ?? '');

  // Re-sync local state when the URL changes externally (back/forward).
  useEffect(() => {
    /* eslint-disable react-hooks/set-state-in-effect */
    setModule(initial.module ?? defaultModule);
    setEventType(initial.eventType ?? '');
    setResponsibleUserId(initial.responsibleUserId ?? '');
    setFrom(initial.from ?? '');
    setTo(initial.to ?? '');
    /* eslint-enable react-hooks/set-state-in-effect */
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const eventTypes = trailModuleById(module)?.eventTypes ?? [];

  function navigate(nextParams) {
    const qs = new URLSearchParams();
    for (const [k, v] of Object.entries(nextParams)) {
      if (v != null && v !== '') qs.set(k, String(v));
    }
    const search = qs.toString();
    router.push(`${pathname}${search ? `?${search}` : ''}`);
  }

  function handleModuleChange(value) {
    setModule(value);
    setEventType(''); // event types are module-scoped
  }

  function handleSubmit(event) {
    event.preventDefault();
    navigate({
      module,
      eventType,
      responsibleUserId: showResponsible ? responsibleUserId.trim() : '',
      from,
      to,
      page: 0,
    });
  }

  function handleReset() {
    setModule(defaultModule);
    setEventType('');
    setResponsibleUserId('');
    setFrom('');
    setTo('');
    navigate({ module: defaultModule });
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
        {/* Module */}
        <FormField id="ev-module" label={labels.moduleLabel}>
          <div className="relative">
            <select
              id="ev-module"
              value={module}
              onChange={(e) => handleModuleChange(e.target.value)}
              className={`appearance-none pr-xl ${INPUT_CLS}`}
            >
              {modules.map((m) => (
                <option key={m.id} value={m.id}>{labels.modules?.[m.labelKey] ?? m.id}</option>
              ))}
            </select>
            <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
              <Icon name="expand_more" size={20} />
            </span>
          </div>
        </FormField>

        {/* Event type (module-scoped) */}
        <FormField id="ev-type" label={labels.eventTypeLabel}>
          <div className="relative">
            <select
              id="ev-type"
              value={eventType}
              onChange={(e) => setEventType(e.target.value)}
              className={`appearance-none pr-xl ${INPUT_CLS}`}
            >
              <option value="">{labels.eventTypeAny}</option>
              {eventTypes.map((t) => (
                <option key={t} value={t}>{labels.eventTypeLabels?.[t] ?? t}</option>
              ))}
            </select>
            <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
              <Icon name="expand_more" size={20} />
            </span>
          </div>
        </FormField>

        {/* Responsible user id (Registros only) */}
        {showResponsible && (
          <FormField id="ev-responsible" label={labels.responsibleLabel}>
            <input
              id="ev-responsible"
              type="text"
              value={responsibleUserId}
              onChange={(e) => setResponsibleUserId(e.target.value)}
              placeholder={labels.responsiblePlaceholder}
              className={INPUT_CLS}
            />
          </FormField>
        )}

        {/* Date range */}
        <FormField id="ev-from" label={labels.fromLabel}>
          <input
            id="ev-from"
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className={INPUT_CLS}
          />
        </FormField>
        <FormField id="ev-to" label={labels.toLabel}>
          <input
            id="ev-to"
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className={INPUT_CLS}
          />
        </FormField>
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
