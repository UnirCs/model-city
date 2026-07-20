'use client';

import { useState } from 'react';
import Avatar from '@modelcity/core/components/atoms/Avatar';
import Icon from '@modelcity/core/components/atoms/Icon';
import barrios from '@modelcity/core/lib/config/neighbourhoods';

/* ─────────────────────────────────────────────
   Sub-component: EditableField (free text)
   ───────────────────────────────────────────── */
function EditableField({ iconName, label, value, editable = false, onSave, saveLabel, cancelLabel }) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft]     = useState(value);

  function handleSave() {
    onSave?.(draft.trim() || value);
    setEditing(false);
  }

  function handleCancel() {
    setDraft(value);
    setEditing(false);
  }

  return (
    <div className="flex items-start gap-md px-md py-sm group">
      <Icon name={iconName} size={18} className="text-secondary mt-xs shrink-0" />
      <div className="flex-1 min-w-0">
        <dt className="text-caption text-on-surface-variant uppercase tracking-wide">{label}</dt>

        {editing ? (
          <div className="mt-xs flex flex-col gap-xs">
            <input
              autoFocus
              value={draft}
              onChange={e => setDraft(e.target.value)}
              onKeyDown={e => {
                if (e.key === 'Enter')  handleSave();
                if (e.key === 'Escape') handleCancel();
              }}
              className="w-full bg-surface-container border border-secondary rounded-md px-sm py-xs text-body-md text-on-surface focus:outline-none focus:ring-2 focus:ring-secondary/30"
            />
            <div className="flex gap-sm">
              <button onClick={handleSave}   className="text-label-md text-on-primary bg-secondary px-sm py-xs rounded-md hover:opacity-90 transition-opacity">{saveLabel}</button>
              <button onClick={handleCancel} className="text-label-md text-error hover:bg-error-container/30 border border-error px-sm py-xs rounded-md transition-colors">{cancelLabel}</button>
            </div>
          </div>
        ) : (
          <div className="mt-xs flex items-center gap-sm">
            <dd className="text-body-md text-on-surface flex-1 wrap-break-word">{value}</dd>
            {editable && (
              <button
                onClick={() => setEditing(true)}
                aria-label={`${label}`}
                className="shrink-0 p-xs rounded-md text-on-surface-variant opacity-0 group-hover:opacity-100 hover:text-secondary hover:bg-surface-container transition-all"
              >
                <Icon name="edit" size={16} />
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Sub-component: EditableSelectField
   Inline editing via a <select> with neighbourhoods.
   ───────────────────────────────────────────── */
function EditableSelectField({ iconName, label, displayValue, currentName, onSave, saveLabel, cancelLabel }) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft]     = useState(currentName);

  function handleSave() {
    if (draft) {
      // Find the displayName for the chosen slug
      let chosen = displayValue;
      for (const zone of barrios) {
        const found = zone.neighbourhoods.find(n => n.name === draft);
        if (found) { chosen = found.displayName; break; }
      }
      onSave?.({ name: draft, displayName: chosen });
    }
    setEditing(false);
  }

  function handleCancel() {
    setDraft(currentName);
    setEditing(false);
  }

  return (
    <div className="flex items-start gap-md px-md py-sm group">
      <Icon name={iconName} size={18} className="text-secondary mt-xs shrink-0" />
      <div className="flex-1 min-w-0">
        <dt className="text-caption text-on-surface-variant uppercase tracking-wide">{label}</dt>

        {editing ? (
          <div className="mt-xs flex flex-col gap-xs">
            <select
              autoFocus
              value={draft}
              onChange={e => setDraft(e.target.value)}
              className="w-full bg-surface-container border border-secondary rounded-md px-sm py-xs text-body-md text-on-surface focus:outline-none focus:ring-2 focus:ring-secondary/30"
            >
              {barrios.map(zone => (
                <optgroup key={zone.zoneName} label={zone.zone}>
                  {zone.neighbourhoods.map(n => (
                    <option key={n.name} value={n.name}>{n.displayName}</option>
                  ))}
                </optgroup>
              ))}
            </select>
            <div className="flex gap-sm">
              <button onClick={handleSave}   className="text-label-md text-on-primary bg-secondary px-sm py-xs rounded-md hover:opacity-90 transition-opacity">{saveLabel}</button>
              <button onClick={handleCancel} className="text-label-md text-error hover:bg-error-container/30 border border-error px-sm py-xs rounded-md transition-colors">{cancelLabel}</button>
            </div>
          </div>
        ) : (
          <div className="mt-xs flex items-center gap-sm">
            <dd className="text-body-md text-on-surface flex-1 wrap-break-word">{displayValue}</dd>
            <button
              onClick={() => setEditing(true)}
              aria-label={`${label}`}
              className="shrink-0 p-xs rounded-md text-on-surface-variant opacity-0 group-hover:opacity-100 hover:text-secondary hover:bg-surface-container transition-all"
            >
              <Icon name="edit" size={16} />
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Organism: ProfileCard
   ───────────────────────────────────────────── */
/**
 * @param {{
 *   authUser: { name?: string, email?: string, picture?: string },
 *   profile: {
 *     name: string,
 *     email: string,
 *     createdAt: string,           // ISO-8601 Instant
 *     neighbourhood: { name: string, displayName: string }
 *   } | null,
 *   dict: object
 * }} props
 */
export default function ProfileCard({ authUser, profile, dict, lang }) {
  // If the backend does not respond, use Auth0 data as fallback
  const initialName  = profile?.name  ?? authUser?.name  ?? '—';
  const email        = profile?.email ?? authUser?.email ?? '—';
  const createdAt    = profile?.createdAt ?? null;
  const initialNeighbourhood = profile?.neighbourhood ?? null;

  const [name, setName]               = useState(initialName);
  const [neighbourhood, setNeighbourhood] = useState(initialNeighbourhood);

  const joinedDate = createdAt
    ? new Date(createdAt).toLocaleDateString(lang, { year: 'numeric', month: 'long', day: 'numeric' })
    : '—';

  const backendUnavailable = !profile;

  return (
    <div className="space-y-md">

      {/* Warning if the backend is unavailable */}
      {backendUnavailable && (
        <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
          <Icon name="warning" size={18} className="shrink-0" />
          {dict.backendError}
        </div>
      )}

      {/* ── Header ─────────────────────────────── */}
      <div className="hidden sm:block bg-surface-container-lowest rounded-md p-lg border border-outline-variant shadow-sm">
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-md">
          {/* Single avatar — lg (56 px) is comfortably visible on mobile
              and still professional at any larger viewport. */}
          <Avatar name={name} size="lg" />
          <div className="flex-1 min-w-0">
            <h1 className="text-h2 text-primary leading-tight">{name}</h1>
            <p className="text-body-md text-on-surface-variant mt-xs">{email}</p>
          </div>
        </div>
      </div>

      {/* ── Personal data ─────────────────────── */}
      <section className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm overflow-hidden">
        <div className="px-md py-sm bg-surface-container-low border-b border-outline-variant">
          <h2 className="text-h3 text-primary">{dict.title}</h2>
        </div>
        <dl className="divide-y divide-outline-variant">

          {/* Name — editable */}
          <EditableField
            iconName="person"
            label={dict.name}
            value={name}
            editable
            onSave={setName}
            saveLabel={dict.save}
            cancelLabel={dict.cancel}
          />

          {/* Email — read only */}
          <EditableField
            iconName="email"
            label={dict.email}
            value={email}
            saveLabel={dict.save}
            cancelLabel={dict.cancel}
          />

          {/* Registration date — read only */}
          <EditableField
            iconName="calendar_today"
            label={dict.since}
            value={joinedDate}
            saveLabel={dict.save}
            cancelLabel={dict.cancel}
          />

          {/* Neighbourhood — editable with select */}
          {neighbourhood ? (
            <EditableSelectField
              iconName="location_city"
              label={dict.neighbourhood}
              displayValue={neighbourhood.displayName}
              currentName={neighbourhood.name}
              onSave={setNeighbourhood}
              saveLabel={dict.save}
              cancelLabel={dict.cancel}
            />
          ) : (
            <EditableField
              iconName="location_city"
              label={dict.neighbourhood}
              value="—"
              saveLabel={dict.save}
              cancelLabel={dict.cancel}
            />
          )}

        </dl>
      </section>

      {/* ── Logout button (mobile and medium) ─────────────────────── */}
      <a
        href="/auth/logout"
        className="lg:hidden w-full flex items-center justify-center gap-md px-md py-sm text-error border-2 border-error rounded-md hover:bg-error-container/40 transition-colors duration-150 text-label-md font-medium"
      >
        <Icon name="logout" size={22} />
        <span>{dict.logout}</span>
      </a>

    </div>
  );
}
