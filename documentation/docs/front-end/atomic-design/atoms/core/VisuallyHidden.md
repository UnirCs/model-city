---
title: VisuallyHidden
sidebar_label: VisuallyHidden
sidebar_position: 13
---

# VisuallyHidden

`packages/core/components/atoms/VisuallyHidden.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

Renders content that is invisible on screen but exposed to assistive technologies.
Use it to add context to icon-only buttons, to disambiguate links that share the
same visible label ("Read more"), or to supply a unique accessible name. It mirrors
the WHATWG-recommended `sr-only` pattern.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `as` | `keyof JSX.IntrinsicElements` | no | `'span'` | HTML tag to render. |
| `children` | `ReactNode` | yes | — | Screen-reader-only content. |

## Composition

- **Uses:** a single dynamic tag with the `sr-only` class.
- **Used by:** icon-only controls throughout the UI (paired with `Icon`).

## Internationalisation

None directly — the (localized) text is passed by the caller.

## Accessibility

The whole point of the atom: content stays in the accessibility tree while being
removed from the visual layout (SC 1.1.1 / 4.1.2 support for icon-only controls).

## Styling & tokens

`sr-only` only.

## Usage

```jsx
<button>
  <Icon name="delete" />
  <VisuallyHidden>{item.title}</VisuallyHidden>
</button>
```

## Related

`atoms/core/Icon`, `atoms/core/SkipLink`, `atoms/core/Abbr`.
