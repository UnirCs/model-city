---
title: Restyle a design-system component
sidebar_label: Restyle a component
sidebar_position: 1
---

# Restyle a design-system component

**Goal:** change the look of a shared atom — here the design-system
[`Button`](../../atomic-design/atoms/core/Button.md) — for the whole city, without
touching upstream.

- **Override file:** `overrides/core/components/atoms/Button.js`
- **Regen needed:** no — resolution swaps it at import time.

Every importer of `@modelcity/core/components/atoms/Button` (forms, modals, panels,
every module) picks up the city version automatically.

## Recipe

Copy `packages/core/components/atoms/Button.js` to the mirrored path and edit it.
Keep the **same default export and prop surface** (`variant`, `size`, `className`,
`children`, spread native attrs) so every call site keeps working.

```js
// overrides/core/components/atoms/Button.js
const variants = {
  // Aranjuez brand — rounded-full pills, brand gradient on primary
  primary:
    'bg-gradient-to-r from-primary to-secondary text-on-primary shadow-sm hover:opacity-90',
  secondary: 'bg-secondary text-on-secondary hover:opacity-90',
  outline:   'border border-primary text-primary hover:bg-primary/10',
  ghost:     'text-primary hover:bg-surface-container',
  error:     'bg-error text-on-error hover:opacity-90',
  'outline-error': 'border border-error text-error hover:bg-error-container/30',
};

const sizes = {
  sm: 'px-sm py-xs text-label-md rounded-full',   // ← pill instead of rounded-md
  md: 'px-md py-sm text-label-md rounded-full',
  lg: 'px-lg py-md text-body-md rounded-full',
};

export default function Button({ variant = 'primary', size = 'md', className = '', children, ...props }) {
  return (
    <button
      className={[
        'inline-flex items-center justify-center gap-xs font-semibold transition-all duration-200 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer',
        variants[variant] ?? variants.primary,
        sizes[size] ?? sizes.md,
        className,
      ].join(' ')}
      {...props}
    >
      {children}
    </button>
  );
}
```

## Notes

- Prefer restyling with the existing **design tokens** (`bg-primary`, `px-md`, …) so
  light/dark theming keeps working — see
  [Rebrand the design tokens](./override-globals-css.md) to change the tokens
  themselves.
- Override the **smallest** component that isolates your change. Restyling `Button`
  once ripples everywhere; overriding a whole page just to tweak a button is wasteful
  and forfeits upstream fixes to that page.

## Verify

`npm run dev`, then confirm any screen with a button renders the city style and a
non-overridden sibling (e.g. an `Icon`) is unchanged.
