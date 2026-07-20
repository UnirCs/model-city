'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Button from '@modelcity/core/components/atoms/Button';
import Icon from '@modelcity/core/components/atoms/Icon';
import barrios from '@modelcity/core/lib/config/neighbourhoods';
import { registerUser } from '@modelcity/core/lib/actions/registration';

/* ─────────────────────────────────────────────
   Sub-component: field with label and error
   ───────────────────────────────────────────── */
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

/* ─────────────────────────────────────────────
   Main component: RegistrationForm
   ───────────────────────────────────────────── */
/**
 * New user registration form.
 *
 * Fields:
 *   · email    — read only (comes from Auth0)
 *   · name     — editable, pre-filled with the account name
 *   · barrio   — selection from a list of municipality neighbourhoods
 *
 * On submit → Server Action registerUser → redirects to /{lang}/home on success.
 *
 * @param {{ email: string, defaultName: string, dict: object, lang: string }} props
 */
export default function RegistrationForm({ email, defaultName, dict, lang }) {
  const router = useRouter();

  const [name, setName]     = useState(defaultName);
  const [barrio, setBarrio] = useState('');

  const [submitting, setSubmitting]   = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [errors, setErrors]           = useState({});

  /* ── Basic validation ──────────────────── */
  function validate() {
    const e = {};
    if (!name.trim()) e.name   = dict.errors.nameRequired;
    if (!barrio)      e.barrio = dict.errors.neighbourhoodRequired;
    return e;
  }

  /* ── Form submission ───────────────────── */
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

    try {
      const result = await registerUser({ name: name.trim(), neighbourhoodName: barrio });

      if (result?.error) {
        setSubmitError(result.error);
        return;
      }

      router.push(`/${lang}/home`);
    } catch {
      setSubmitError(dict.errors.serverError);
    } finally {
      setSubmitting(false);
    }
  }

  /* ── Render ─────────────────────────────── */
  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">

      {/* Email — read only */}
      <Field label={dict.emailLabel} hint={dict.emailHint}>
        <div className="flex items-center gap-sm px-md py-sm rounded-md bg-surface-container border border-outline-variant text-on-surface-variant text-body-md select-none">
          <Icon name="mail" size={18} className="shrink-0" />
          <span className="truncate">{email}</span>
        </div>
      </Field>

      {/* Display name */}
      <Field label={dict.nameLabel} hint={dict.nameHint} error={errors.name}>
        <input
          type="text"
          value={name}
          onChange={e => {
            setName(e.target.value);
            setErrors(prev => ({ ...prev, name: '' }));
          }}
          placeholder={dict.namePlaceholder}
          className={[
            'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface',
            'transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
            errors.name ? 'border-error' : 'border-outline-variant',
          ].join(' ')}
          autoComplete="name"
        />
      </Field>

      {/* Neighbourhood */}
      <Field label={dict.neighbourhoodLabel} hint={dict.neighbourhoodHint} error={errors.barrio}>
        <div className="relative">
          <select
            value={barrio}
            onChange={e => {
              setBarrio(e.target.value);
              setErrors(prev => ({ ...prev, barrio: '' }));
            }}
            className={[
              'w-full appearance-none px-md py-sm pr-xl rounded-md border text-body-md bg-surface-container-lowest text-on-surface',
              'transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary cursor-pointer',
              errors.barrio ? 'border-error' : 'border-outline-variant',
              !barrio && 'text-on-surface-variant',
            ].filter(Boolean).join(' ')}
          >
            <option value="" disabled>{dict.neighbourhoodPlaceholder}</option>
            {barrios.map(zone => (
              <optgroup key={zone.zoneName} label={zone.zone}>
                {zone.neighbourhoods.map(n => (
                  <option key={n.name} value={n.name}>{n.displayName}</option>
                ))}
              </optgroup>
            ))}
          </select>
          {/* Decorative chevron */}
          <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
            <Icon name="expand_more" size={20} />
          </span>
        </div>
      </Field>

      {/* Submit error */}
      {submitError && (
        <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
          <Icon name="error" size={18} className="shrink-0" />
          {submitError}
        </div>
      )}

      {/* Action buttons */}
      <div className="grid grid-cols-5 gap-sm mt-sm w-full">
        <a
          href="/auth/logout"
          className="col-span-2 inline-flex items-center justify-center gap-xs font-semibold transition-all duration-200 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary cursor-pointer border border-primary text-primary bg-transparent hover:bg-primary/10 px-sm py-sm text-label-md rounded-md"
        >
          {dict.back}
        </a>

        <Button
          type="submit"
          variant="primary"
          size="md"
          disabled={submitting}
          className="col-span-3"
        >
          {submitting
            ? <>{dict.submitting}</>
            : <>{dict.submit}</>
          }
        </Button>
      </div>
    </form>
  );
}
