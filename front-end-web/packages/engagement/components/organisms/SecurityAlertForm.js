'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import LocationPickerMapClient from '@modelcity/core/components/molecules/LocationPickerMapClient';
import { DefaultLangFields, TranslationSections } from '@modelcity/core/components/molecules/MultilingualFields';
import { createSecurityAlert } from '@modelcity/engagement/lib/actions/securityAlerts';
import { SEVERITIES, severityVisual } from '@modelcity/engagement/lib/security/severity';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';
const INPUT_ERROR_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-error';

function Field({ label, hint, error, children }) {
  return (
    <div className="flex flex-col gap-xs">
      <label className="text-label-md text-on-surface font-medium">{label}</label>
      {hint && <p className="text-caption text-on-surface-variant">{hint}</p>}
      {children}
      {error && (
        <p className="text-caption text-error flex items-center gap-xs">
          <Icon name="error" size={14} />
          {error}
        </p>
      )}
    </div>
  );
}

/**
 * Organism: SecurityAlertForm
 *
 * Two-column form to create a new security alert. Left column hosts the
 * descriptive / metadata fields (severity, description, zone, neighbourhood,
 * expiration). Right column holds the interactive map picker that drives the
 * latitude / longitude values.
 *
 * @param {{
 *   t:          object,
 *   tSeverity:  Record<string, string>,
 *   barrios:    Array<{
 *     id: number,
 *     zone: string,
 *     neighbourhoods: Array<{ id: number, displayName: string }>,
 *   }>,
 *   lang:       string,
 * }} props
 */
