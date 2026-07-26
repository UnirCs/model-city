---
title: Add a brand-new screen
sidebar_label: Add a new screen
sidebar_position: 7
---

# Add a brand-new screen

**Goal:** add a page that upstream does not ship — a city-only route — under an
existing module's URL space.

- **New file:** `overrides/leisure/routes/festivals/page.js`
- **Regen needed:** **yes** — there is no upstream shim, so the codegen must emit one.

The codegen walks the **union** of `packages/<id>/routes` and
`overrides/<id>/routes`; an override-only path is an **add**.

## Recipe

```js
// overrides/leisure/routes/festivals/page.js
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import FestivalsGrid from '@modelcity/leisure/components/organisms/FestivalsGrid';

export const dynamic = 'force-dynamic';

export default async function FestivalsPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'leisure');
  return <FestivalsGrid heading={dict.leisure.festivals?.title ?? 'Festivals'} />;
}
```

Add a dynamic child too if you need one:

```js
// overrides/leisure/routes/festivals/[slug]/page.js
export const dynamic = 'force-dynamic';
export default async function FestivalDetail({ params }) {
  const { lang, slug } = await params;
  /* … */
}
```

`gen:modules` logs `override route (add): leisure/routes/festivals/page` (and the
`[slug]` child). The page is reachable at `/{lang}/festivals`.

## Notes

- Because it lives under the `(leisure)` route group, the new route is **gated by the
  same module flag and session gate** as the rest of leisure.
- To make it appear in the menus, also
  [override the navigation contributor](./override-navigation.md); to localise it,
  [add i18n keys](./override-i18n-dictionary.md).
- Put the `FestivalsGrid` component under `overrides/leisure/components/organisms/`
  (see [Add a city-local component](./add-a-city-local-component.md)) — not under
  `routes/`.

## Verify

`npm run gen:modules` prints the `add` log line; `npm run dev` serves the page at
`/{lang}/festivals`.
