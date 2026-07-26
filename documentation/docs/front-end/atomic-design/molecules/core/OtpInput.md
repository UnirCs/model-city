---
title: OtpInput
sidebar_label: OtpInput
sidebar_position: 21
---

# OtpInput

`packages/core/components/molecules/OtpInput.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A controlled row of individual digit boxes for one-time-password entry. It handles
keyboard navigation (arrows, backspace), paste and auto-advance, and calls
`onComplete` as soon as all boxes are filled. It is intentionally **stateless** —
all digit state is owned by the parent.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `digits` | `string[]` | yes | — | Controlled digit values (length = `length`). |
| `onChange` | `(digits: string[]) => void` | yes | — | Called on every edit/paste. |
| `onComplete` | `(digits: string[]) => void` | yes | — | Called once all boxes are filled. |
| `length` | `number` | no | `6` | Number of digit boxes. |
| `disabled` | `boolean` | no | `false` | Disables all inputs. |
| `notice` | `string \| null` | no | `null` | When set, switches the boxes to a warning (orange) palette. |

## Behaviour

Non-digits are stripped. Typing auto-advances; Backspace clears and steps back;
Arrow keys move focus; paste distributes up to `length` digits and fires
`onComplete` when the paste fills all boxes. The first box uses
`autoComplete="one-time-code"`.

## Composition

- **Uses:** native `<input>` boxes + `useRef` for focus management.
- **Used by:** `organisms/core/OtpVerificationPanel`, `molecules/engagement/VotingZone`.

## Internationalisation

None directly (the optional `notice` string is supplied by the caller).

## Accessibility

`inputMode="numeric"`, per-box `focus-visible` outline that overrides border styles
so the focus indicator is never lost (WCAG 2.4.7 / 2.4.13).

## Styling & tokens

`text-h3` digit boxes, `border-primary`/`border-outline`, warning `orange-*`
palette when `notice` is set.

## Usage

```jsx
<OtpInput digits={digits} onChange={setDigits} onComplete={submit} />
```

## Related

`organisms/core/OtpVerificationPanel`, `molecules/engagement/VotingZone`.
