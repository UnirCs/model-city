'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import { DefaultLangFields, TranslationSections } from '@modelcity/core/components/molecules/MultilingualFields';
import { saveEvent } from '@modelcity/leisure/lib/actions/events';

const MAX_PHOTOS = 3;

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';
const INPUT_ERROR_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-error';

const EVENT_TYPES = [
  'MUSIC',
  'NIGHTLIFE',
  'PERFORMING_ARTS',
  'HOBBIES',
  'BUSINESS',
  'FOOD_AND_DRINK',
  'OTHER',
];

function Field({ label, error, children }) {
  return (
    <div className="flex flex-col gap-xs">
      <label className="text-label-md text-on-surface font-medium">{label}</label>
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
 * Converts a backend ISO datetime to the value expected by `<input type="datetime-local">`.
 */
function toLocalInput(iso) {
  if (!iso) return '';
  // Trim seconds / timezone — input expects "YYYY-MM-DDTHH:mm"
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/**
 * Organism: EventForm
 *
 * Shared form to create (POST) or fully update (PUT) a leisure event.
 * Receives the catalogue of available city places so the user can bind the
 * event to one of them (events are always anchored to an existing place).
 *
 * @param {{
 *   mode: 'create' | 'edit',
 *   event?: object,
 *   places: Array<{ id: number, name: string }>,
 *   t: object,                          // dict.leisure.eventForm
 *   typeLabels: Record<string, string>, // dict.leisure.eventTypes
 *   lang: string,
 * }} props
 */
export default function EventForm({ mode, event, places, t, typeLabels, lang }) {
  const router = useRouter();
  const goBack = useLocalizedBack();
  const isEdit = mode === 'edit';

  const initName = () => {
    const tr = event?.translations?.name;
    return { es: tr?.es ?? event?.name ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };
  const initDesc = () => {
    const tr = event?.translations?.description;
    return { es: tr?.es ?? event?.description ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };

  const [placeId, setPlaceId]         = useState(event?.placeId ? String(event.placeId) : '');
  const [name, setName]               = useState(initName);
  const [description, setDescription] = useState(initDesc);
  const [eventType, setEventType]     = useState(event?.eventType ?? '');
  const [requiresTicket, setRequiresTicket] = useState(event?.requiresTicket ?? true);
  const [paid, setPaid]               = useState(event?.paid ?? false);
  const [price, setPrice]             = useState(event?.price != null ? String(event.price) : '0');
  const [currency, setCurrency]       = useState(event?.currency ?? 'EUR');
  const [capacity, setCapacity]       = useState(event?.capacity != null ? String(event.capacity) : '');
  const [startsAt, setStartsAt]       = useState(toLocalInput(event?.startsAt));
  const [endsAt, setEndsAt]           = useState(toLocalInput(event?.endsAt));
  const [photos, setPhotos] = useState(() => {
    const initial = event?.photoUrls ?? [];
    return initial.length > 0 ? [...initial] : [''];
  });

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [errors, setErrors] = useState({});

  const addPhoto    = () => photos.length < MAX_PHOTOS && setPhotos((p) => [...p, '']);
  const removePhoto = (i) => setPhotos((prev) => prev.filter((_, idx) => idx !== i));
  const updatePhoto = (i, v) => setPhotos((prev) => prev.map((p, idx) => (idx === i ? v : p)));

  function validate() {
    const e = {};
    if (!placeId)            e.placeId     = t.errors.placeRequired;
    if (!name.es.trim())     e.name        = t.errors.nameRequired;
    if (!description.es.trim()) e.description = t.errors.descriptionRequired;
    if (!eventType)          e.eventType   = t.errors.typeRequired;
    if (currency.trim().length !== 3) e.currency = t.errors.currencyInvalid;
    if (capacity && Number(capacity) <= 0) e.capacity = t.errors.capacityInvalid;
    if (paid && Number(price) <= 0) e.price = t.errors.priceInvalid;
    if (!startsAt) e.startsAt = t.errors.startRequired;
    if (!endsAt)   e.endsAt   = t.errors.endRequired;
    if (startsAt && endsAt && new Date(endsAt) <= new Date(startsAt)) {
      e.endsAt = t.errors.invalidRange;
    }
    const cleanPhotos = photos.map((p) => p.trim()).filter(Boolean);
    if (cleanPhotos.length > MAX_PHOTOS) e.photos = t.errors.photosMax;
    return e;
  }

  async function handleSubmit(ev) {
    ev.preventDefault();
    const v = validate();
    if (Object.keys(v).length > 0) { setErrors(v); return; }
    setErrors({});
    setSubmitError('');
    setSubmitting(true);

    const payload = {
      placeId:        Number(placeId),
      name:           { es: name.es.trim(), en: name.en.trim(), fr: name.fr.trim() },
      description:    { es: description.es.trim(), en: description.en.trim(), fr: description.fr.trim() },
      eventType,
      requiresTicket,
      paid,
      price:          Number(paid ? price : 0).toFixed(2),
      currency:       currency.trim().toUpperCase(),
      capacity:       capacity ? Number(capacity) : null,
      startsAt:       new Date(startsAt).toISOString(),
      endsAt:         new Date(endsAt).toISOString(),
      photoUrls:      photos.map((p) => p.trim()).filter(Boolean),
    };

    try {
      const result = await saveEvent(payload, isEdit ? event.id : null);
      if (result?.error) { setSubmitError(t.errors.serverError); return; }
      const targetId = result?.id ?? event?.id;
      router.push(targetId
        ? `/${lang}/events/${targetId}`
        : `/${lang}/events`);
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
  ];

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-md">

        {/* LEFT — main fields */}
        <div className="flex flex-col gap-md">
          <Field label={t.placeLabel} error={errors.placeId}>
            <div className="relative">
              <select
                value={placeId}
                onChange={(e) => { setPlaceId(e.target.value); setErrors((p) => ({ ...p, placeId: '' })); }}
                className={`appearance-none pr-xl ${errors.placeId ? INPUT_ERROR_CLS : INPUT_CLS}`}
              >
                <option value="">{t.placePlaceholder}</option>
                {places.map((pl) => (
                  <option key={pl.id} value={pl.id}>{pl.name}</option>
                ))}
              </select>
              <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                <Icon name="expand_more" size={20} />
              </span>
            </div>
          </Field>

          <DefaultLangFields fields={translatableFields} />

          <Field label={t.typeLabel} error={errors.eventType}>
            <div className="relative">
              <select
                value={eventType}
                onChange={(e) => { setEventType(e.target.value); setErrors((p) => ({ ...p, eventType: '' })); }}
                className={`appearance-none pr-xl ${errors.eventType ? INPUT_ERROR_CLS : INPUT_CLS}`}
              >
                <option value="">{t.typePlaceholder}</option>
                {EVENT_TYPES.map((code) => (
                  <option key={code} value={code}>{typeLabels?.[code] ?? code}</option>
                ))}
              </select>
              <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
                <Icon name="expand_more" size={20} />
              </span>
            </div>
          </Field>
        </div>

        {/* RIGHT — schedule, pricing, photos */}
        <div className="flex flex-col gap-md">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-md">
            <Field label={t.startsAtLabel} error={errors.startsAt}>
              <input
                type="datetime-local"
                value={startsAt}
                onChange={(e) => { setStartsAt(e.target.value); setErrors((p) => ({ ...p, startsAt: '' })); }}
                className={errors.startsAt ? INPUT_ERROR_CLS : INPUT_CLS}
              />
            </Field>
            <Field label={t.endsAtLabel} error={errors.endsAt}>
              <input
                type="datetime-local"
                value={endsAt}
                onChange={(e) => { setEndsAt(e.target.value); setErrors((p) => ({ ...p, endsAt: '' })); }}
                className={errors.endsAt ? INPUT_ERROR_CLS : INPUT_CLS}
              />
            </Field>
          </div>

          <Field label={t.capacityLabel} error={errors.capacity}>
            <input
              type="number"
              min={1}
              value={capacity}
              onChange={(e) => { setCapacity(e.target.value); setErrors((p) => ({ ...p, capacity: '' })); }}
              placeholder={t.capacityPlaceholder}
              className={errors.capacity ? INPUT_ERROR_CLS : INPUT_CLS}
            />
          </Field>

          <fieldset className="flex flex-col gap-sm border border-outline-variant rounded-md p-md">
            <legend className="px-xs text-label-md text-on-surface font-medium">{t.ticketingLabel}</legend>

            <label className="flex items-center gap-sm text-body-md text-on-surface">
              <input
                type="checkbox"
                checked={requiresTicket}
                onChange={(e) => setRequiresTicket(e.target.checked)}
              />
              {t.requiresTicketLabel}
            </label>
            <label className="flex items-center gap-sm text-body-md text-on-surface">
              <input
                type="checkbox"
                checked={paid}
                onChange={(e) => setPaid(e.target.checked)}
                disabled={!requiresTicket}
              />
              {t.paidLabel}
            </label>

            {paid && (
              <div className="grid grid-cols-2 gap-md mt-sm">
                <Field label={t.priceLabel} error={errors.price}>
                  <input
                    type="number"
                    min={0}
                    step="0.01"
                    value={price}
                    onChange={(e) => { setPrice(e.target.value); setErrors((p) => ({ ...p, price: '' })); }}
                    className={errors.price ? INPUT_ERROR_CLS : INPUT_CLS}
                  />
                </Field>
                <Field label={t.currencyLabel} error={errors.currency}>
                  <input
                    type="text"
                    value={currency}
                    maxLength={3}
                    onChange={(e) => { setCurrency(e.target.value.toUpperCase()); setErrors((p) => ({ ...p, currency: '' })); }}
                    className={`tabular-nums uppercase ${errors.currency ? INPUT_ERROR_CLS : INPUT_CLS}`}
                  />
                </Field>
              </div>
            )}
          </fieldset>

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

