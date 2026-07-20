'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import RouteMapClient from '@modelcity/leisure/components/molecules/RouteMapClient';
import { DefaultLangFields, TranslationSections } from '@modelcity/core/components/molecules/MultilingualFields';
import { categoryIcon, categoryLabel } from '@modelcity/leisure/lib/utils/format';
import { saveCityRoute } from '@modelcity/leisure/lib/actions/cityRoutes';

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
 * Organism: CityRouteForm
 *
 * Shared form for creating (POST) or updating (PUT) a city route.
 *
 * Layout: two columns. Left column holds the form fields and the two place
 * pickers ("available" + "selected"). Right column holds a live map preview
 * that re-renders every time the selected places change.
 *
 * The "selected" list is capped at 6 entries and supports manual reordering
 * via up/down arrows, since the backend expects the ids in itinerary order.
 *
 * @param {{
 *   mode:    'create' | 'edit',
 *   route?:  {
 *     id: number | string,
 *     name?: string,
 *     description?: string,
 *     targetAudience?: string,
 *     imageUrl?: string | null,
 *     estimatedDurationMinutes?: number | null,
 *     cityPlaces?: Array<{ id: number, name: string, latitude: number, longitude: number, category?: string }>,
 *   },
 *   availablePlaces: Array<{
 *     id: number,
 *     name: string,
 *     latitude: number,
 *     longitude: number,
 *     category: string,
 *   }>,
 *   t: object,
 *   tCategories: Record<string, string>,
 *   lang: string,
 * }} props
 */
