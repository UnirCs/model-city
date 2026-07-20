'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import OtpVerificationPanel from '@modelcity/core/components/organisms/OtpVerificationPanel';
import CertInvalidError from '@modelcity/core/components/molecules/CertInvalidError';
import { verifyCertificate } from '@modelcity/core/lib/api/certClient';
import { requestVoteAuthorization, confirmVoteWithOtp } from '@modelcity/engagement/lib/actions/vote';
import { useAnnounce } from '@modelcity/core/lib/a11y/AnnouncerProvider';


/**
 * Molecule: VotingZone
 *
 * Orchestrates the full voting flow:
 *   1. YES / NO selection
 *   2. Browser-side digital certificate verification (TLS client auth via ALB)
 *   3. Server Action → POST /transaction-authorization/operation-authorizations
 *   4. OtpVerificationPanel — handles all OTP entry and error states
 *   5. On OTP success → vote is cast via confirmVoteWithOtp
 *
 * OTP-specific UI and state are fully delegated to {@link OtpVerificationPanel}.
 *
 * @param {{
 *   t:           object,
 *   tOtp:        object,
 *   tCert:       object,
 *   isActive:    boolean,
 *   questionId:  number | string,
 *   accessToken: string
 * }} props
 */
export default function VotingZone({ t, tOtp, tCert, isActive, questionId, accessToken }) {
  const announce = useAnnounce();
  /**
   * Phase state machine:
   *  'select'          — choosing YES or NO
   *  'checking-cert'   — browser TLS cert request in progress
   *  'cert-invalid'    — FNMT cert not found / invalid
   *  'requesting-auth' — server action POSTing the authorization
   *  'error'           — authorization request failed (unrecoverable this session)
   *  'awaiting-otp'    — OtpVerificationPanel is shown
   *  'vote-error'      — OTP ok but final vote POST failed
   *  'done'            — vote successfully cast
   */
  const [phase, setPhase]   = useState('select');
  const [vote, setVote]     = useState(null);   // 'yes' | 'no'
  const [authId, setAuthId] = useState(null);
  const [verificationToken, setVerificationToken] = useState(null);

  /* ── Vote selection ─────────────────────────────────────── */

  const isLocked = phase !== 'select';

  const handleSelectVote = (choice) => {
    if (isLocked) return;
    setVote((prev) => (prev === choice ? null : choice));
  };

  /* ── Main confirm flow ──────────────────────────────────── */

  const handleConfirm = async () => {
    if (!vote || isLocked) return;

    // Step 1 — Cert verification (TLS client auth must originate from the browser)
    setPhase('checking-cert');
    const certResult = await verifyCertificate(accessToken);
    if (!certResult.valid) { setPhase('cert-invalid'); return; }

    // Step 2 — Request authorization → OTP email sent to citizen
    setPhase('requesting-auth');
    setVerificationToken(certResult.verificationToken);
    const authResult = await requestVoteAuthorization({
      questionId,
      verificationToken: certResult.verificationToken,
    });
    if (!authResult.ok) { setPhase('error'); announce(tOtp.authError, 'assertive'); return; }

    setAuthId(authResult.operationAuthorizationId);
    setPhase('awaiting-otp');
  };

  /* ── Reset ──────────────────────────────────────────────── */

  const handleRetry = () => {
    setPhase('select');
    setVote(null);
    setAuthId(null);
    setVerificationToken(null);
  };

  /* ── OTP callbacks for OtpVerificationPanel ─────────────── */

  /** Called by the panel when the user submits an OTP. */
  const handleOtpSubmit = async (currentAuthId, otp) =>
    confirmVoteWithOtp({
      operationAuthorizationId: currentAuthId,
      otp,
      questionId,
      vote: vote.toUpperCase(),
    });

  /** Called by the panel when the user requests a new code. */
  const handleResendAuth = async () =>
    requestVoteAuthorization({ questionId, verificationToken });

  /* ════════════════════════════════════════════════════════ */
  /* RENDER                                                    */
  /* ════════════════════════════════════════════════════════ */

  if (!isActive) {
    return (
      <div className="rounded-md bg-surface-container border border-outline-variant p-lg flex flex-col items-center gap-sm text-center">
        <Icon name="lock_clock" size={36} className="text-outline" />
        <p className="text-body-md text-on-surface-variant">{t.notActive}</p>
      </div>
    );
  }

  if (phase === 'done') {
    return (
      <div className="rounded-md bg-secondary text-on-secondary p-lg flex flex-col items-center gap-sm text-center shadow-lg">
        <Icon name="how_to_vote" fill size={48} />
        <p className="text-h3 font-semibold">{t.registered}</p>
      </div>
    );
  }

  const loadingLabel =
    phase === 'checking-cert'   ? t.verifyingCert     :
    phase === 'requesting-auth' ? tOtp.requestingAuth  :
    null;

  return (
    <div className="rounded-md overflow-hidden shadow-lg border border-outline-variant">

      {/* ── Header ────────────────────────────────────────── */}
      <div className="bg-primary px-md py-sm flex flex-col items-center text-center">
        <Icon name="how_to_vote" fill size={32} className="text-secondary-fixed mb-xs" />
        <p className="text-label-md text-white font-semibold tracking-wide">{t.title}</p>
        <p className="text-caption text-white/60 mt-xs uppercase tracking-widest">{t.subtitle}</p>
      </div>

      {/* ── Split YES / NO panels ─────────────────────────── */}
      <div className={['flex h-40 transition-opacity duration-300', isLocked ? 'opacity-40 pointer-events-none' : ''].join(' ')}>
        {/* YES */}
        <button
          type="button"
          onClick={() => handleSelectVote('yes')}
          aria-pressed={vote === 'yes'}
          disabled={isLocked}
          className={[
            'flex-1 flex flex-col items-center justify-center gap-xs transition-all duration-300 select-none',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-secondary',
            vote === 'yes'  ? 'bg-secondary text-on-secondary'
            : vote === 'no' ? 'bg-surface-container text-on-surface-variant opacity-50'
                            : 'bg-surface-container-low text-secondary hover:bg-secondary/10 cursor-pointer',
          ].join(' ')}
        >
          <Icon name="thumb_up" fill={vote === 'yes'} size={36} className={vote === 'yes' ? 'text-on-secondary' : 'text-secondary'} />
          <span className="text-label-md font-semibold uppercase tracking-wider">{t.yes}</span>
          {vote === 'yes' && <Icon name="check_circle" fill size={18} className="text-on-secondary/70" />}
        </button>

        <div className="w-px bg-outline-variant self-stretch" aria-hidden="true" />

        {/* NO */}
        <button
          type="button"
          onClick={() => handleSelectVote('no')}
          aria-pressed={vote === 'no'}
          disabled={isLocked}
          className={[
            'flex-1 flex flex-col items-center justify-center gap-xs transition-all duration-300 select-none',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-error',
            vote === 'no'   ? 'bg-error text-on-error'
            : vote === 'yes'? 'bg-surface-container text-on-surface-variant opacity-50'
                            : 'bg-surface-container-low text-error hover:bg-error/10 cursor-pointer',
          ].join(' ')}
        >
          <Icon name="thumb_down" fill={vote === 'no'} size={36} className={vote === 'no' ? 'text-on-error' : 'text-error'} />
          <span className="text-label-md font-semibold uppercase tracking-wider">{t.no}</span>
          {vote === 'no' && <Icon name="cancel" fill size={18} className="text-on-error/70" />}
        </button>
      </div>

      {/* ── Confirm button ────────────────────────────────── */}
      {phase === 'select' && vote && (
        <div className="bg-surface-container-highest flex justify-center p-md border-t border-outline-variant">
          <button
            type="button"
            onClick={handleConfirm}
            className="flex items-center gap-sm px-lg py-sm rounded-md bg-primary text-on-primary text-label-md font-semibold hover:shadow-md transition-all active:scale-95"
          >
            <Icon name="send" size={18} />
            {t.confirm}
          </button>
        </div>
      )}

      {/* ── Spinner (cert check / auth request) ───────────── */}
      {loadingLabel && (
        <div className="bg-surface-container-highest border-t border-outline-variant px-md py-lg flex flex-col items-center gap-sm text-center">
          <Icon name="refresh" size={28} className="text-secondary animate-spin" />
          <p className="text-label-md text-on-surface-variant">{loadingLabel}</p>
        </div>
      )}

      {/* ── FNMT cert error ───────────────────────────────── */}
      {phase === 'cert-invalid' && <CertInvalidError t={tCert} />}

      {/* ── Auth request error ────────────────────────────── */}
      {phase === 'error' && (
        <div className="bg-error-container border-t border-outline-variant px-md py-md flex flex-col items-center gap-md text-center">
          <Icon name="block" fill size={36} className="text-error" />
          <p className="text-body-md text-on-error-container">{tOtp.authError}</p>
          <button type="button" onClick={handleRetry} className="flex items-center gap-xs text-label-md text-secondary hover:underline">
            <Icon name="refresh" size={16} />
            {tOtp.retry}
          </button>
        </div>
      )}

      {/* ── Vote POST error ───────────────────────────────── */}
      {phase === 'vote-error' && (
        <div className="bg-error-container border-t border-outline-variant px-md py-md flex flex-col items-center gap-md text-center">
          <Icon name="event_busy" fill size={36} className="text-error" />
          <p className="text-body-md text-on-error-container">{t.voteError}</p>
          <button type="button" onClick={handleRetry} className="flex items-center gap-xs text-label-md text-secondary hover:underline">
            <Icon name="refresh" size={16} />
            {tOtp.retry}
          </button>
        </div>
      )}

      {/* ── Duplicate vote (409) ──────────────────────────── */}
      {phase === 'already-voted' && (
        <div className="bg-error-container border-t border-outline-variant px-md py-md flex flex-col items-center gap-md text-center">
          <Icon name="how_to_vote" fill size={36} className="text-error" />
          <p className="text-body-md text-on-error-container">{t.duplicateVote}</p>
        </div>
      )}

      {/* ── OTP verification panel ────────────────────────── */}
      {phase === 'awaiting-otp' && authId && (
        <OtpVerificationPanel
          initialAuthId={authId}
          onSubmit={handleOtpSubmit}
          onResend={handleResendAuth}
          onSuccess={() => { setPhase('done'); announce(t.registered, 'polite'); }}
          onFatalError={(errorCode) => {
            if (errorCode === 'already_voted') {
              setPhase('already-voted');
              announce(t.duplicateVote, 'assertive');
            } else {
              setPhase('vote-error');
              announce(t.voteError, 'assertive');
            }
          }}
          t={tOtp}
        />
      )}

    </div>
  );
}
