---
title: Override the home service catalogue
sidebar_label: Override home catalogue
sidebar_position: 16
---

# Override the home service catalogue

**Goal:** reorder, relabel or add cards on the authenticated home grid — by
overriding the static service catalogue.

- **Override file:** `overrides/core/lib/config/services.js`
- **Regen needed:** no.

The home page renders `ServiceGrid` from a static catalogue in
`core/lib/config/services.js`. Each entry carries `{ id, icon, href }`; the home page
resolves each card's owning module from its `href` (`moduleForPath`) and hides it when
the module is disabled ([Modularity](../../architecture/modularity.md)).

## Recipe

Keep the **same export shape** (an array of `{ id, icon, href }`). Reorder, drop, or
add city entries — a card whose `href` belongs to a disabled module is hidden
automatically, and a card for a **new city route** just needs that route to exist.

```js
// overrides/core/lib/config/services.js
const SERVICES = [
  { id: 'events',       icon: 'event',          href: '/events' },
  { id: 'festivals',    icon: 'celebration',    href: '/festivals' },   // ← city-only card
  { id: 'participation',icon: 'campaign',       href: '/participation/questions' },
  { id: 'security',     icon: 'emergency',      href: '/security/alerts' },
  { id: 'mobility',     icon: 'directions_car', href: '/mobility/reserve' },
  { id: 'tourism',      icon: 'map',            href: '/tourism/routes' },
  // upstream order changed to put culture first
];

export default SERVICES;
```

## Notes

- The **card labels/descriptions** come from the dictionary keyed by `id`, so to add a
  card also add its copy — see [Override i18n copy](./override-i18n-dictionary.md).
- Match the upstream export style exactly (default export vs named) so the home page
  imports it correctly.

## Verify

`npm run dev`, open `/{lang}/home` and confirm the grid order/contents match, and a
card for a disabled module is hidden.
