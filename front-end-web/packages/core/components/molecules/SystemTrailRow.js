'use client';

import { useEffect, useRef, useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import { formatDateTime } from '@modelcity/core/lib/format';

/** Badge colour per operation type. */
const OP_CLASS = {
  CREATE: 'bg-secondary-container text-on-secondary-container',
  UPDATE: 'bg-tertiary-container text-on-tertiary-container',
  DELETE: 'bg-error-container text-on-error-container',
};

/**
 * Molecule: SystemTrailRow (client)
 *
 * Collapsible card for the system-trail viewer. The header (always visible)
 * shows the operation badge on the left, followed by the event type name, the
 * timestamp, and a chevron toggle. When expanded it shows the metadata fields
 * (resource, responsible user, operation ID) each on its own row, followed by
 * the raw payload when present.
 *
 * @param {{
 *   event: {
 *     eventId: string, eventType: string, operationType: string,
 *     occurredAt: string, correlationId?: string, responsibleUserId?: string|null,
 *     resourceType?: string|null, resourceId?: string|null, payload?: object|null,
 *   },
 *   lang: string,
 *   labels: object,
 * }} props
 */
export default function SystemTrailRow({ event, lang, labels }) {
  const [open, setOpen] = useState(false);
  const [bodyHeight, setBodyHeight] = useState(0);
  const bodyRef = useRef(null);

  useEffect(() => {
    if (!bodyRef.current) return;
    const measure = () => setBodyHeight(bodyRef.current?.scrollHeight ?? 0);
    const observer = new ResizeObserver(measure);
    observer.observe(bodyRef.current);
    measure();
    return () => observer.disconnect();
  }, []);

  const opLabel = labels.operationTypes?.[event.operationType] ?? event.operationType;
  const opClass = OP_CLASS[event.operationType] ?? 'bg-surface-container-high text-on-surface-variant';
  const typeLabel = labels.eventTypeLabels?.[event.eventType] ?? event.eventType;
  const resource = event.resourceType
    ? `${event.resourceType}${event.resourceId ? ` #${event.resourceId}` : ''}`
    : '—';
  const hasPayload = event.payload && Object.keys(event.payload).length > 0;

  return (
    <article className="bg-surface-container rounded-md border border-outline-variant shadow-sm overflow-hidden">
      {/* Header — always visible, toggles the card */}
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
        className="w-full flex items-start gap-sm px-md py-sm text-left cursor-pointer hover:bg-surface-container-high transition-colors"
      >
        {/* Mobile: stacked; sm+: fixed-column grid so title always starts at the same x */}
        <div className="flex-1 min-w-0 flex flex-col gap-xs sm:grid sm:grid-cols-[7rem_1fr_auto] sm:items-center sm:gap-sm">
          <div className="sm:flex sm:items-center">
            {event.operationType && (
              <span className={`self-start shrink-0 rounded-full px-sm py-[2px] text-caption font-medium ${opClass}`}>
                {opLabel}
              </span>
            )}
          </div>
          <span className="text-label-md font-semibold text-primary break-words min-w-0">
            {typeLabel}
          </span>
          <span className="text-caption text-on-surface-variant">
            {formatDateTime(event.occurredAt, lang)}
          </span>
        </div>
        <Icon
          name={open ? 'expand_less' : 'expand_more'}
          size={18}
          className="text-on-surface-variant shrink-0 mt-[2px]"
        />
      </button>

      {/* Animated body — always mounted, height transitions from 0 to natural */}
      <div
        style={{
          height: open ? bodyHeight : 0,
          overflow: 'hidden',
          transition: 'height 300ms ease-in-out',
        }}
      >
        <div ref={bodyRef} className="border-t border-outline-variant px-md py-sm flex flex-col gap-sm">
          <dl className="flex flex-col gap-sm text-body-sm sm:grid sm:grid-cols-[max-content_1fr] sm:gap-x-md sm:gap-y-xs">
            <div className="sm:contents">
              <dt className="font-semibold text-on-surface-variant">{labels.colResource}</dt>
              <dd className="text-on-surface break-all sm:truncate" title={resource}>{resource}</dd>
            </div>
            <div className="sm:contents">
              <dt className="font-semibold text-on-surface-variant">{labels.colResponsible}</dt>
              <dd className="text-on-surface break-all sm:truncate" title={event.responsibleUserId ?? '—'}>
                {event.responsibleUserId ?? '—'}
              </dd>
            </div>
            <div className="sm:contents">
              <dt className="font-semibold text-on-surface-variant">{labels.colCorrelation}</dt>
              <dd className="text-on-surface break-all sm:truncate" title={event.correlationId ?? '—'}>
                {event.correlationId ?? '—'}
              </dd>
            </div>
          </dl>

          {hasPayload && (
            <pre className="overflow-x-auto rounded-md bg-surface-container-low border border-outline-variant p-sm text-caption text-on-surface">
              {JSON.stringify(event.payload, null, 2)}
            </pre>
          )}
        </div>
      </div>
    </article>
  );
}
