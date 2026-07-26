---
title: Override role & capability helpers
sidebar_label: Override roles
sidebar_position: 15
---

# Override role & capability helpers

**Goal:** change **who** can do something — here let the `OPERATOR` role manage
events in this city — by overriding a module's capability helpers.

- **Override file:** `overrides/leisure/lib/auth/roles.js`
- **Regen needed:** no.

Authorisation is role-based: `core/lib/auth/roles.js` holds the primitives and cross-
cutting gates, and each module's `lib/auth/roles.js` builds capability helpers on top
([Auth & roles](../../architecture/auth-and-roles.md)). These helpers gate **both**
server actions (a write returns `{ error: 'forbidden' }`) and UI affordances, so
overriding them changes both at once.

## Recipe

Keep the **same exported helper names** (e.g. `canManageEvents`, `canDeleteEvents`,
`canBuyEventTickets`, …) so every call site keeps working. Reuse the core primitives:

```js
// overrides/leisure/lib/auth/roles.js
import { ROLES, hasAnyRole, isCitizen } from '@modelcity/core/lib/auth/roles';

// City policy: OPERATORS may manage events too (upstream restricts to BACKOFFICE/ADMIN).
export function canManageEvents(session) {
  return hasAnyRole(session, [ROLES.PLATFORM_ADMIN, ROLES.BACKOFFICE, ROLES.OPERATOR]);
}

export function canDeleteEvents(session) {
  return hasAnyRole(session, [ROLES.PLATFORM_ADMIN, ROLES.BACKOFFICE]);
}

export function canBuyEventTickets(session) {
  return isCitizen(session);
}

// …re-declare the remaining helpers the module originally exported…
```

:::caution[Keep the whole surface and don't weaken security]

- Copy the upstream file and re-declare **every** exported helper — dropping one makes
  its call sites treat it as `undefined` (falsy → access denied everywhere, or a
  runtime error).
- The front-end check is a UX gate; the **back-end enforces authorisation
  independently**. Widening a front-end helper without the matching back-end policy
  will just produce `403`s from the API. Coordinate both.

:::

## Verify

`npm run dev`, sign in as an operator and confirm the "Edit/Delete event" affordances
appear and the save action succeeds only if the back-end also permits it.
