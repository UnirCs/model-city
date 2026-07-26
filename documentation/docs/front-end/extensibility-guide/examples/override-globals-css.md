---
title: Rebrand the design tokens (globals.css)
sidebar_label: Override globals.css
sidebar_position: 12
---

# Rebrand the design tokens (globals.css)

**Goal:** rebrand the whole city — change the primary/secondary palette, radii or
type scale — by overriding the shared stylesheet, so every component re-themes at
once.

- **Override file:** `overrides/core/styles/globals.css`
- **Regen needed:** no.

`packages/core/styles/globals.css` defines the `--ds-*` CSS variables for light
(`:root`) and dark (`.dark`) and maps them to semantic Tailwind v4 utilities via
`@theme inline`. Because components reference the **semantic names** (`bg-primary`,
`text-on-surface`, `px-md`, `text-h1`, …), changing the variables re-themes the app
without touching a single component.

## Recipe

Copy the upstream `globals.css` and edit the token block (keep the Tailwind imports
and the `@theme inline` mapping intact):

```css
/* overrides/core/styles/globals.css */
@import 'tailwindcss';
/* …keep the upstream @theme inline mapping and any plugin imports… */

:root {
  /* Aranjuez brand palette */
  --ds-primary: #6a1b2a;              /* was #002045 */
  --ds-on-primary: #ffffff;
  --ds-secondary: #b08d57;            /* warm gold */
  --ds-on-secondary: #1a1206;
  --ds-surface: #fbf7f2;
  --ds-on-surface: #211c17;
  /* …keep every other --ds-* the upstream file defines… */

  /* Optional: rounder corners city-wide */
  --radius-md: 14px;
}

.dark {
  --ds-primary: #e8a9b3;
  --ds-on-primary: #3a0c15;
  --ds-surface: #17120e;
  --ds-on-surface: #ece3da;
  /* …dark counterparts for every token… */
}
```

## Notes

- **Copy the whole file, then edit** — an override replaces it entirely, so you must
  keep the Tailwind `@import`, the `@theme inline` block and **all** the `--ds-*`
  variables (define every token the upstream file did, or utilities that reference a
  missing token break).
- Re-run the platform's contrast check (WCAG 2.2 AAA target) after changing colours to
  make sure text/background pairs still meet ≥7:1 — see
  [Design system & tokens](../../architecture/design-system.md#accessibility-posture).
- To change only a component's look (not the palette), override that component instead
  — see [Restyle a component](./restyle-a-component.md).

## Verify

`npm run dev`; the whole UI adopts the new palette in both light and dark mode, and
component overrides are not needed.
