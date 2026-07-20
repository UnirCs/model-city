'use client';

import { useState, useRef, useCallback } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import OtpInput from '@modelcity/core/components/molecules/OtpInput';

const DEFAULT_OTP_LENGTH = 6;

/**
 * OTP error codes that the panel recovers from by showing a resend button.
 * Any other error is forwarded to `onFatalError`.
 */
const RESENDABLE_ERRORS = new Set([
  'otp_expired',
  'otp_already_used',
  'otp_mismatch',
]);

/**
 * Organism: OtpVerificationPanel
 *
 * Self-contained OTP verification flow. Manages its own current authorization ID
 * (starting from `initialAuthId`) and all OTP-related UI states. Suitable for
 * any operation that requires a one-time-password confirmation — not tied to
 * any specific business domain.
 *
 * Phase machine (internal):
 *  'idle'           — waiting for the user to type the code
 *  'submitting'     — calling `onSubmit`
 *  'resending'      — calling `onResend` for a new code
 *  'otp-error'      — invalid code, user can retype
 *  'otp-expired'    — code expired → resend button
 *  'otp-already-used' — code already consumed → resend button
 *  'otp-mismatch'   — resource/operation mismatch → resend button
 *  'otp-no-attempts' — too many failures, terminal state
 *  'otp-new-sent'   — backend sent a fresh code automatically
 *
 * @param {{
 *   initialAuthId:  string,
 *   otpLength?:     number,
 *   onSubmit:       (authId: string, otp: string) => Promise<{ ok: true } | { error: string }>,
 *   onResend:       () => Promise<{ ok: true, operationAuthorizationId: string } | { error: string }>,
 *   onSuccess:      () => void,
 *   onFatalError:   (errorCode: string) => void,
 *   t:              object,
 * }} props
 */
