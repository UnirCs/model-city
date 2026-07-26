---
title: QuestionDetailView
sidebar_label: QuestionDetailView
sidebar_position: 3
---

# QuestionDetailView

`packages/engagement/components/organisms/QuestionDetailView.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Server Component

## Purpose

Renders the body of a civic consultation detail page: banner with status badge,
description with a validation-threshold footer, objectives, the consultation period,
the voting zone (or a staff / already-voted notice), vote statistics and the staff
administration controls. The back button is owned by the page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `question` | `object` | yes | — | Consultation payload from the engagement service. |
| `t` | `object` | yes | — | `participation.questionDetail` dictionary. |
| `tOtp` | `object` | yes | — | OTP dictionary (voting flow). |
| `tCert` | `object` | yes | — | Certificate dictionary (voting flow). |
| `lang` | `string` | yes | — | Locale. |
| `staff` | `boolean` | yes | — | User is staff (not a citizen). |
| `canManage` | `boolean` | yes | — | User can manage consultations. |
| `accessToken` | `string` | yes | — | Auth0 Bearer JWT for the client-side cert verification. |

## Composition

- **Uses:** `molecules/engagement/VotingZone`,
  `molecules/engagement/CitizenOnlyNotice`,
  `molecules/engagement/QuestionStatusActions`,
  `molecules/engagement/EditQuestionButton`, `molecules/core/AdminSection`,
  `atoms/core/Icon`.
- **Used by:** the consultation detail page.

## Internationalisation

Copy from `participation.questionDetail` plus the OTP/cert dictionaries.

## Accessibility

Structured sections with headings; citizens get `VotingZone`, staff get
`CitizenOnlyNotice` / admin actions.

## Related

`molecules/engagement/VotingZone`, `organisms/engagement/QuestionsBrowser`,
`organisms/engagement/CreateQuestionForm`.
