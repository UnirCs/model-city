---
title: UnauthorizedView
sidebar_label: UnauthorizedView
sidebar_position: 18
---

# UnauthorizedView

`packages/core/components/organisms/UnauthorizedView.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

A full-page inline error surface shown when a user tries to access a route their
role does not permit. It receives the already-resolved `t` (the `unauthorized`
dictionary slice) and the current `lang`, so it builds the localized home link
without needing the translations context (works in server components).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | `unauthorized` slice (`gateTitle`, `gateSubtitle`, `gateBody`, `gateHint`, `backHome`). |
| `lang` | `string` | yes | — | Active locale (for the home link). |

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`.
- **Used by:** role-gated pages that render it when a capability check fails
  (see [Auth & roles](../../../architecture/auth-and-roles.md)).

## Internationalisation

All copy from the `unauthorized` dictionary slice.

## Accessibility

Renders a single page `<h1>`; decorative illustration elements are `aria-hidden`; a
prominent primary "back home" link is provided.

## Styling & tokens

`bg-error-container/40` glow, `bg-surface-container` hint plaque, `bg-primary` CTA,
`rounded-md`.

## Related

`molecules/core/EmptyState`, [Auth & roles](../../../architecture/auth-and-roles.md).
