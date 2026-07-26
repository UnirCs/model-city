---
title: PhotoGallery
sidebar_label: PhotoGallery
sidebar_position: 9
---

# PhotoGallery

`packages/leisure/components/molecules/PhotoGallery.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

A responsive grid of thumbnails. Clicking a thumbnail opens a full-screen overlay
with the photo at full resolution, a blurred backdrop and a close button; prev/next
arrows navigate when there is more than one photo. Escape and outside-click also
close. Returns `null` when there are no photos.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `photos` | `string[]` | yes | — | Image URLs. |
| `alt` | `string` | yes | — | Base alt text (indexed per photo). |
| `closeLabel` | `string` | no | `'Close'` | Overlay close `aria-label`. |
| `prevLabel` | `string` | no | `'Previous photo'` | Prev `aria-label`. |
| `nextLabel` | `string` | no | `'Next photo'` | Next `aria-label`. |

## Behaviour

Two-flag mount/visible state drives an opacity+scale open/close animation
(`TRANSITION_MS = 300`). Keyboard: Escape closes, Arrow Left/Right navigate.

## Composition

- **Uses:** `atoms/core/Icon`, `useState`/`useEffect`/`useCallback`.
- **Used by:** `organisms/leisure/PlaceDetailView`, `organisms/leisure/SpaceDetailView`.

## Internationalisation

The overlay control labels come from the caller (defaults are English).

## Accessibility

Thumbnails are labelled buttons with `focus-visible` rings; the overlay is a
`role="dialog" aria-modal` with keyboard navigation and a photo counter.

## Styling & tokens

`aspect-4/3` thumbnails, `bg-primary/60 backdrop-blur-sm` backdrop, `rounded-md`,
scale/opacity transitions.

## Related

`molecules/leisure/PlaceMap` (shares the expand-overlay language),
`organisms/leisure/PlaceDetailView`.
