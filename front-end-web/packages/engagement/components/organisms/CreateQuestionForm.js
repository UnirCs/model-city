 'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import {
  DefaultLangFields,
  TranslationSections,
  AiTranslateButton,
} from '@modelcity/core/components/molecules/MultilingualFields';
import barrios from '@modelcity/core/lib/config/neighbourhoods';
import { createQuestion } from '@modelcity/engagement/lib/actions/questions';

/* ─────────────────────────────────────────────────────────────────────────
 * Helpers
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Returns the minimum allowed openDate (today + 3 days) as "YYYY-MM-DD".
 * Using UTC to avoid timezone shifts when comparing date-only strings.
 */
function minOpenDate() {
  const d = new Date();
  d.setUTCDate(d.getUTCDate() + 3);
  return d.toISOString().split('T')[0];
}

function Field({ label, hint, error, children }) {  return (
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

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';

const INPUT_ERROR_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-error';

/* ─────────────────────────────────────────────────────────────────────────
 * CreateQuestionForm
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Organism: CreateQuestionForm
 *
 * Full form for platform administrators to create a new civic consultation.
 * On success, navigates to the new question's detail page.
 *
 * @param {{ t: object, lang: string }} props
 */
export default function CreateQuestionForm({ t, lang }) {
  const router = useRouter();
  const goBack = useLocalizedBack();

  const [title, setTitle]           = useState({ es: '', en: '', fr: '' });
  const [description, setDescription] = useState({ es: '', en: '', fr: '' });
  const [imageUrl, setImageUrl]     = useState('');
  const [openDate, setOpenDate]     = useState('');
  const [closeDate, setCloseDate]   = useState('');
  const [zoneId, setZoneId]         = useState(String(barrios[0].id));
  const [neighbourhoodId, setNeighbourhoodId] = useState('');
  const [objectives, setObjectives] = useState([{ es: '', en: '', fr: '' }]);

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [errors, setErrors]         = useState({});

  /* ── Zone change: reset neighbourhood ─────────────────────── */
  const selectedZone = barrios.find((z) => String(z.id) === zoneId) ?? barrios[0];

  const handleZoneChange = (newZoneId) => {
    setZoneId(newZoneId);
    setNeighbourhoodId('');
  };

  /* ── Objectives helpers ─────────────────────────────────────── */
  const addObjective    = () => setObjectives((prev) => [...prev, { es: '', en: '', fr: '' }]);
  const removeObjective = (i) => setObjectives((prev) => prev.filter((_, idx) => idx !== i));
  const updateObjective = (i, langKey, val) =>
    setObjectives((prev) => prev.map((o, idx) => (idx === i ? { ...o, [langKey]: val } : o)));

  /* ── Validation ─────────────────────────────────────────────── */
  function validate() {
    const e = {};
    if (!title.es.trim())       e.title       = t.errors.titleRequired;
    if (!description.es.trim()) e.description = t.errors.descriptionRequired;
    if (!openDate)           e.openDate    = t.errors.openDateRequired;
    else if (openDate < minOpenDate()) e.openDate = t.errors.openDateTooSoon;
    if (!closeDate)          e.closeDate   = t.errors.closeDateRequired;
    if (openDate && closeDate && openDate >= closeDate) e.closeDate = t.errors.dateOrder;
    if (!neighbourhoodId)    e.neighbourhood = t.errors.neighbourhoodRequired;
    return e;
  }

  /* ── Submit ─────────────────────────────────────────────────── */
  async function handleSubmit(e) {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setErrors({});
    setSubmitting(true);
    setSubmitError('');

    const payload = {
      title:           { es: title.es.trim(), en: title.en.trim(), fr: title.fr.trim() },
      description:     { es: description.es.trim(), en: description.en.trim(), fr: description.fr.trim() },
      imageUrl:        imageUrl.trim() || null,
      openDate,
      closeDate,
      zoneId:          Number(zoneId),
      neighbourhoodId: Number(neighbourhoodId),
      objectives: objectives
        .filter((o) => o.es.trim())
        .map((o, idx) => ({ objective: { es: o.es.trim(), en: o.en.trim(), fr: o.fr.trim() }, sortOrder: idx + 1 })),
    };

    try {
      const result = await createQuestion(payload);
      if (result?.error) {
        setSubmitError(t.errors.serverError);
        return;
      }
      // Navigate to the new question detail (if id returned) or back to list
      if (result?.id) {
        router.push(`/${lang}/participation/questions/${result.id}`);
      } else {
        router.push(`/${lang}/participation/questions`);
      }
    } catch {
      setSubmitError(t.errors.serverError);
    } finally {
      setSubmitting(false);
    }
  }

  /* ── Translatable fields ────────────────────────────────────── */
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

  /** Objective translations rendered inside each secondary-language section. */
  const renderObjectiveTranslations = (l) => (
    <div className="flex flex-col gap-sm">
      <p className="text-label-sm text-on-surface-variant font-medium">{t.objectivesLabel}</p>
      {objectives.map((obj, i) => (
        <div key={i} className="flex flex-col gap-2xs">
          <div className="flex items-center justify-between gap-sm">
            <span className="text-caption text-on-surface-variant">{`${t.objectivePlaceholder} ${i + 1}`}</span>
            <AiTranslateButton
              source={obj.es}
              targetLang={l}
              onResult={(text) => updateObjective(i, l, text)}
              ariaLabel={`${t.objectivePlaceholder} ${i + 1}`}
            />
          </div>
          <input
            type="text"
            value={obj[l]}
            onChange={(e) => updateObjective(i, l, e.target.value)}
            placeholder={`${t.objectivePlaceholder} ${i + 1}`}
            className={INPUT_CLS}
          />
        </div>
      ))}
    </div>
  );

  /* ── Render ─────────────────────────────────────────────────── */
  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">

      {/* Title + Description (default language; other languages below) */}
      <DefaultLangFields fields={translatableFields} />

      {/* Image URL (optional) */}
      <Field label={t.imageUrlLabel} hint={t.imageUrlHint}>
        <input
          type="url"
          value={imageUrl}
          onChange={(e) => setImageUrl(e.target.value)}
          placeholder={t.imageUrlPlaceholder}
          className={INPUT_CLS}
        />
      </Field>

      {/* Dates */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-md">
        <Field label={t.openDateLabel} error={errors.openDate}>
          <input
            type="date"
            value={openDate}
            min={minOpenDate()}
            onChange={(e) => { setOpenDate(e.target.value); setErrors((p) => ({ ...p, openDate: '', closeDate: '' })); }}
            className={errors.openDate ? INPUT_ERROR_CLS : INPUT_CLS}
          />
        </Field>
        <Field label={t.closeDateLabel} error={errors.closeDate}>
          <input
            type="date"
            value={closeDate}
            onChange={(e) => { setCloseDate(e.target.value); setErrors((p) => ({ ...p, closeDate: '' })); }}
            className={errors.closeDate ? INPUT_ERROR_CLS : INPUT_CLS}
          />
        </Field>
      </div>

      {/* Zone */}
      <Field label={t.zoneLabel}>
        <div className="relative">
          <select
            value={zoneId}
            onChange={(e) => handleZoneChange(e.target.value)}
            className={`appearance-none pr-xl ${INPUT_CLS}`}
          >
            {barrios.map((z) => (
              <option key={z.id} value={String(z.id)}>{z.zone}</option>
            ))}
          </select>
          <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
            <Icon name="expand_more" size={20} />
          </span>
        </div>
      </Field>

      {/* Neighbourhood */}
      <Field label={t.neighbourhoodLabel} error={errors.neighbourhood}>
        <div className="relative">
          <select
            value={neighbourhoodId}
            onChange={(e) => { setNeighbourhoodId(e.target.value); setErrors((p) => ({ ...p, neighbourhood: '' })); }}
            className={`appearance-none pr-xl ${errors.neighbourhood ? INPUT_ERROR_CLS : INPUT_CLS}`}
          >
            <option value="" disabled>{t.neighbourhoodPlaceholder}</option>
            {selectedZone.neighbourhoods.map((n) => (
              <option key={n.id} value={String(n.id)}>{n.displayName}</option>
            ))}
          </select>
          <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
            <Icon name="expand_more" size={20} />
          </span>
        </div>
      </Field>

      {/* Objectives */}
      <div className="flex flex-col gap-sm">
        <div className="flex items-center justify-between">
          <span className="text-label-md text-on-surface font-medium">{t.objectivesLabel}</span>
          <button
            type="button"
            onClick={addObjective}
            className="flex items-center gap-xs text-label-sm text-secondary hover:underline"
          >
            <Icon name="add_circle" size={16} />
            {t.addObjective}
          </button>
        </div>

        {/* Options group — left-border accent per citizen-services-rules */}
        {objectives.length > 0 && (
          <div className="pl-md border-l-2 border-secondary flex flex-col gap-sm pr-sm">
            {objectives.map((obj, i) => (
              <div key={i} className="flex items-center gap-sm">
                <input
                  type="text"
                  value={obj.es}
                  onChange={(e) => updateObjective(i, 'es', e.target.value)}
                  placeholder={`${t.objectivePlaceholder} ${i + 1}`}
                  className={`flex-1 ${INPUT_CLS}`}
                />
                {objectives.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removeObjective(i)}
                    className="shrink-0 rounded-full p-xs hover:bg-error-container text-error transition-colors"
                    aria-label={t.removeObjective}
                  >
                    <Icon name="remove_circle" size={18} />
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Other languages — collapsible per-language sections with AI translation */}
      <TranslationSections fields={translatableFields} renderExtra={renderObjectiveTranslations} />

      {/* Submit error */}
      {submitError && (
        <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
          <Icon name="error" size={18} className="shrink-0" />
          {submitError}
        </div>
      )}

      {/* CTA */}
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
          {submitting ? t.submitting : t.submit}
        </Button>
      </div>
    </form>
  );
}

