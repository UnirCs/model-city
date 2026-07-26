---
title: Override a server action
sidebar_label: Override a server action
sidebar_position: 9
---

# Override a server action

**Goal:** add a city side effect to a mutation — here write a city audit log entry
when an event is created — while keeping the upstream API call.

- **Override file:** `overrides/leisure/lib/actions/events.js`
- **Regen needed:** no — server actions are plain module files, swapped by resolution.

Server actions are `'use server'` modules under `lib/actions/`. Keep the **same
exported names** the callers (forms, routes) import, so every call site resolves to
the city version transparently.

## Recipe

```js
// overrides/leisure/lib/actions/events.js
'use server';

import { revalidatePath } from 'next/cache';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { canManageEvents } from '@modelcity/leisure/lib/auth/roles';
// Re-use the upstream API client — only the action wrapper changes.
import { createEvent, updateEvent } from '@modelcity/leisure/lib/api/client';
import { auditCityChange } from '@modelcity/leisure/lib/utils/cityAudit'; // city-local helper

export async function saveEvent(payload, id = null) {
  const session = await auth0.getSession();
  if (!canManageEvents(session)) return { error: 'forbidden' };
  const accessToken = session?.tokenSet?.accessToken;

  const result = id
    ? await updateEvent(id, payload, accessToken)
    : await createEvent(payload, accessToken);

  if (!result.ok) return { error: result.body?.message ?? 'save_failed' };

  await auditCityChange('event.saved', result.data?.id); // ← city-specific side effect
  revalidatePath('/[lang]/events', 'page');
  return { ok: true, id: id ?? result.data?.id };
}
```

:::caution[Keep the whole export surface]

Callers import **every** action from this file (e.g. `saveEvent`, `claimFreeTicket`,
`refundTicket`). Because an override **replaces the whole file**, you must re-declare
**all** the actions the module originally exported — not just the one you changed —
by copying the untouched ones from the upstream `packages/leisure/lib/actions/events.js`.
Anything you drop resolves to `undefined` at its call sites.

This is the strongest argument for the golden rule: **override the smallest file that
isolates your change.** If only `saveEvent` needs a side effect, that side effect
(e.g. `auditCityChange`) can instead live in a city-local helper that you call from a
thin wrapper, minimising what you have to keep in sync.

:::

## Verify

`npm run dev`, create an event as staff and confirm both the upstream save and your
audit side effect run. No `gen:modules` needed.
