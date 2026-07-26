---
title: SkipLink
sidebar_label: SkipLink
sidebar_position: 12
---

# SkipLink

`packages/core/components/atoms/SkipLink.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

A visually-hidden navigation link that becomes visible on keyboard focus, letting
keyboard and screen-reader users bypass repetitive blocks (top nav, brand bar) and
jump straight to the page's main content. The target must exist as an element with
the matching id and (recommended) `tabIndex={-1}` so it can receive programmatic
focus.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `href` | `string` | no | `'#main'` | In-document fragment target. |
| `children` | `ReactNode` | yes | — | Link text (e.g. "Skip to content"). |
| `className` | `string` | no | `''` | Extra classes appended last. |

## Composition

- **Uses:** a plain `<a>` — the target is an in-document fragment, never a route, so
  `LocalizedLink` is intentionally not used.
- **Used by:** the root/app layout, pointing at the `AppShell` `<main id="main">`
  landmark.

## Internationalisation

None directly — the label is passed by the caller (typically from the `a11y`
dictionary slice).

## Accessibility

Implements WCAG 2.2 SC 2.4.1 (Bypass Blocks, Level A). Hidden via `sr-only` and
revealed with `focus:not-sr-only`, gaining a fixed, high-contrast, focus-outlined
appearance on focus.

## Styling & tokens

`sr-only` / `focus:not-sr-only`, `focus:bg-primary`/`focus:text-on-primary`,
`focus:outline-2`, `focus:z-100`.

## Usage

```jsx
<SkipLink href="#main">{dict.a11y.skipToContent}</SkipLink>
```

## Related

`atoms/core/VisuallyHidden`, `templates/core/AppShell` (the `#main` landmark).
