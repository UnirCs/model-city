---
title: Badge
sidebar_label: Badge
sidebar_position: 4
---

# Badge

`packages/core/components/atoms/Badge.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

A status chip with semantic design-system colours. Maps a `status` key to a colour
pair and a default label; the label can be overridden.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `status` | `'active' \| 'pending' \| 'inactive' \| 'error'` | no | `'active'` | Selects the colour pair and default label. Unknown → `inactive`. |
| `label` | `string` | no | — | Overrides the default label for the status. |
| `className` | `string` | no | `''` | Extra classes appended last. |

## Variants & states

`statusMap`:

| `status` | Default label | Colours |
| --- | --- | --- |
| `active` | `Activo` | `bg-secondary-container` / `text-on-secondary-container` |
| `pending` | `Pendiente` | `bg-tertiary-container` / `text-on-tertiary-container` |
| `inactive` | `Inactivo` | `bg-surface-container-high` / `text-on-surface-variant` |
| `error` | `Error` | `bg-error-container` / `text-on-error-container` |

## Composition

- **Uses:** native `<span>` only.
- **Used by:** user and entity cards/rows (e.g. `UserMiniCard`, admin views).

## Internationalisation

The built-in default labels are Spanish literals; pass a localized `label` to
display translated text.

## Accessibility

Presentational chip; colour is paired with a text label (not colour-only). Meets
the design system's contrast targets.

## Styling & tokens

`rounded-full`, `text-caption`, `px-sm py-xs`, semantic container colour pairs.

## Usage

```jsx
<Badge status="pending" label={t.status.pending} />
```

## Related

`molecules/core/InlineStatus`, `molecules/core/UserMiniCard`.
