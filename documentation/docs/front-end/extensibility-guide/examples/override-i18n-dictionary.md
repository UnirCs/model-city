---
title: Override an i18n dictionary
sidebar_label: Override i18n copy
sidebar_position: 13
---

# Override an i18n dictionary

**Goal:** change wording or add new keys for the city — here rename the events
section and add a `festivals` namespace — by overriding the locale JSON.

- **Override files:** `overrides/leisure/lib/i18n/locales/{es,en,fr}/leisure.json`
- **Regen needed:** no (pure copy override). Adding a *new namespace file* also needs
  no regen unless you register it via a module i18n loader override.

The dictionary engine composes a `common` namespace with per-module namespaces loaded
lazily ([Internationalisation](../../architecture/i18n.md)). Locale JSON lives at
`packages/<module>/lib/i18n/locales/{es,en,fr}/<namespace>.json`.

:::caution[i18n is whole-file]

There is no per-key merge yet. **Copy the upstream JSON and edit** — an override
replaces the entire file, so a partial file drops every key you didn't include.

:::

## Recipe — change existing copy

```jsonc
// overrides/leisure/lib/i18n/locales/es/leisure.json  (copy upstream, then edit)
{
  "events": {
    "sectionTitle": "Agenda cultural de Aranjuez",   // ← reworded
    "bannerTitle": "Qué hacer en Aranjuez",
    "viewDetails": "Ver detalles"
    // …keep every other key the upstream file had…
  }
}
```

Do the same for `en/leisure.json` and `fr/leisure.json` (all three locales are
mandatory).

## Recipe — add a new namespace

1. Add the JSON for each locale:
   `overrides/leisure/lib/i18n/locales/{es,en,fr}/festivals.json`.
2. If a component calls `getDictionary(lang, 'festivals')`, register the loader by
   overriding the module's i18n index
   (`overrides/leisure/lib/i18n/index.js`) to add the `festivals` loader for each
   locale (copy the upstream index and append):
   ```js
   es: {
     leisure:   () => import('./locales/es/leisure.json').then((m) => m.default),
     tourism:   () => import('./locales/es/tourism.json').then((m) => m.default),
     festivals: () => import('./locales/es/festivals.json').then((m) => m.default), // ← new
   },
   // repeat for en, fr
   ```

## Verify

`npm run dev`; the reworded strings show on `/{lang}/events`, and (for the new
namespace) `getDictionary(lang, 'festivals')` returns your keys.
