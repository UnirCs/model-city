---
title: Override the navigation
sidebar_label: Override navigation
sidebar_position: 14
---

# Override the navigation

**Goal:** add, reorder or relabel the menu entries a module contributes — here add a
"Festivals" item to the leisure section — by overriding the module's nav contributor.

- **Override file:** `overrides/leisure/lib/nav/sections.js`
- **Regen needed:** no for the nav itself (it is a plain module). If the item points
  to a **new route**, add that route too ([Add a screen](./add-a-new-screen.md)) and
  run `npm run gen:modules` for the route.

Each module contributes its role-gated sections through `lib/nav/sections.js`, which
`core/lib/nav/sections.js` composes. The nav is then filtered by module flags and
capability helpers ([Sitemap & navigation](../../architecture/sitemap.md)).

## Recipe

Keep the **same export shape** the composer expects (a contributor that returns the
section objects), and reuse the capability helpers so gating stays correct:

```js
// overrides/leisure/lib/nav/sections.js
import { canViewGeneralSections } from '@modelcity/leisure/lib/auth/roles';

/** Returns the leisure nav sections for the given session + nav labels. */
export function leisureNavSections({ session, n }) {
  const sections = [];

  sections.push({
    sectionPath: '/events',
    rootHref: '/events',
    icon: 'event',
    label: n.eventsAgenda,
    items: [
      { href: '/events', icon: 'event', label: n.events },
      // City-only entry pointing at the new route:
      { href: '/festivals', icon: 'celebration', label: n.festivals },
    ],
  });

  if (canViewGeneralSections(session)) {
    sections.push(/* …tourism / sports-spaces sections, copied from upstream… */);
  }

  return sections;
}
```

:::caution[Match the upstream contract]

Copy the upstream `sections.js` first and edit it — the composer imports a specific
export (name and shape). Reordering is just reordering the returned array; disabling
an item is dropping it (or gating it with a capability helper).

:::

## Notes

- The nav is **role-gated**: keep using `canViewGeneralSections`, `isStaffUser`,
  `canManage*`, etc. so each role still sees the right items.
- A section whose module flag is off is dropped automatically — you don't gate for
  that here.

## Verify

`npm run dev`; the new item shows in the desktop sidebar, tablet rail and mobile
services sheet, gated by the same roles as the rest of leisure.
