---
title: Skeleton
sidebar_label: Skeleton
sidebar_position: 11
---

# Skeleton

`packages/core/components/atoms/Skeleton.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

The base placeholder block used to compose loading UIs. Renders a single element in
the design-system `surface-container-high` colour with Tailwind's `animate-pulse`.
Purely decorative, so it is always `aria-hidden`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `className` | `string` | no | `''` | Extra Tailwind classes (sizing, spacing). |
| `width` | `string \| number` | no | — | Explicit width (px when a number). |
| `height` | `string \| number` | no | — | Explicit height (px when a number). |
| `rounded` | `'md' \| 'full'` | no | `'md'` | Border-radius token (ignored when `variant='circle'`). |
| `variant` | `'rect' \| 'circle'` | no | `'rect'` | `circle` forces `rounded-full`. |
| `as` | `keyof JSX.IntrinsicElements` | no | `'div'` | HTML tag to render. |

## Composition

- **Uses:** a single dynamic tag (`as`).
- **Used by:** the shared skeleton molecules under
  `core/components/molecules/skeletons/` (CardSkeleton, ListSkeleton, TableSkeleton,
  FormSkeleton, MapSkeleton, PageHeaderSkeleton, DetailHeaderSkeleton,
  StatusBarSkeleton) and any `loading.js` fallback.

## Internationalisation

None.

## Accessibility

Always `aria-hidden="true"`; conveys no information to assistive tech (the loading
state is communicated elsewhere).

## Styling & tokens

`bg-surface-container-high`, `animate-pulse`, `rounded-md` / `rounded-full`.

## Usage

```jsx
<Skeleton height={20} width="60%" />
<Skeleton variant="circle" width={40} height={40} />
```

## Related

The `skeletons/*` molecules that compose it, and the
[Rendering](../../../architecture/rendering.md) `loading.js` model.