export default function OtpVerificationPanel({
  initialAuthId,
  otpLength = DEFAULT_OTP_LENGTH,
  onSubmit,
  onResend,
  onSuccess,
  onFatalError,
  t,
}) {
  const [phase, setPhase]   = useState('idle');
  const [digits, setDigits] = useState(() => Array(otpLength).fill(''));
  const [notice, setNotice] = useState(null);

  // Always holds the latest authId, even after the panel received a fresh one
  const authIdRef = useRef(initialAuthId);

  const resetDigits = useCallback(() => {
    setDigits(Array(otpLength).fill(''));
  }, [otpLength]);

  /* ── Submit OTP ─────────────────────────────────────────── */

  const handleSubmit = useCallback(async (filledDigits) => {
    setPhase('submitting');

    const result = await onSubmit(authIdRef.current, filledDigits.join(''));

    if (result.ok) {
      onSuccess();
      return;
    }

    switch (result.error) {
      case 'otp_expired':
        setPhase('otp-expired');
        break;

      case 'otp_already_used':
        setPhase('otp-already-used');
        break;

      case 'otp_mismatch':
        setPhase('otp-mismatch');
        break;

      case 'otp_no_attempts':
        setPhase('otp-no-attempts');
        break;

      case 'otp_new_sent':
        // Backend automatically sent a new code — stay in idle with a notice
        resetDigits();
        setNotice(t.newSent);
        setPhase('idle');
        break;

      case 'otp_invalid':
        // Generic wrong digit — clear the boxes and let the user try again
        resetDigits();
        setNotice(t.error);
        setPhase('idle');
        break;

      default:
        // Non-OTP error (e.g. vote_failed, network_error) — delegate upward
        onFatalError(result.error);
        break;
    }
  }, [onSubmit, onSuccess, onFatalError, t.newSent, t.error, resetDigits]);

  /* ── Resend ─────────────────────────────────────────────── */

  const handleResend = useCallback(async () => {
    setPhase('resending');
    setNotice(null);

    const result = await onResend();

    if (!result.ok) {
      onFatalError(result.error ?? 'authorization_failed');
      return;
    }

    authIdRef.current = result.operationAuthorizationId;
    resetDigits();
    setPhase('idle');
  }, [onResend, onFatalError, resetDigits]);

  /* ── Helpers ────────────────────────────────────────────── */

  const isLoading  = phase === 'submitting' || phase === 'resending';
  const isTerminal = phase === 'otp-no-attempts';

  /* ════════════════════════════════════════════════════════ */
  /* RENDER                                                    */
  /* ════════════════════════════════════════════════════════ */

  /* ── Spinner (submitting / resending) ───────────────────── */
  if (isLoading) {
    const label = phase === 'resending' ? t.requestingAuth : t.verifying;
    return (
      <div className="border-t border-outline-variant bg-surface-container-highest px-md py-lg flex flex-col items-center gap-sm text-center">
        <Icon name="refresh" size={28} className="text-secondary animate-spin" />
        <p className="text-label-md text-on-surface-variant">{label}</p>
      </div>
    );
  }

  /* ── Terminal: no attempts remaining ────────────────────── */
  if (isTerminal) {
    return (
      <div className="bg-error-container border-t border-outline-variant px-md py-md flex flex-col items-center gap-md text-center">
        <Icon name="block" fill size={36} className="text-error" />
        <p className="text-body-md text-on-error-container">{t.noAttempts}</p>
      </div>
    );
  }

  /* ── Resendable error states ────────────────────────────── */
  if (phase === 'otp-expired') {
    return (
      <div className="bg-orange-100 border-t border-orange-200 px-md py-md flex flex-col items-center gap-md text-center">
        <Icon name="timer_off" fill size={36} className="text-orange-600" />
        <p className="text-body-md text-orange-900">{t.expired}</p>
        <button
          type="button"
          onClick={handleResend}
          className="flex items-center gap-xs px-lg py-sm rounded-md bg-secondary text-on-secondary text-label-md font-semibold hover:shadow-md transition-all active:scale-95"
        >
          <Icon name="send" size={16} />
          {t.resendCode}
        </button>
      </div>
    );
  }

  if (phase === 'otp-mismatch') {
    return (
      <div className="bg-surface-container-highest border-t border-outline-variant px-md py-md flex flex-col items-center gap-md text-center">
        <Icon name="gpp_maybe" fill size={36} className="text-outline" />
        <p className="text-body-md text-on-surface">{t.mismatch}</p>
        <button
          type="button"
          onClick={handleResend}
          className="flex items-center gap-xs px-lg py-sm rounded-md bg-secondary text-on-secondary text-label-md font-semibold hover:shadow-md transition-all active:scale-95"
        >
          <Icon name="send" size={16} />
          {t.resendCode}
        </button>
      </div>
    );
  }

  if (phase === 'otp-already-used') {
    return (
      <div className="bg-surface-container-highest border-t border-outline-variant px-md py-md flex flex-col items-center gap-md text-center">
        <Icon name="lock_reset" fill size={36} className="text-outline" />
        <p className="text-body-md text-on-surface">{t.alreadyUsed}</p>
        <button
          type="button"
          onClick={handleResend}
          className="flex items-center gap-xs px-lg py-sm rounded-md bg-secondary text-on-secondary text-label-md font-semibold hover:shadow-md transition-all active:scale-95"
        >
          <Icon name="send" size={16} />
          {t.resendCode}
        </button>
      </div>
    );
  }

  /* ── Idle: OTP digit input ──────────────────────────────────── */
  return (
    <div
      className={[
        'border-t px-md py-md flex flex-col items-center gap-sm',
        notice
          ? 'bg-orange-100 border-orange-200'
          : 'bg-surface-container-highest border-outline-variant',
      ].join(' ')}
    >
      <div className={['flex items-center gap-xs', notice ? 'text-orange-700' : 'text-secondary'].join(' ')}>
        <Icon name="mark_email_unread" fill size={20} />
        <p className="text-label-md font-semibold">{t.title}</p>
      </div>

      <p className={['text-caption text-center', notice ? 'text-orange-800' : 'text-on-surface-variant'].join(' ')}>
        {t.subtitle}
      </p>

      {/* Info notice — new code sent automatically by the backend */}
      {notice && (
        <div className="flex items-start gap-xs bg-orange-100 text-orange-800 rounded-md px-sm py-xs w-full">
          <Icon name="info" size={16} className="mt-0.5 shrink-0" />
          <p className="text-caption">{notice}</p>
        </div>
      )}

      <OtpInput
        digits={digits}
        onChange={setDigits}
        onComplete={handleSubmit}
        length={otpLength}
        notice={notice}
      />
    </div>
  );
}


