---
title: LandingBanner
sidebar_label: LandingBanner
sidebar_position: 3
---

# LandingBanner

`packages/core/components/organisms/LandingBanner.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

The full-bleed three-photo diagonal banner of the public landing page, with the
oversized title, subtitle, login CTA and the FNMT digital-certificate note. Known
acronyms in the FNMT note are wrapped in `<abbr>` (via a `withAbbr` helper) for SC
3.1.4.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | The landing dictionary (`t.banner.*`). |
| `lang` | `string` | yes | — | For the login CTA `returnTo`. |
| `abbr` | `Record<string,string>` | yes | — | Acronym expansions (`a11y.abbr`) used by the FNMT note. |

## Composition

- **Uses:** `molecules/core/LoginButton`, `atoms/core/Icon`, `atoms/core/Abbr`.
- **Used by:** the public landing page.

## Internationalisation

Copy from the `landing` dictionary; acronym expansions from `a11y.abbr`.

## Accessibility

`<section aria-labelledby="landing-title">` with a single `<h1>`; all decorative
photos/gradients are `aria-hidden`; the FNMT acronym is wrapped in `Abbr` (SC 3.1.4).

## Styling & tokens

Layered `object-cover` images with `clip-path` diagonals, readability gradient,
`clamp()` title, white CTA button.

## Related

`organisms/core/LandingServices`, `organisms/core/LandingEasyRead`,
`organisms/core/LandingFooter`, `molecules/core/LoginButton`, `atoms/core/Abbr`.
