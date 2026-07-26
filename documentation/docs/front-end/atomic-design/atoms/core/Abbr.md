---
title: Abbr
sidebar_label: Abbr
sidebar_position: 1
---

# Abbr

`packages/core/components/atoms/Abbr.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

Thin wrapper around the native `<abbr>` element that exposes the expansion of an
acronym through the `title` attribute, satisfying WCAG 2.2 SC 3.1.4
(Abbreviations). It renders `children` as the visible token and uses `title` as the
spelled-out expansion. If `children` is omitted, the `term` prop is used as the
visible token.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `title` | `string` | yes | — | The spelled-out expansion, surfaced via the native `title` attribute. |
| `term` | `string` | no | — | Fallback visible token used when `children` is not supplied. |
| `children` | `ReactNode` | no | — | Visible token. Takes precedence over `term`. |
| `className` | `string` | no | — | Extra classes on the `<abbr>`. |

## Composition

- **Uses:** native `<abbr>` only.
- **Used by:** inline glossary/acronym expansions across content surfaces.

## Internationalisation

None directly — the visible token and `title` are supplied by the caller (which
typically passes localized strings).

## Accessibility

Native `<abbr title>` semantics; assistive technologies can announce the expansion.
Satisfies SC 3.1.4 (Abbreviations).

## Usage

```jsx
<Abbr title="Documento Nacional de Identidad">DNI</Abbr>
```

## Related

`atoms/core/VisuallyHidden` (another small a11y primitive).
