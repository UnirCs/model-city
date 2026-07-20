'use client';

import { useState } from 'react';
import Modal from '@modelcity/core/components/molecules/Modal';
import FormField from '@modelcity/core/components/atoms/FormField';
import Button from '@modelcity/core/components/atoms/Button';
import Icon from '@modelcity/core/components/atoms/Icon';
import { useAnnounce } from '@modelcity/core/lib/a11y/AnnouncerProvider';

/** Role options with icons and description keys */
const ROLE_OPTIONS = [
  {
    value:       'MODEL-CITY-BACKOFFICE',
    labelKey:    'roleAdministrative',
    descKey:     'roleAdministrativeDesc',
    icon:        'manage_accounts',
    activeColor: 'bg-primary text-on-primary',
    ringColor:   'focus-visible:ring-primary',
    idleColor:   'bg-surface-container-low text-primary hover:bg-primary/10',
    fadedColor:  'bg-surface-container text-on-surface-variant opacity-50',
  },
  {
    value:       'MODEL-CITY-OPERATOR',
    labelKey:    'roleField',
    descKey:     'roleFieldDesc',
    icon:        'engineering',
    activeColor: 'bg-secondary text-on-secondary',
    ringColor:   'focus-visible:ring-secondary',
    idleColor:   'bg-surface-container-low text-secondary hover:bg-secondary/10',
    fadedColor:  'bg-surface-container text-on-surface-variant opacity-50',
  },
  {
    value:       'MODEL-CITY-MOBILITY-AGENT',
    labelKey:    'roleMobility',
    descKey:     'roleMobilityDesc',
    icon:        'directions_bus',
    activeColor: 'bg-tertiary text-on-tertiary',
    ringColor:   'focus-visible:ring-tertiary',
    idleColor:   'bg-surface-container-low text-tertiary hover:bg-tertiary/10',
    fadedColor:  'bg-surface-container text-on-surface-variant opacity-50',
  },
];

const INPUT_BASE =
  'w-full rounded-md border border-outline bg-surface-container-lowest text-body-md text-on-surface ' +
  'px-md py-sm placeholder:text-on-surface-variant/60 ' +
  'focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary ' +
  'disabled:opacity-50 disabled:cursor-not-allowed transition-colors';

const INPUT_ERROR = 'border-error focus:ring-error focus:border-error';

/**
 * Molecule: CreateAgentModal
 * Modal form for inviting a new municipal agent (operator).
 * Calls the `onSubmit` server action with { role, name, email }.
 * Shows a success banner (green) or an error banner (red) after submission.
 *
 * @param {{
 *   open: boolean,
 *   onSubmit: (payload: { role: string, name: string, email: string }) => Promise<{ ok: true } | { error: string }>,
 *   onClose: () => void,
 *   labels: {
 *     title: string,
 *     roleLabel: string, rolePlaceholder: string,
 *     roleAdministrative: string, roleField: string, roleMobility: string,
 *     nameLabel: string, namePlaceholder: string,
 *     emailLabel: string, emailPlaceholder: string,
 *     submit: string, submitting: string, cancel: string,
 *     successTitle: string, successMessage: string, errorMessage: string,
 *     nameRequired: string, emailRequired: string, roleRequired: string, emailInvalid: string,
 *     close: string,
 *   },
 * }} props
 */
