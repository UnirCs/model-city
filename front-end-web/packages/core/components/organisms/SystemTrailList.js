import Icon from '@modelcity/core/components/atoms/Icon';
import SystemTrailRow from '@modelcity/core/components/molecules/SystemTrailRow';

/**
 * Organism: SystemTrailList
 *
 * Wraps the system-trail results in a single bordered container (Records and
 * the per-user activity panel). Events are stacked full width and separated by
 * dividers. Renders an error banner when the fetch failed and an empty message
 * (inside the container) when there are no matching events. An optional `title`
 * is shown as the container header — used by the detail view where "System
 * events" is a sub-section heading one level below the page title.
 *
 * @param {{
 *   events: Array<object>,
 *   lang: string,
 *   fetchError?: boolean,
 *   title?: string,
 *   titleIcon?: string,
 *   labels: object,
 * }} props
 */
export default function SystemTrailList({ events, lang, fetchError = false, title, titleIcon = 'receipt_long', labels }) {
  if (fetchError) {
    return (
      <div
        role="alert"
        className="flex items-start gap-sm rounded-md bg-error-container text-on-error-container px-md py-sm text-body-sm border border-error/20"
      >
        <Icon name="report" size={18} className="shrink-0 mt-0.5" />
        <p>{labels.loadError}</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-sm">
      {title && (
        <div className="flex items-center gap-sm">
          <Icon name={titleIcon} size={20} className="text-secondary" fill />
          <h2 className="text-h3 text-primary">{title}</h2>
        </div>
      )}

      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        {!events || events.length === 0 ? (
          <p className="text-body-md text-on-surface-variant text-center py-lg">{labels.empty}</p>
        ) : (
          <div className="flex flex-col gap-sm">
            {events.map((event) => (
              <SystemTrailRow key={event.eventId} event={event} lang={lang} labels={labels} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
