---
title: Override an API client
sidebar_label: Override an API client
sidebar_position: 10
---

# Override an API client

**Goal:** change how a module talks to the gateway — here add a city header to every
request and point at a city-specific gateway path — by overriding the module's single
server-side API client.

- **Override file:** `overrides/leisure/lib/api/client.js`
- **Regen needed:** no.

Each feature module exposes its whole server-side surface from one
`lib/api/client.js` ([Data & API](../../architecture/data-and-api.md)). Overriding it
lets you wrap or extend every call. Keep the **same exported function names** (e.g.
`getEvents`, `getEvent`, `createEvent`, …) — the routes and actions import them.

## Recipe

Reuse `serverFetch` so you keep the correlation-id and `Accept-Language` plumbing:

```js
// overrides/leisure/lib/api/client.js
import { serverFetch } from '@modelcity/core/lib/observability/serverFetch';

const GATEWAY = process.env.MICROSERVICE_BASE_URL ?? 'http://localhost:8762';
// City deployment routes leisure under a custom gateway segment:
const BASE = `${GATEWAY}/aranjuez-leisure`;

function cityHeaders(accessToken) {
  return {
    'X-City': 'aranjuez',
    ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
  };
}

export async function getEvents({ page = 0, eventType, paid } = {}, accessToken) {
  const qs = new URLSearchParams({ page: String(page) });
  if (eventType) qs.set('eventType', eventType);
  if (paid != null) qs.set('paid', String(paid));
  const res = await serverFetch(`${BASE}/events?${qs}`, {
    cache: 'no-store',
    headers: cityHeaders(accessToken),
  });
  if (!res.ok) { console.error('[leisure/api] getEvents error:', res.status); return null; }
  return res.json();
}

// …re-declare the rest of the module's exported functions the same way…
```

## Notes

- **Never throw to the caller.** Keep the upstream sentinel contract: reads return the
  parsed JSON or `null` on error (some return shaped results); mutations return
  `{ ok: true, … }` or `{ error, status?, body? }`.
- Use `serverFetch` (not raw `fetch`) so the `X-Model-City-Correlation-Id` and locale
  headers keep flowing — see
  [Data & API → Observability](../../architecture/data-and-api.md).
- The client is **server-only**; never import it into a client component.

## Verify

`npm run dev`, hit a leisure page and confirm requests carry the `X-City` header
(check the gateway/microservice logs) and the app still renders.