export default function SecurityAlertForm({ t, tSeverity, barrios, lang }) {
  const router = useRouter();
  const goBack = useLocalizedBack();

  const [severity, setSeverity]         = useState('');
  const [title, setTitle]               = useState({ es: '', en: '', fr: '' });
  const [description, setDescription]   = useState({ es: '', en: '', fr: '' });
  const [zoneId, setZoneId]             = useState('');
  const [neighbourhoodId, setNeighbourhoodId] = useState('');
  const [expiresAt, setExpiresAt]       = useState('');
  const [latitude, setLatitude]         = useState(null);
  const [longitude, setLongitude]       = useState(null);

  const [submitting, setSubmitting]     = useState(false);
  const [submitError, setSubmitError]   = useState('');
  const [errors, setErrors]             = useState({});

  /** Neighbourhood options scoped to the currently-selected zone. */
  const neighbourhoodOptions = useMemo(() => {
    if (!zoneId) return [];
    const zone = barrios.find((z) => z.id === Number(zoneId));
    return zone?.neighbourhoods ?? [];
  }, [zoneId, barrios]);

  /** Minimum date allowed in the picker — tomorrow (YYYY-MM-DD). */
  const minDate = useMemo(() => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().slice(0, 10);
  }, []);

  function handleZoneChange(value) {
    setZoneId(value);
    setNeighbourhoodId('');
    setErrors((p) => ({ ...p, zoneId: '' }));
  }

  function handleMapChange({ latitude: lat, longitude: lng }) {
    setLatitude(lat);
    setLongitude(lng);
    setErrors((p) => ({ ...p, latitude: '', longitude: '' }));
  }

  function validate() {
    const e = {};
    if (!severity)            e.severity    = t.errors.severityRequired;
    if (!title.es.trim())     e.title       = t.errors.titleRequired;
    if (!description.es.trim()) e.description = t.errors.descriptionRequired;
    if (!zoneId)              e.zoneId      = t.errors.zoneRequired;
    if (latitude == null)     e.latitude    = t.errors.latitudeRequired;
    if (longitude == null)    e.longitude   = t.errors.longitudeRequired;
    if (!expiresAt) {
      e.expiresAt = t.errors.expiresAtRequired;
    } else {
      // Compare only by day — picker accepts today onwards but we want future.
      const picked = new Date(`${expiresAt}T23:59:59`);
      if (picked.getTime() <= Date.now()) {
        e.expiresAt = t.errors.expiresAtPast;
      }
    }
    return e;
  }

  /** Converts the picked YYYY-MM-DD to an ISO OffsetDateTime at end of day. */
  function buildExpiresAtIso() {
    // End of the selected day in the user's local time zone, then ISO-stringified
    // (Date#toISOString always emits a `Z` offset, which is a valid OffsetDateTime).
    const local = new Date(`${expiresAt}T23:59:59`);
    return local.toISOString();
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const v = validate();
    if (Object.keys(v).length > 0) {
      setErrors(v);
      return;
    }

    setErrors({});
    setSubmitError('');
    setSubmitting(true);

    const payload = {
      severity,
      title: { es: title.es.trim(), en: title.en.trim(), fr: title.fr.trim() },
      description: { es: description.es.trim(), en: description.en.trim(), fr: description.fr.trim() },
      latitude,
      longitude,
      zoneId: Number(zoneId),
      neighbourhoodId: neighbourhoodId ? Number(neighbourhoodId) : null,
      expiresAt: buildExpiresAtIso(),
    };

    try {
      const result = await createSecurityAlert(payload);
      if (result?.error) {
        setSubmitError(t.errors.serverError);
        return;
      }
      router.push(`/${lang}/security/alerts/manage`);
      router.refresh();
    } catch {
      setSubmitError(t.errors.serverError);
    } finally {
      setSubmitting(false);
    }
  }

  const translatableFields = [
    {
      key: 'title',
      label: t.titleLabel,
      placeholder: t.titlePlaceholder,
      error: errors.title,
      as: 'input',
      value: title,
      onChange: (l, v) => { setTitle((p) => ({ ...p, [l]: v })); setErrors((p) => ({ ...p, title: '' })); },
    },
    {
      key: 'description',
      label: t.descriptionLabel,
      placeholder: t.descriptionPlaceholder,
      error: errors.description,
      as: 'textarea',
      rows: 3,
      value: description,
      onChange: (l, v) => { setDescription((p) => ({ ...p, [l]: v })); setErrors((p) => ({ ...p, description: '' })); },
    },
  ];

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-md">

        {/* ── LEFT — fields ─────────────────────────────────── */}
        <div className="flex flex-col gap-md">
          <Field label={t.severityLabel} error={errors.severity}>
            <div className="relative">
              <select
                value={severity}
                onChange={(e) => { setSeverity(e.target.value); setErrors((p) => ({ ...p, severity: '' })); }}
                className={`appearance-none pr-xl ${errors.severity ? INPUT_ERROR_CLS : INPUT_CLS}`}
              >
                <option value="">{t.severityPlaceholder}</option>
                {SEVERITIES.map((code) => (
                  <option key={code} value={code}>{tSeverity[code]}</option>
                ))}
              </select>
              <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                <Icon name="expand_more" size={20} />
              </span>
            </div>
            {severity && (
              <span className={`mt-xs inline-flex items-center gap-xs px-sm py-xs rounded-full text-caption font-bold uppercase tracking-wide self-start ${severityVisual(severity).chipClass}`}>
                <Icon name={severityVisual(severity).icon} size={14} />
                {tSeverity[severity]}
              </span>
            )}
          </Field>

          <DefaultLangFields fields={translatableFields} />

          <div className="flex flex-col gap-md">
            <Field label={t.zoneLabel} error={errors.zoneId}>
              <div className="relative">
                <select
                  value={zoneId}
                  onChange={(e) => handleZoneChange(e.target.value)}
                  className={`appearance-none pr-xl ${errors.zoneId ? INPUT_ERROR_CLS : INPUT_CLS}`}
                >
                  <option value="">{t.zonePlaceholder}</option>
                  {barrios.map((z) => (
                    <option key={z.id} value={z.id}>{z.zone}</option>
                  ))}
                </select>
                <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                  <Icon name="expand_more" size={20} />
                </span>
              </div>
            </Field>

            <Field label={t.neighbourhoodLabel}>
              <div className="relative">
                <select
                  value={neighbourhoodId}
                  onChange={(e) => setNeighbourhoodId(e.target.value)}
                  disabled={!zoneId}
                  className={`appearance-none pr-xl ${INPUT_CLS} disabled:opacity-50`}
                >
                  <option value="">{zoneId ? t.neighbourhoodAll : t.neighbourhoodPlaceholder}</option>
                  {neighbourhoodOptions.map((n) => (
                    <option key={n.id} value={n.id}>{n.displayName}</option>
                  ))}
                </select>
                <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                  <Icon name="expand_more" size={20} />
                </span>
              </div>
            </Field>
          </div>

          <Field label={t.expiresAtLabel} hint={t.expiresAtHint} error={errors.expiresAt}>
            <input
              type="date"
              value={expiresAt}
              min={minDate}
              onChange={(e) => { setExpiresAt(e.target.value); setErrors((p) => ({ ...p, expiresAt: '' })); }}
              className={errors.expiresAt ? INPUT_ERROR_CLS : INPUT_CLS}
            />
          </Field>
        </div>

        {/* ── RIGHT — map picker + coordinate readout ─────── */}
        <div className="flex flex-col gap-sm">
          <h3 className="text-h3 text-primary flex items-center gap-sm">
            <Icon name="my_location" fill size={20} className="text-secondary" />
            {t.mapPickerTitle}
          </h3>
          <p className="text-caption text-on-surface-variant">{t.coordinatesHint}</p>

          <LocationPickerMapClient
            latitude={latitude}
            longitude={longitude}
            onChange={handleMapChange}
            mapTitle={t.mapPickerTitle}
          />

          {(errors.latitude || errors.longitude) && (
            <p className="text-caption text-error flex items-center gap-xs">
              <Icon name="error" size={14} />
              {errors.latitude || errors.longitude}
            </p>
          )}
        </div>
      </div>

      <TranslationSections fields={translatableFields} />

      {submitError && (
        <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
          <Icon name="error" size={18} className="shrink-0" />
          {submitError}
        </div>
      )}

      <div className="flex items-center justify-end gap-md">
        <Button
          type="button"
          variant="outline-error"
          onClick={goBack}
          disabled={submitting}
        >
          {t.cancel}
        </Button>
        <Button type="submit" variant="primary" disabled={submitting}>
          {submitting ? t.submitting : t.submitCreate}
        </Button>
      </div>
    </form>
  );
}








