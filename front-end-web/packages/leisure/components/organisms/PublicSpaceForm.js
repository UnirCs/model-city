'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import LocationPickerMapClient from '@modelcity/core/components/molecules/LocationPickerMapClient';
import { DefaultLangFields, TranslationSections } from '@modelcity/core/components/molecules/MultilingualFields';
import { savePublicSpace } from '@modelcity/leisure/lib/actions/publicSpaces';

const MAX_PHOTOS = 3;

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';

/**
 * Organism: PublicSpaceForm
 *
 * Shared form to create (POST) or fully update (PUT) a public space.
 * Two columns: textual fields on the left, map picker on the right.
 *
 * @param {{
 *   mode: 'create' | 'edit',
 *   space?: object,
 *   t: object,         // leisure.spaceForm dictionary
 *   lang: string,
 * }} props
 */
export default function PublicSpaceForm({ mode, space, t, lang }) {
  const router = useRouter();
  const goBack = useLocalizedBack();
  const isEdit = mode === 'edit';

  const initName = () => {
    const tr = space?.translations?.name;
    return { es: tr?.es ?? space?.name ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };
  const initDesc = () => {
    const tr = space?.translations?.description;
    return { es: tr?.es ?? space?.description ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };
  const initAddr = () => {
    const tr = space?.translations?.address;
    return { es: tr?.es ?? space?.address ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };

  const [name, setName]               = useState(initName);
  const [description, setDescription] = useState(initDesc);
  const [address, setAddress]         = useState(initAddr);
  const [photos, setPhotos] = useState(() => {
    const initial = space?.photoUrls ?? [];
    return initial.length > 0 ? [...initial] : [''];
  });
  const [latitude, setLatitude]   = useState(space?.latitude ?? null);
  const [longitude, setLongitude] = useState(space?.longitude ?? null);

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [errors, setErrors] = useState({});

  const addPhoto    = () => {
    if (photos.length >= MAX_PHOTOS) return;
    setPhotos((prev) => [...prev, '']);
  };
  const removePhoto = (i) => setPhotos((prev) => prev.filter((_, idx) => idx !== i));
  const updatePhoto = (i, val) =>
    setPhotos((prev) => prev.map((p, idx) => (idx === i ? val : p)));

  function handleMapChange({ latitude: lat, longitude: lng }) {
    setLatitude(lat);
    setLongitude(lng);
    setErrors((p) => ({ ...p, latitude: '', longitude: '' }));
  }

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
      name:        { es: name.es.trim(), en: name.en.trim(), fr: name.fr.trim() },
      description: { es: description.es.trim(), en: description.en.trim(), fr: description.fr.trim() },
      address:     { es: address.es.trim(), en: address.en.trim(), fr: address.fr.trim() },
      latitude,
      longitude,
      photoUrls:   photos.map((p) => p.trim()).filter(Boolean),
    };

    try {
      const result = await savePublicSpace(payload, isEdit ? space.id : null);
      if (result?.error) {
        setSubmitError(t.errors.serverError);
        return;
      }
      const targetId = result?.id ?? space?.id;
      const target = targetId
        ? `/${lang}/sports-spaces/${targetId}`
        : `/${lang}/sports-spaces`;
      router.push(target);
      router.refresh();
    } catch {
      setSubmitError(t.errors.serverError);
    } finally {
      setSubmitting(false);
    }
  }

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
  ];

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-md">

        {/* LEFT — fields */}
        <div className="flex flex-col gap-md">
          <DefaultLangFields fields={translatableFields} />

          <div className="flex flex-col gap-sm">
            <div className="flex items-center justify-between">
              <span className="text-label-md text-on-surface font-medium">{t.photosLabel}</span>
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

        {/* RIGHT — map picker */}
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
        <Button type="button" variant="outline-error" onClick={goBack} disabled={submitting}>
          {t.cancel}
        </Button>
        <Button type="submit" variant="primary" disabled={submitting}>
          {submitting ? t.submitting : isEdit ? t.submitEdit : t.submitCreate}
        </Button>
      </div>
    </form>
  );
}

