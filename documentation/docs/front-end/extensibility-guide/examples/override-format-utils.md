---
title: Override formatting utilities
sidebar_label: Override format utils
sidebar_position: 17
---

# Override formatting utilities

**Goal:** change how a module formats values — here render prices and event
date-times the city way — by overriding the module's single utility file.

- **Override file:** `overrides/leisure/lib/utils/format.js`
- **Regen needed:** no.

Each module keeps its formatting/utility helpers in one `lib/utils/format.js` (used by
cards and lists, e.g. `EventsList`, `CityPlaceCard`). Overriding it changes every
consumer at once.

## Recipe

Keep the **same exported function names and signatures** (`formatEventDateTime`,
`formatPrice`, `formatDuration`, `categoryIcon`, `categoryLabel`, …) so every importer
keeps working:

```js
// overrides/leisure/lib/utils/format.js
export function formatEventDateTime(iso, lang) {
  // City style: weekday + short date + 24h time
  return new Intl.DateTimeFormat(lang, {
    weekday: 'short', day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit', hour12: false,
  }).format(new Date(iso));
}

export function formatPrice(amount, currency, lang, freeLabel) {
  if (!amount) return freeLabel;
  return new Intl.NumberFormat(lang, { style: 'currency', currency: currency ?? 'EUR' })
    .format(amount / 100); // amounts are in cents
}

// …re-declare the remaining helpers the module originally exported
// (formatDuration, categoryIcon, categoryLabel, …) — copy them from upstream…
```

:::caution[Keep the whole surface]

An override replaces the whole file. Cards import several helpers from it; re-declare
(or copy) **all** the functions the upstream file exported, or the ones you omit
resolve to `undefined` at their call sites.

:::

## Verify

`npm run dev`; event cards and lists show the new date/price format across the leisure
module.
