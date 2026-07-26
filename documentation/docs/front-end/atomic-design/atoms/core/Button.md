---
title: Button
sidebar_label: Button
sidebar_position: 5
---

# Button

`packages/core/components/atoms/Button.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

The reusable design-system button. Composes a `variant` colour scheme with a `size`
and forwards all native `<button>` attributes (including `onClick`, `type`,
`disabled`) onto the underlying element.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `variant` | `'primary' \| 'secondary' \| 'outline' \| 'ghost' \| 'error' \| 'outline-error'` | no | `'primary'` | Colour scheme. Unknown → `primary`. |
| `size` | `'sm' \| 'md' \| 'lg'` | no | `'md'` | Padding + type scale. Unknown → `md`. |
| `className` | `string` | no | `''` | Extra classes appended last. |
| `children` | `ReactNode` | yes | — | Button content. |
| `...props` | `ButtonHTMLAttributes` | no | — | Spread onto the native `<button>` (e.g. `onClick`, `type`, `disabled`). |

## Variants & states

`variants` map: `primary`, `secondary`, `outline`, `ghost`, `error`,
`outline-error`. `sizes` map: `sm`, `md`, `lg`. Disabled styling is baked into the
base classes (`disabled:opacity-50 disabled:cursor-not-allowed`).

## Composition

- **Uses:** native `<button>` only.
- **Used by:** widely across forms, modals and panels — e.g. `CreateAgentModal`,
  `SessionExpiredModal`, `RegistrationForm`, `EventForm`, `ReservationForm`,
  `BuyTicketButton`, `DeleteEntityButton`, and many more.

## Internationalisation

None (label supplied via `children`).

## Accessibility

Native button semantics; includes a `focus-visible` outline
(`focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary`).

## Styling & tokens

Semantic colour pairs (`bg-primary`/`text-on-primary`, `border-error`/`text-error`,
etc.), spacing tokens (`px-md`, `py-sm`), type scale (`text-label-md`,
`text-body-md`).

## Usage

```jsx
<Button variant="outline-error" size="sm" onClick={handleDelete}>
  {t.common.delete}
</Button>
```

## Related

`atoms/core/Icon` (commonly placed inside as a leading icon).
