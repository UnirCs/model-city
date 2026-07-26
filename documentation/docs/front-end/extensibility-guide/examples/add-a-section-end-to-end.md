---
title: Add a whole new section (end-to-end)
sidebar_label: Add a section (end-to-end)
sidebar_position: 19
---

# Add a whole new section (end-to-end)

**Goal:** ship a city-only *Neighbourhoods* section under leisure that upstream does
not have — the route(s), the copy and the navigation — combining several overrides.

- **Regen needed:** **yes** (new routes).

This example ties together the route, i18n and navigation recipes into one feature.
The golden rule holds throughout: **mirror the upstream path under `overrides/` and
edit**.

## 1. The route(s) — an `add`

```js
// overrides/leisure/routes/neighbourhoods/page.js
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import NeighbourhoodGrid from '@modelcity/leisure/components/organisms/NeighbourhoodGrid';

export const dynamic = 'force-dynamic';

export default async function NeighbourhoodsPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'neighbourhoods');
  return <NeighbourhoodGrid heading={dict.neighbourhoods.title} />;
}
```

```js
// overrides/leisure/routes/neighbourhoods/[slug]/page.js
export const dynamic = 'force-dynamic';
export default async function NeighbourhoodDetail({ params }) {
  const { lang, slug } = await params;
  /* … */
}
```

Put `NeighbourhoodGrid` under
`overrides/leisure/components/organisms/NeighbourhoodGrid.js`
([city-local component](./add-a-city-local-component.md)), **not** under `routes/`.

## 2. The copy — a new namespace

Add `overrides/leisure/lib/i18n/locales/{es,en,fr}/neighbourhoods.json` and register
its loader by overriding `overrides/leisure/lib/i18n/index.js`
(see [Override i18n copy](./override-i18n-dictionary.md)).

## 3. The navigation

Override `overrides/leisure/lib/nav/sections.js` to add the section/item
(see [Override navigation](./override-navigation.md)).

## 4. Regenerate

```bash
npm run gen:modules
```

Logs `override route (add): leisure/routes/neighbourhoods/page` (and the `[slug]`
child). The section is reachable at `/{lang}/neighbourhoods`, fully localised and
linked from the nav — without forking a single upstream file.

## Verify

`npm run dev`; the section appears in the menus, both pages render, and the copy is
localised in `es`/`en`/`fr`.
