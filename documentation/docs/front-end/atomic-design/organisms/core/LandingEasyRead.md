---
title: LandingEasyRead
sidebar_label: LandingEasyRead
sidebar_position: 4
---

# LandingEasyRead

`packages/core/components/organisms/LandingEasyRead.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

A plain-language summary of the portal for users with cognitive disabilities or low
literacy (WCAG 2.2 SC 3.1.5, Reading Level). It renders a heading, an intro and a
checklist of four easy-read lines.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | The landing dictionary (reads `t.easyRead.{title,intro,p1..p4}`). |

## Composition

- **Uses:** `atoms/core/Icon` (`check_circle`).
- **Used by:** the public landing page (`core/routes-public/page`).

## Internationalisation

All copy from the `landing` dictionary's `easyRead` slice.

## Accessibility

`<section aria-labelledby>` with a constrained `max-w-[72ch]` measure for
readability; satisfies SC 3.1.5.

## Styling & tokens

`bg-surface`, `border-outline-variant`, `text-h2`, `text-body-md`.

## Related

`organisms/core/LandingBanner`, `organisms/core/LandingServices`,
`organisms/core/LandingFooter`.
