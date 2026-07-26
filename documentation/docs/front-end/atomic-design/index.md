---
title: Atomic Design — component reference
sidebar_label: Overview
sidebar_position: 1
---

# Atomic Design — component reference

Reference pages for every React component in the platform packages. Each page
mirrors the source path and documents only what the source does: props, variants,
rendering mode, i18n, a11y and styling. The pages are verified **against the real
`packages/*` source**, not against a prior spec.

For the design system as an architecture concern (tiers, tokens, theming, a11y
posture) see [Design system & tokens](../architecture/design-system.md).

## How this section is organised

Components follow **atomic design**, grouped by tier and, within each tier, by
module:

| Tier | What it is | Path |
| --- | --- | --- |
| **Atoms** | Smallest primitives | `packages/<module>/components/atoms` |
| **Molecules** | Small compositions (maps, skeletons, modals) | `packages/<module>/components/molecules` |
| **Organisms** | Self-contained sections (nav bars, forms, views) | `packages/<module>/components/organisms` |
| **Templates** | Page scaffolds — currently `AppShell` | `packages/core/components/templates` |
| **Providers** | Global context (`ThemeProvider`) | `packages/core/components/providers` |

Modules: **core** (always-on shared) and the feature modules **leisure**,
**engagement**, **mobility**.

### Rendering convention

A component is a **Client Component** when its source file begins with
`'use client'`; otherwise it is a **Server Component**. Each page states which it
is.