export default function CreateAgentModal({ open, onSubmit, onClose, labels }) {
  const announce = useAnnounce();
  const [role,  setRole]  = useState('');
  const [name,  setName]  = useState('');
  const [email, setEmail] = useState('');

  const [errors,     setErrors]    = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [result,     setResult]    = useState(null);    // 'success' | 'error'
  const [errorInfo,  setErrorInfo]  = useState(null);   // { message, detail } | null

  /* ── Maps HTTP status → i18n key ── */
  const errorKeyForStatus = (status) => {
    if (status === 0)                   return 'errorNetwork';
    if (status === 400)                 return 'errorBadRequest';
    if (status === 403 || status === 401) return 'errorForbidden';
    if (status === 409)                 return 'errorConflict';
    if (status >= 500)                  return 'errorServer';
    return 'errorMessage';
  };

  /* ── Client-side validation ── */
  const validate = () => {
    const next = {};
    if (!role)  next.role  = labels.roleRequired;
    if (!name.trim())  next.name  = labels.nameRequired;
    if (!email.trim()) {
      next.email = labels.emailRequired;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      next.email = labels.emailInvalid;
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  /* ── Submit handler ── */
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    setResult(null);
    const res = await onSubmit({ role, name: name.trim(), email: email.trim() });
    setSubmitting(false);

    if ('ok' in res) {
      setResult('success');
      announce(labels.successTitle, 'polite');
    } else {
      setErrorInfo({
        message: labels[errorKeyForStatus(res.status)] ?? labels.errorMessage,
        detail:  res.detail ?? res.error ?? null,
      });
      setResult('error');
      announce(labels[errorKeyForStatus(res.status)] ?? labels.errorMessage, 'assertive');
    }
  };

  /* ── Reset and close ── */
  const handleClose = () => {
    setRole(''); setName(''); setEmail('');
    setErrors({}); setResult(null); setErrorInfo(null);
    onClose();
  };

  return (
    <Modal open={open} title={labels.title} onClose={handleClose} closeLabel={labels.close}>
      {/* ── Result banners ── */}
      {result === 'success' && (
        <div className="mb-md flex items-start gap-sm rounded-md bg-green-50 text-green-800 px-md py-sm text-body-md border border-green-200 dark:bg-green-950 dark:text-green-200 dark:border-green-800">
          <Icon name="check_circle" size={20} className="mt-0.5 shrink-0" />
          <div>
            <p className="font-semibold">{labels.successTitle}</p>
            <p>{labels.successMessage}</p>
          </div>
        </div>
      )}

      {result === 'error' && errorInfo && (
        <div className="mb-md rounded-md bg-error-container text-on-error-container text-body-md border border-error/20 overflow-hidden">
          {/* Main error row */}
          <div className="flex items-start gap-sm px-md py-sm">
            <Icon name="report" size={20} className="shrink-0 mt-0.5" />
            <p className="flex-1">{errorInfo.message}</p>
          </div>
          {/* Technical detail (if backend provided one) */}
          {errorInfo.detail && (
            <div className="border-t border-error/20 px-md py-xs flex items-start gap-xs">
              <Icon name="terminal" size={14} className="shrink-0 mt-0.5 opacity-60" />
              <p className="text-caption opacity-75 break-all">
                <span className="font-semibold">{labels.errorDetailLabel}</span>{' '}
                {errorInfo.detail}
              </p>
            </div>
          )}
        </div>
      )}

      {/* ── Form (hidden after success) ── */}
      {result !== 'success' && (
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">
          {/* Role picker */}
          <div className="flex flex-col gap-xs">
            <span className="text-label-md text-on-surface font-medium">
              {labels.roleLabel}
              <span className="text-error ml-xs" aria-hidden="true">*</span>
            </span>

            <div className="flex flex-col gap-[0.5rem]" role="radiogroup" aria-label={labels.roleLabel}>
              {ROLE_OPTIONS.map((opt) => {
                const isSelected = role === opt.value;
                const isFaded    = role && role !== opt.value;
                return (
                  <button
                    key={opt.value}
                    type="button"
                    role="radio"
                    aria-checked={isSelected}
                    disabled={submitting}
                    onClick={() => { setRole(opt.value); setErrors((p) => ({ ...p, role: undefined })); }}
                    className={[
                      'flex items-center gap-md rounded-md border-2 px-4 py-[0.875rem] text-left w-full',
                      'transition-all duration-200 select-none cursor-pointer',
                      'focus-visible:outline-none focus-visible:ring-2',
                      opt.ringColor,
                      isSelected ? `${opt.activeColor} border-transparent shadow-md`
                        : isFaded  ? `${opt.fadedColor} border-transparent`
                                   : `${opt.idleColor} border-outline-variant`,
                    ].join(' ')}
                  >
                    <Icon
                      name={opt.icon}
                      size={28}
                      fill={isSelected}
                      className="shrink-0"
                    />
                    <div className="flex-1 min-w-0">
                      <p className="text-label-md font-semibold leading-tight">{labels[opt.labelKey]}</p>
                      <p className={[
                        'text-caption leading-snug mt-0.5',
                        isSelected ? 'opacity-80' : 'text-on-surface-variant',
                      ].join(' ')}>
                        {labels[opt.descKey]}
                      </p>
                    </div>
                    {isSelected && (
                      <Icon name="check_circle" fill size={18} className="shrink-0 opacity-80" />
                    )}
                  </button>
                );
              })}
            </div>

            {errors.role && (
              <p className="text-caption text-error" role="alert">{errors.role}</p>
            )}
          </div>

          {/* Name */}
          <FormField id="agent-name" label={labels.nameLabel} required error={errors.name}>
            <input
              id="agent-name"
              type="text"
              value={name}
              onChange={(e) => { setName(e.target.value); setErrors((p) => ({ ...p, name: undefined })); }}
              placeholder={labels.namePlaceholder}
              disabled={submitting}
              autoComplete="name"
              className={[INPUT_BASE, errors.name ? INPUT_ERROR : ''].join(' ')}
            />
          </FormField>

          {/* Email */}
          <FormField id="agent-email" label={labels.emailLabel} required error={errors.email}>
            <input
              id="agent-email"
              type="email"
              value={email}
              onChange={(e) => { setEmail(e.target.value); setErrors((p) => ({ ...p, email: undefined })); }}
              placeholder={labels.emailPlaceholder}
              disabled={submitting}
              autoComplete="email"
              className={[INPUT_BASE, errors.email ? INPUT_ERROR : ''].join(' ')}
            />
          </FormField>

          {/* Actions */}
          <div className="flex justify-end gap-sm pt-xs">
            <Button type="button" variant="outline-error" size="md" onClick={handleClose} disabled={submitting}>
              {labels.cancel}
            </Button>
            <Button type="submit" variant="primary" size="md" disabled={submitting}>
              {submitting ? (
                <>
                  <Icon name="progress_activity" size={18} className="animate-spin" />
                  <span>{labels.submitting}</span>
                </>
              ) : (
                <>
                  <Icon name="send" size={18} />
                  <span>{labels.submit}</span>
                </>
              )}
            </Button>
          </div>
        </form>
      )}

      {/* Close button after success */}
      {result === 'success' && (
        <div className="flex justify-end pt-xs">
          <Button type="button" variant="primary" size="md" onClick={handleClose}>
            {labels.close}
          </Button>
        </div>
      )}
    </Modal>
  );
}





