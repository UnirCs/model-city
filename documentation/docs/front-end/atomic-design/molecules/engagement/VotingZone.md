---
title: VotingZone
sidebar_label: VotingZone
sidebar_position: 8
---

# VotingZone

`packages/engagement/components/molecules/VotingZone.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

Orchestrates the full citizen voting flow for a consultation:

1. YES / NO selection.
2. Browser-side digital-certificate verification (TLS client auth via the ALB).
3. Server Action → `POST /transaction-authorization/operation-authorizations`.
4. [`OtpVerificationPanel`](../../organisms/core/OtpVerificationPanel.md) — all OTP
   entry and error states.
5. On OTP success → the vote is cast via `confirmVoteWithOtp`.

OTP-specific UI/state is fully delegated to `OtpVerificationPanel`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | Voting-zone dictionary. |
| `tOtp` | `object` | yes | — | OTP-panel dictionary. |
| `tCert` | `object` | yes | — | Certificate-error dictionary. |
| `isActive` | `boolean` | yes | — | Whether the consultation accepts votes. |
| `questionId` | `number \| string` | yes | — | The consultation. |
| `accessToken` | `string` | yes | — | Bearer token (also used for the browser mTLS cert call). |

## Behaviour

An internal phase machine (`select` → `checking-cert` → `cert-invalid` /
`requesting-auth` → OTP → cast) drives the UI. Certificate verification goes straight
from the browser to the ALB (see [Data & API](../../../architecture/data-and-api.md)).

## Composition

- **Uses:** `organisms/core/OtpVerificationPanel`,
  `molecules/engagement/CitizenOnlyNotice`, `molecules/core/CertInvalidError`,
  `atoms/core/Icon`, `useAnnounce`, the vote/authorization actions + `certClient`.
- **Used by:** `organisms/engagement/QuestionDetailView`.

## Internationalisation

Copy via `t` / `tOtp` / `tCert`; results announced through the live region.

## Accessibility

YES/NO are labelled controls; OTP entry delegates to the accessible
`OtpVerificationPanel`; cert failures render `CertInvalidError`.

## Related

`organisms/core/OtpVerificationPanel`, `molecules/core/OtpInput`,
`molecules/core/CertInvalidError`, [Local mTLS](../../../../how-to-start/mtls-local.md).