export default function CityRouteForm({
  mode,
  route,
  availablePlaces,
  t,
  tCategories,
  lang,
}) {
  const router = useRouter();
  const goBack = useLocalizedBack();
  const isEdit = mode === 'edit';
  const MAX_PLACES = t.max ?? 6;

  const initName = () => {
    const tr = route?.translations?.name;
    return { es: tr?.es ?? route?.name ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };
  const initDesc = () => {
    const tr = route?.translations?.description;
    return { es: tr?.es ?? route?.description ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };

  /* ── Form state ──────────────────────────────────────────── */
  const [name, setName]                   = useState(initName);
  const [description, setDescription]     = useState(initDesc);
  const [targetAudience, setTargetAudience] = useState(route?.targetAudience ?? '');
  const [imageUrl, setImageUrl]           = useState(route?.imageUrl ?? '');
  const [duration, setDuration]           = useState(
    route?.estimatedDurationMinutes != null ? String(route.estimatedDurationMinutes) : '',
  );

  /* ── Place picker state ──────────────────────────────────── */
  // We keep a denormalised list of full place objects so we can render the
  // map preview and rich list items without extra lookups.
  const placeIndex = useMemo(() => {
    const map = new Map();
    availablePlaces.forEach((p) => map.set(p.id, p));
    (route?.cityPlaces ?? []).forEach((p) => {
      if (!map.has(p.id)) map.set(p.id, p);
    });
    return map;
  }, [availablePlaces, route]);

  const initialSelected = (route?.cityPlaces ?? [])
    .map((p) => placeIndex.get(p.id))
    .filter(Boolean);

  const [selected, setSelected] = useState(initialSelected);

  const selectedIds = useMemo(
    () => new Set(selected.map((p) => p.id)),
    [selected],
  );

  const available = useMemo(
    () => availablePlaces.filter((p) => !selectedIds.has(p.id)),
    [availablePlaces, selectedIds],
  );

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [errors, setErrors] = useState({});

  /* ── Place picker actions ────────────────────────────────── */
  function addPlace(place) {
    if (selected.length >= MAX_PLACES) return;
    setSelected((prev) => [...prev, place]);
    setErrors((p) => ({ ...p, places: '' }));
  }

  function removePlace(id) {
    setSelected((prev) => prev.filter((p) => p.id !== id));
  }

  function movePlace(index, direction) {
    setSelected((prev) => {
      const next = [...prev];
      const target = index + direction;
      if (target < 0 || target >= next.length) return prev;
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
  }

  /* ── Validation ──────────────────────────────────────────── */
  function validate() {
    const e = {};
    if (!name.es.trim())         e.name        = t.errors.nameRequired;
    if (!description.es.trim())  e.description = t.errors.descriptionRequired;
    if (!targetAudience.trim())  e.targetAudience = t.errors.targetAudienceRequired;
    if (selected.length === 0)   e.places      = t.errors.placesRequired;
    if (selected.length > MAX_PLACES) e.places = t.errors.placesMax;
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

    const durationNum = duration.trim() === '' ? null : Number(duration);
    const payload = {
      name:           { es: name.es.trim(), en: name.en.trim(), fr: name.fr.trim() },
      description:    { es: description.es.trim(), en: description.en.trim(), fr: description.fr.trim() },
      targetAudience: targetAudience.trim(),
      imageUrl:       imageUrl.trim() || null,
      estimatedDurationMinutes:
        Number.isFinite(durationNum) ? durationNum : null,
      cityPlaceIds:   selected.map((p) => p.id),
    };

    try {
      const result = await saveCityRoute(payload, isEdit ? route.id : null);
      if (result?.error) {
        setSubmitError(t.errors.serverError);
        return;
      }
      const targetId = result?.id ?? route?.id;
      const target = targetId
        ? `/${lang}/tourism/routes/${targetId}`
        : `/${lang}/tourism/routes`;
      router.push(target);
      router.refresh();
    } catch {
      setSubmitError(t.errors.serverError);
    } finally {
      setSubmitting(false);
    }
  }

  /* ── Render ──────────────────────────────────────────────── */
  const previewPlaces = selected.filter(
    (p) => p.latitude != null && p.longitude != null,
  );

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

        {/* ── LEFT — form fields + place pickers ───────────── */}
        <div className="flex flex-col gap-md">

          <DefaultLangFields fields={translatableFields} />

          <Field label={t.targetAudienceLabel} error={errors.targetAudience}>
            <input
              type="text"
              value={targetAudience}
              onChange={(e) => { setTargetAudience(e.target.value); setErrors((p) => ({ ...p, targetAudience: '' })); }}
              placeholder={t.targetAudiencePlaceholder}
              className={errors.targetAudience ? INPUT_ERROR_CLS : INPUT_CLS}
            />
          </Field>

          <Field label={t.imageUrlLabel} hint={t.imageUrlHint}>
            <input
              type="url"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              placeholder={t.imageUrlPlaceholder}
              className={INPUT_CLS}
            />
          </Field>

          <Field label={t.durationLabel}>
            <input
              type="number"
              min={0}
              value={duration}
              onChange={(e) => setDuration(e.target.value)}
              placeholder={t.durationPlaceholder}
              className={INPUT_CLS}
            />
          </Field>

          {/* ── Place pickers ────────────────────────────── */}
          <div className="flex flex-col gap-sm">
            <div className="flex items-center justify-between">
              <span className="text-label-md text-on-surface font-medium">
                {t.placesLabel}
              </span>
              <span className="text-caption text-on-surface-variant tabular-nums">
                {t.selectedCount
                  .replace('{count}', String(selected.length))
                  .replace('{max}', String(MAX_PLACES))}
              </span>
            </div>
            <p className="text-caption text-on-surface-variant">{t.placesHint}</p>

            {/* Left-border accent group per citizen-services-rules */}
            <div className="pl-md border-l-2 border-secondary pr-sm flex flex-col gap-md">

              {/* Selected */}
              <div className="flex flex-col gap-xs">
                <p className="text-caption uppercase tracking-widest font-bold text-secondary">
                  {t.selectedLabel}
                </p>
                {selected.length === 0 ? (
                  <p className="text-body-sm text-on-surface-variant italic">
                    {t.emptySelected}
                  </p>
                ) : (
                  <ul className="flex flex-col gap-xs">
                    {selected.map((p, idx) => (
                      <li
                        key={p.id}
                        className="flex items-center gap-sm p-sm rounded-md bg-surface-container-lowest border border-outline-variant"
                      >
                        <span className="shrink-0 w-7 h-7 bg-primary text-on-primary flex items-center justify-center rounded-md text-label-sm font-bold">
                          {String(idx + 1).padStart(2, '0')}
                        </span>
                        <Icon
                          name={categoryIcon(p.category)}
                          size={18}
                          className="text-secondary shrink-0"
                        />
                        <span className="flex-1 text-body-sm text-on-surface truncate">
                          {p.name}
                        </span>
                        <button
                          type="button"
                          onClick={() => movePlace(idx, -1)}
                          disabled={idx === 0}
                          aria-label={t.moveUp}
                          className="p-xs rounded-md hover:bg-surface-container disabled:opacity-30 disabled:cursor-not-allowed text-on-surface-variant"
                        >
                          <Icon name="arrow_upward" size={16} />
                        </button>
                        <button
                          type="button"
                          onClick={() => movePlace(idx, +1)}
                          disabled={idx === selected.length - 1}
                          aria-label={t.moveDown}
                          className="p-xs rounded-md hover:bg-surface-container disabled:opacity-30 disabled:cursor-not-allowed text-on-surface-variant"
                        >
                          <Icon name="arrow_downward" size={16} />
                        </button>
                        <button
                          type="button"
                          onClick={() => removePlace(p.id)}
                          aria-label={t.removePlace}
                          className="p-xs rounded-md hover:bg-error-container text-error"
                        >
                          <Icon name="remove_circle" size={18} />
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              {/* Available */}
              <div className="flex flex-col gap-xs">
                <p className="text-caption uppercase tracking-widest font-bold text-secondary">
                  {t.availableLabel}
                </p>
                {available.length === 0 ? (
                  <p className="text-body-sm text-on-surface-variant italic">
                    {t.emptyAvailable}
                  </p>
                ) : (
                  <ul className="flex flex-col gap-xs max-h-72 overflow-y-auto pr-xs">
                    {available.map((p) => {
                      const full = selected.length >= MAX_PLACES;
                      return (
                        <li
                          key={p.id}
                          className="flex items-center gap-sm p-sm rounded-md bg-surface-container-lowest border border-outline-variant"
                        >
                          <Icon
                            name={categoryIcon(p.category)}
                            size={18}
                            className="text-secondary shrink-0"
                          />
                          <div className="flex-1 min-w-0">
                            <p className="text-body-sm text-on-surface truncate">{p.name}</p>
                            <p className="text-caption text-on-surface-variant truncate">
                              {categoryLabel(p.category, tCategories)}
                            </p>
                          </div>
                          <button
                            type="button"
                            onClick={() => addPlace(p)}
                            disabled={full}
                            aria-label={t.addPlace}
                            className="p-xs rounded-md hover:bg-primary/5 text-primary disabled:opacity-30 disabled:cursor-not-allowed"
                          >
                            <Icon name="add_circle" size={20} />
                          </button>
                        </li>
                      );
                    })}
                  </ul>
                )}
              </div>
            </div>

            {errors.places && (
              <p className="text-caption text-error flex items-center gap-xs">
                <Icon name="error" size={14} />
                {errors.places}
              </p>
            )}
          </div>
        </div>

        {/* ── RIGHT — live map preview ─────────────────────── */}
        <div className="flex flex-col gap-sm">
          <h3 className="text-h3 text-primary flex items-center gap-sm">
            <Icon name="map" fill size={20} className="text-secondary" />
            {t.mapPreview}
          </h3>
          {previewPlaces.length > 0 ? (
            <div className="min-h-80">
              <RouteMapClient places={previewPlaces} mapTitle={t.mapPreview} />
            </div>
          ) : (
            <div className="w-full h-80 rounded-md border border-dashed border-outline-variant bg-surface-container-low flex flex-col items-center justify-center gap-sm text-on-surface-variant p-md text-center">
              <Icon name="map" size={36} className="opacity-40" />
              <p className="text-body-sm">{t.mapEmpty}</p>
            </div>
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

