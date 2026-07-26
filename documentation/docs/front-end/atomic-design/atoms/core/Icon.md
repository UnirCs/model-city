---
title: Icon
sidebar_label: Icon
sidebar_position: 9
---

# Icon

`packages/core/components/atoms/Icon.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

A thin wrapper for **Material Symbols Outlined**. Renders the ligature `name` inside
a `material-symbols-outlined` span, with size and optional fill controlled through
`font-variation-settings`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `name` | `string` | yes | — | Material Symbols ligature (e.g. `arrow_back`, `delete`). |
| `fill` | `boolean` | no | `false` | Enables the filled variant (`'FILL' 1`). |
| `size` | `number` | no | `24` | Font size in px; also drives the optical size (`opsz`). |
| `className` | `string` | no | `''` | Extra classes appended after `material-symbols-outlined`. |

## Behaviour

Sets inline `fontSize` and
`fontVariationSettings: 'FILL' <0|1>, 'wght' 300, 'GRAD' 0, 'opsz' <size>`.

## Composition

- **Uses:** native `<span>` + the Material Symbols font (loaded globally).
- **Used by:** nearly every interactive component with an icon (buttons, nav items,
  headers, cards).

## Internationalisation

None.

## Accessibility

Marked `aria-hidden="true"` — icons are decorative here; the accessible name comes
from the surrounding control's label or a sibling `VisuallyHidden`.

## Styling & tokens

Colour is inherited (`currentColor`), so callers set `text-*` on the `Icon` or its
parent.

## Usage

```jsx
<Icon name="delete" size={18} className="text-error" />
```

## Related

`atoms/core/Button`, `atoms/core/BackButton`, `atoms/core/VisuallyHidden`.
