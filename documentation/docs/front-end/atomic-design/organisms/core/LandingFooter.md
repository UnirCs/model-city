---
title: LandingFooter
sidebar_label: LandingFooter
sidebar_position: 5
---

# LandingFooter

`packages/core/components/organisms/LandingFooter.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

The slim footer of the public landing page: the municipal address, the "powered by"
attribution (linking the platform GitHub repo), the legal/help links (legal,
cookies, accessibility, contact) and the copyright line.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | The landing dictionary (reads `t.footer.*`). |
| `lang` | `string` | yes | — | Active locale. |
| `brand` | `string` | yes | — | The brand name (`common.brand`). |

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`.
- **Used by:** the public landing page.

## Internationalisation

All copy from the `landing` dictionary's `footer` slice.

## Accessibility

The footer links live in a `<nav aria-label>`; the external attribution link uses
`rel="noopener noreferrer"`.

## Styling & tokens

`bg-surface-container`, `border-outline-variant`, `text-caption`,
`max-w-container-max`.

## Related

`organisms/core/LandingBanner`, `organisms/core/LandingServices`,
`organisms/core/LandingEasyRead`.
