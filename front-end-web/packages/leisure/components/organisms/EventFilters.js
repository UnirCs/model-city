import FilterSection from '@modelcity/leisure/components/molecules/FilterSection';
import FilterChips from '@modelcity/leisure/components/molecules/FilterChips';

/**
 * Organism: EventFilters
 *
 * Event type + paid/free filter controls. Each chip links to the events list
 * with the corresponding query string (preserving the other active filter).
 *
 * @param {{
 *   lang: string,
 *   tFilters: object,           // leisure.eventFilters dictionary
 *   typeLabels: object,         // leisure.eventTypes dictionary
 *   eventTypes: string[],       // available event type codes
 *   eventType: string | null,   // active type filter
 *   paid: boolean | null,       // active paid filter
 * }} props
 */
export default function EventFilters({ lang, tFilters, typeLabels, eventTypes, eventType, paid }) {
  return (
    <div className="mb-lg">
      <FilterSection title={tFilters.title} ariaLabel="event filters">
        <div>
          <p className="text-caption text-on-surface-variant mb-sm">{tFilters.typeLabel}</p>
          <FilterChips
            options={[
              { value: '', label: tFilters.anyType, icon: null },
              ...eventTypes.map((code) => ({
                value: code,
                label: typeLabels?.[code] ?? code,
                icon: null,
              })),
            ]}
            activeValue={eventType ?? ''}
            hrefBuilder={(value) => {
              const qs = new URLSearchParams();
              if (value) qs.set('eventType', value);
              if (paid != null) qs.set('paid', String(paid));
              return `/${lang}/events?${qs.toString()}`;
            }}
            ariaLabel="event types"
          />
        </div>
        <div>
          <p className="text-caption text-on-surface-variant mb-sm">{tFilters.paidLabel}</p>
          <FilterChips
            options={[
              { value: '', label: tFilters.anyPaid, icon: null },
              { value: 'false', label: tFilters.freeOnly, icon: null },
              { value: 'true', label: tFilters.paidOnly, icon: null },
            ]}
            activeValue={paid == null ? '' : String(paid)}
            hrefBuilder={(value) => {
              const qs = new URLSearchParams();
              if (eventType) qs.set('eventType', eventType);
              if (value) qs.set('paid', value);
              return `/${lang}/events?${qs.toString()}`;
            }}
            ariaLabel="paid status"
          />
        </div>
      </FilterSection>
    </div>
  );
}
