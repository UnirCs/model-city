---
title: OtpVerificationPanel
sidebar_label: OtpVerificationPanel
sidebar_position: 9
---

# OtpVerificationPanel

`packages/core/components/organisms/OtpVerificationPanel.js` · **Tier:** organism · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A self-contained, **domain-agnostic** OTP verification flow. It manages its own
current authorization id (starting from `initialAuthId`) and all OTP UI states via
an internal phase machine, so any operation requiring a one-time-password
confirmation can reuse it.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `initialAuthId` | `string` | yes | — | The starting operation authorization id. |
| `otpLength` | `number` | no | `6` | Number of OTP digits. |
| `onSubmit` | `(authId, otp) => Promise<{ ok: true } \| { error }>` | yes | — | Verifies the code. |
| `onResend` | `() => Promise<{ ok: true, operationAuthorizationId } \| { error }>` | yes | — | Requests a fresh code. |
| `onSuccess` | `() => void` | yes | — | Called on successful verification. |
| `onFatalError` | `(errorCode) => void` | yes | — | Called for non-OTP errors. |
| `t` | `object` | yes | — | Localized OTP copy. |

## Phase machine

`idle` (typing) · `submitting` · `resending` · `otp-expired` / `otp-already-used` /
`otp-mismatch` (resendable) · `otp-no-attempts` (terminal) · plus `otp_new_sent` /
`otp_invalid` handled inline by clearing the boxes with a notice. Recoverable errors
(`RESENDABLE_ERRORS`) show a resend button; anything else is forwarded to
`onFatalError`.

## Composition

- **Uses:** `atoms/core/Icon`, `molecules/core/OtpInput`, `useState`/`useRef`/
  `useCallback`.
- **Used by:** OTP-gated flows (e.g. `molecules/engagement/VotingZone`, agent
  invitation confirmation).

## Internationalisation

All copy via `t` (title/subtitle, verifying/requesting, expired/mismatch/etc.,
resend).

## Accessibility

Loading and terminal states show clear icon + message blocks; the input delegates
to `OtpInput`'s keyboard handling.

## Styling & tokens

State-specific tints (`bg-orange-100` warning, `bg-error-container` terminal,
`bg-surface-container-highest`), `bg-secondary` resend button.

## Related

`molecules/core/OtpInput`, `molecules/engagement/VotingZone`.
