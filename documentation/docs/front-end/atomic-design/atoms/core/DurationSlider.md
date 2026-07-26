---
title: DurationSlider
sidebar_label: DurationSlider
sidebar_position: 6
---

# DurationSlider

`packages/core/components/atoms/DurationSlider.js` · **Tier:** atom · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A range slider with a numeric readout and quick-pick chips. Used by the citizen
reservation and renewal flows to pick a parking duration — by default between 20
and 150 minutes in 5-minute increments. The value is clamped into `[min, max]`
before being propagated through `onChange`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `number` | yes | — | Current duration (controlled). |
| `onChange` | `(next: number) => void` | yes | — | Called with the clamped value on slider/chip change. |
| `min` | `number` | no | `20` | Lower bound. |
| `max` | `number` | no | `150` | Upper bound. |
| `step` | `number` | no | `5` | Increment. |
| `quickPicks` | `number[]` | no | `[20, 60, 90, 150]` | Values rendered as quick-pick chips. |
| `formatDuration` | `(minutes: number) => string` | yes | — | Formats a minute count into a display string (readout, scale ends, chips). |
| `label` | `string` | no | — | Field label + slider `aria-label`. |
| `hint` | `string` | no | — | Helper text below the control. |
| `quickPicksLabel` | `string` | no | — | Caption above the chip row. |
| `disabled` | `boolean` | no | `false` | Disables the range input and chips. |

## Variants & states

Selected quick-pick chips get the secondary-container "selected" style; the rest
are outlined. Disabled state dims the control. Custom slider thumb/track styling is
provided by a scoped `styled-jsx` block (with `-webkit-` and `-moz-` variants).

## Composition

- **Uses:** native `<input type="range">`, chip `<button>`s, and a `styled-jsx`
  style block.
- **Used by:** `molecules/mobility/RenewStayModal`, `organisms/mobility/ReservationForm`.

## Internationalisation

No dictionary access; all display strings come in as props (`label`, `hint`,
`quickPicksLabel`) and via `formatDuration`, so the caller localises them.

## Accessibility

The range input carries `aria-label={label}`. The numeric readout is
`tabular-nums` for stable width. Chips are native buttons.

## Styling & tokens

`bg-surface-container`, `border-outline-variant`, `rounded-md`, type-scale tokens;
custom thumb colour `#13696a` in the scoped styles.

## Usage

```jsx
<DurationSlider
  value={minutes}
  onChange={setMinutes}
  formatDuration={(m) => `${m} min`}
  label={t.parking.duration}
/>
```

## Related

`molecules/mobility/RenewStayModal`, `organisms/mobility/ReservationForm`.
