'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import LocationPickerMapClient from '@modelcity/core/components/molecules/LocationPickerMapClient';
import { DefaultLangFields, TranslationSections } from '@modelcity/core/components/molecules/MultilingualFields';
import { saveCityPlace } from '@modelcity/leisure/lib/actions/cityPlaces';

const MAX_PHOTOS = 3;

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';

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
 * Organism: CityPlaceForm
 *
 * Shared form for creating (POST) or updating (PUT) a city place.
 *
 * Layout: two columns. Left column holds the textual / metadata fields.
 * Right column holds the interactive map picker that drives the latitude /
 * longitude state. The raw coordinates are not shown to the user.
 *
 * @param {{
 *   mode:  'create' | 'edit',
 *   place?: {
 *     id: number | string,
 *     name?: string,
 *     latitude?: number | null,
 *     longitude?: number | null,
 *     description?: string,
 *     address?: string,
 *     photoUrls?: string[],
 *     accessInfo?: string,
 *     accessibilityInfo?: string,
 *     category?: string,
 *     visitDurationMinutes?: number | null,
 *   },
 *   t: object,
 *   tCategories: Record<string, string>,
 *   lang: string,
 * }} props
 */
export default function CityPlaceForm({ mode, place, t, tCategories, lang }) {
  const router = useRouter();
  const goBack = useLocalizedBack();
  const isEdit = mode === 'edit';

  const initField = (field, fallback = '') => {
    const tr = place?.translations?.[field];
    return { es: tr?.es ?? place?.[field] ?? fallback, en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };

  const [name, setName]               = useState(() => initField('name'));
  const [description, setDescription] = useState(() => initField('description'));
  const [address, setAddress]         = useState(() => initField('address'));
  const [category, setCategory]       = useState(place?.category ?? '');
  const [visitDuration, setVisitDuration] = useState(
    place?.visitDurationMinutes != null ? String(place.visitDurationMinutes) : '',
  );
  const [accessInfo, setAccessInfo]   = useState(() => initField('accessInfo'));
  const [accessibilityInfo, setAccessibilityInfo] = useState(() => initField('accessibilityInfo'));
  const [photos, setPhotos] = useState(() => {
    const initial = place?.photoUrls ?? [];
    return initial.length > 0 ? [...initial] : [''];
  });

  const [latitude, setLatitude]   = useState(
    place?.latitude != null ? place.latitude : null,
  );
  const [longitude, setLongitude] = useState(
    place?.longitude != null ? place.longitude : null,
  );

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [errors, setErrors] = useState({});

  /* ── Photo list helpers ──────────────────────────────────── */
  const addPhoto    = () => {
    if (photos.length >= MAX_PHOTOS) return;
    setPhotos((prev) => [...prev, '']);
  };
  const removePhoto = (i) => setPhotos((prev) => prev.filter((_, idx) => idx !== i));
  const updatePhoto = (i, val) =>
    setPhotos((prev) => prev.map((p, idx) => (idx === i ? val : p)));

  /* ── Map picker ──────────────────────────────────────────── */
  function handleMapChange({ latitude: lat, longitude: lng }) {
    setLatitude(lat);
    setLongitude(lng);
    setErrors((p) => ({ ...p, latitude: '', longitude: '' }));
  }

  /* ── Validation ──────────────────────────────────────────── */
  function validate() {
    const e = {};
    if (!name.es.trim())     e.name        = t.errors.nameRequired;
    if (!description.es.trim()) e.description = t.errors.descriptionRequired;
    if (latitude == null)    e.latitude    = t.errors.latitudeRequired;
    if (longitude == null)   e.longitude   = t.errors.longitudeRequired;
    const cleanPhotos = photos.map((p) => p.trim()).filter(Boolean);
    if (cleanPhotos.length > MAX_PHOTOS) e.photos = t.errors.photosMax;
    return e;
  }

  /* ── Submit ──────────────────────────────────────────────── */
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

    const visitNum = visitDuration.trim() === '' ? null : Number(visitDuration);
    const payload = {
      name:        { es: name.es.trim(), en: name.en.trim(), fr: name.fr.trim() },
      latitude,
      longitude,
      description: { es: description.es.trim(), en: description.en.trim(), fr: description.fr.trim() },
      address:     { es: address.es.trim(), en: address.en.trim(), fr: address.fr.trim() },
      photoUrls:   photos.map((p) => p.trim()).filter(Boolean),
      accessInfo:  { es: accessInfo.es.trim(), en: accessInfo.en.trim(), fr: accessInfo.fr.trim() },
      accessibilityInfo: { es: accessibilityInfo.es.trim(), en: accessibilityInfo.en.trim(), fr: accessibilityInfo.fr.trim() },
      category:    category || null,
      visitDurationMinutes:
        Number.isFinite(visitNum) ? visitNum : null,
    };

    try {
      const result = await saveCityPlace(payload, isEdit ? place.id : null);
      if (result?.error) {
        setSubmitError(t.errors.serverError);
        return;
      }
      const targetId = result?.id ?? place?.id;
      const target = targetId
        ? `/${lang}/tourism/locations/${targetId}`
        : `/${lang}/tourism/locations`;
      router.push(target);
      router.refresh();
    } catch {
      setSubmitError(t.errors.serverError);
    } finally {
      setSubmitting(false);
    }
  }

  /* ── Render ──────────────────────────────────────────────── */
  const categoryCodes = Object.keys(tCategories);

  const translatableFields = [
    {
      key: 'name',
      label: t.nameLabel,
      placeholder: t.namePlaceholder,
      error: errors.name,
      as: 'input',
      value: name,
      onChange: (l, v) => { setName((p) => ({ ...p, [l]: v })); setErrors((p) => ({ ...p, name: '' })); },
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
    {
      key: 'address',
      label: t.addressLabel,
      placeholder: t.addressPlaceholder,
      as: 'input',
      value: address,
      onChange: (l, v) => setAddress((p) => ({ ...p, [l]: v })),
    },
    {
      key: 'accessInfo',
      label: t.accessInfoLabel,
      placeholder: t.accessInfoPlaceholder,
      as: 'textarea',
      rows: 2,
      value: accessInfo,
      onChange: (l, v) => setAccessInfo((p) => ({ ...p, [l]: v })),
    },
    {
      key: 'accessibilityInfo',
      label: t.accessibilityInfoLabel,
      placeholder: t.accessibilityInfoPlaceholder,
      as: 'textarea',
      rows: 2,
      value: accessibilityInfo,
      onChange: (l, v) => setAccessibilityInfo((p) => ({ ...p, [l]: v })),
    },
  ];

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-md">

        {/* ── LEFT — fields ─────────────────────────────────── */}
        <div className="flex flex-col gap-md">

          <DefaultLangFields fields={translatableFields} />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-md">
            <Field label={t.categoryLabel}>
              <div className="relative">
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className={`appearance-none pr-xl ${INPUT_CLS}`}
                >
                  <option value="">{t.categoryPlaceholder}</option>
                  {categoryCodes.map((code) => (
                    <option key={code} value={code}>{tCategories[code]}</option>
                  ))}
                </select>
                <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                  <Icon name="expand_more" size={20} />
                </span>
              </div>
            </Field>

            <Field label={t.visitDurationLabel}>
              <input
                type="number"
                min={0}
                value={visitDuration}
                onChange={(e) => setVisitDuration(e.target.value)}
                placeholder={t.visitDurationPlaceholder}
                className={INPUT_CLS}
              />
            </Field>
          </div>

          {/* Photos */}
          <div className="flex flex-col gap-sm">
            <div className="flex items-center justify-between">
              <span className="text-label-md text-on-surface font-medium">
                {t.photosLabel}
              </span>
              <button
                type="button"
                onClick={addPhoto}
                disabled={photos.length >= MAX_PHOTOS}
                className="flex items-center gap-xs text-label-sm text-secondary hover:underline disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <Icon name="add_circle" size={16} />
                {t.addPhoto}
              </button>
            </div>
            <p className="text-caption text-on-surface-variant">{t.photosHint}</p>

            {/* Options group — left-border accent per citizen-services-rules */}
            <div className="pl-md border-l-2 border-secondary flex flex-col gap-sm pr-sm">
              {photos.map((url, i) => (
                <div key={i} className="flex items-center gap-sm">
                  <input
                    type="url"
                    value={url}
                    onChange={(e) => updatePhoto(i, e.target.value)}
                    placeholder={t.photoPlaceholder}
                    className={`flex-1 ${INPUT_CLS}`}
                  />
                  {photos.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removePhoto(i)}
                      className="shrink-0 rounded-full p-xs hover:bg-error-container text-error transition-colors"
                      aria-label={t.removePhoto}
                    >
                      <Icon name="remove_circle" size={18} />
                    </button>
                  )}
                </div>
              ))}
            </div>

            {errors.photos && (
              <p className="text-caption text-error flex items-center gap-xs">
                <Icon name="error" size={14} />
                {errors.photos}
              </p>
            )}
          </div>

        </div>

        {/* ── RIGHT — map picker + coordinates readout ────── */}
        <div className="flex flex-col gap-sm">
          <h3 className="text-h3 text-primary flex items-center gap-sm">
            <Icon name="my_location" fill size={20} className="text-secondary" />
            {t.mapPickerTitle}
          </h3>
          <p className="text-caption text-on-surface-variant">
            {t.coordinatesHint}
          </p>

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

      {/* ── Submit feedback + actions ────────────────────────── */}
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
          {submitting
            ? t.submitting
            : isEdit
              ? t.submitEdit
              : t.submitCreate}
        </Button>
      </div>
    </form>
  );
}


