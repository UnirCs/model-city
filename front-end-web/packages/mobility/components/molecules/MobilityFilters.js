'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import FormField from '@modelcity/core/components/atoms/FormField';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border border-outline-variant text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary';

/**
 * Molecule: MobilityFilters
 *
 * Generic filter bar that drives URL search params for the staff mobility
 * tables. Supports a license-plate input, a date range and (optionally) an
 * `active` status selector. On submit it re-navigates to the same pathname
 * with the new params, which triggers an SSR re-fetch.
 *
 * @param {{
 *   labels: object,
 *   showActive?: boolean,
 *   initial: {
 *     licensePlate?: string,
 *     fromDate?: string,
 *     toDate?: string,
 *     active?: string,
 *   },
 * }} props
 */
export default function MobilityFilters({ labels, showActive = false, initial }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const [licensePlate, setLicensePlate] = useState(initial.licensePlate ?? '');
  const [fromDate, setFromDate]         = useState(initial.fromDate ?? '');
  const [toDate, setToDate]             = useState(initial.toDate ?? '');
  const [active, setActive]             = useState(initial.active ?? '');

  // Re-sync local state when the URL changes externally (browser back/forward).
  useEffect(() => {
    /* eslint-disable react-hooks/set-state-in-effect */
    setLicensePlate(initial.licensePlate ?? '');
    setFromDate(initial.fromDate ?? '');
    setToDate(initial.toDate ?? '');
    setActive(initial.active ?? '');
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
      licensePlate: licensePlate.trim(),
      fromDate,
      toDate,
      active,
      // Reset to first page whenever the filters change.
      page: 0,
    });
  }

  function handleReset() {
    setLicensePlate('');
    setFromDate('');
    setToDate('');
    setActive('');
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

      <div className={`grid grid-cols-1 md:grid-cols-2 ${showActive ? 'lg:grid-cols-4' : 'lg:grid-cols-3'} gap-md`}>
        <FormField id="lp" label={labels.licensePlateLabel}>
          <input
            id="lp"
            type="text"
            value={licensePlate}
            onChange={(e) => setLicensePlate(e.target.value)}
            placeholder={labels.licensePlatePlaceholder}
            className={INPUT_CLS}
          />
        </FormField>
        <FormField id="from" label={labels.fromLabel}>
          <input
            id="from"
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className={INPUT_CLS}
          />
        </FormField>
        <FormField id="to" label={labels.toLabel}>
          <input
            id="to"
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className={INPUT_CLS}
          />
        </FormField>
        {showActive && (
          <FormField id="active" label={labels.activeLabel}>
            <div className="relative">
              <select
                id="active"
                value={active}
                onChange={(e) => setActive(e.target.value)}
                className={`appearance-none pr-xl ${INPUT_CLS}`}
              >
                <option value="">{labels.activeAny}</option>
                <option value="true">{labels.activeOnly}</option>
                <option value="false">{labels.expiredOnly}</option>
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




