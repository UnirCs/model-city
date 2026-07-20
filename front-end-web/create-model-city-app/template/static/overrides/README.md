# City overrides

This directory mirrors the `@modelcity/<module>/…` package layout. Any file you
create here **replaces** its platform counterpart, and route files that only
exist here are **added** as new city routes:

- `overrides/<module>/routes/…` — replace or add a route (picked up by
  `modelcity gen`, which re-emits the route shims).
- `overrides/<module>/<any other path>` — replace any non-route module file
  (components, lib, styles) through the jsconfig `paths` fallback.

Examples:

```
overrides/core/components/atoms/Button.js     → replaces the core Button
overrides/core/styles/globals.css             → replaces the design tokens
overrides/leisure/routes/tourism/faq/page.js  → adds /​[lang]/tourism/faq
```

Run `npm run gen:modules` (automatic on `dev`/`build`) after adding or removing
route overrides.
