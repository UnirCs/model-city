---
title: Data & API layer
sidebar_label: Data & API
sidebar_position: 8
---

# Data & API layer

Data access is split into two strict halves: **server-side API clients** that talk
to the back-end gateway with the user's bearer token, and **Server Actions** that
wrap those clients to perform authorised mutations. There are no Next.js route
handlers in between (see [Rendering](./rendering.md)).

## Per-module server clients

Each module owns its client(s) under `packages/<module>/lib/api/`. They are
**server-only** (Server Components and Server Actions); importing them into a
client component is prohibited.

| Module | Client(s) | Gateway base path |
| --- | --- | --- |
| core | `core/lib/api/client.js` | `${GATEWAY}/${MICROSERVICE_CORE_APP_NAME}` (default `core`) |
| engagement | `engagement/lib/api/client.js` | `${GATEWAY}/engagement` |
| leisure | `leisure/lib/api/client.js` | `${GATEWAY}/leisure` |
| mobility | `mobility/lib/api/client.js` | `${GATEWAY}/${MICROSERVICE_MOBILITY_APP_NAME}` (default `mobility`) |

`GATEWAY` is `MICROSERVICE_BASE_URL` in every client. **core** and **mobility**
additionally compose an app-name segment from their own env var; **leisure** and
**engagement** hardcode their gateway path segment.

### One client per feature module

Every feature module exposes its whole server-side surface from a single
`lib/api/client.js` — including leisure, whose events, spaces and tourism domains
are organised as labelled sections within that one file. This uniform shape is what
lets a city override a module's API client at the same predictable path
(`overrides/<id>/lib/api/client.js`); see [City overrides](../extensibility-guide/index.md).
`@modelcity/core` is the exception: as the shared library it keeps several distinct
cross-cutting clients (`client.js`, `certClient.js`, `systemTrails.js`).

### Browser mTLS client

`core/lib/api/certClient.js` is the one **client-side** API module. It fetches the
ALB directly (`NEXT_PUBLIC_MICROSERVICE_ALB_URL`) so the browser performs the TLS
handshake and presents the user's FNMT client certificate. It must never be
imported on the server. See [Rendering](./rendering.md#no-route-handlers--browser-to-microservice-policy).

## Bearer-token passing

Server clients take the access token as an explicit argument and attach it as a
`Bearer` header; the token comes from `session.tokenSet.accessToken`
([Auth & roles](./auth-and-roles.md)). Many read endpoints accept an optional token
(sent only when present):

```js
const res = await serverFetch(url, {
  cache: 'no-store',
  headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
});
```

## Observability — request correlation

The back-end reads an `X-Model-City-Correlation-Id` header at its entry point: when
present it **reuses** that value instead of minting a new correlationId, so one id
traces a whole frontend → gateway → downstream-services flow.

`packages/core/lib/observability/` holds the primitives:

- `correlation.js` — isomorphic `CORRELATION_HEADER` constant, `newCorrelationId()`
  (Web Crypto v4 UUID) and a `withCorrelationId()` header merger.
- `serverFetch.js` — the server-only `fetch` wrapper every server client uses. It
  injects the header, preserving caller headers, and derives the id from a
  `React.cache`-memoised `getCorrelationId()` so **all** back-end calls made while
  handling a single Server Action invocation / Server Component render share one id.

The browser mTLS `certClient.js` is the exception — each verification is its own
browser-initiated transaction, so it attaches a freshly generated
`newCorrelationId()` per call.

## Uniform error / null-return pattern

Clients **never throw to the caller**. They catch, log with a module-tagged prefix
`[<module>/api] …`, and return a sentinel:

- **Reads** return the parsed JSON, or `null` on any error (some return shaped
  results like `{ data }` / `{ notFound: true }` / `{ error: true }`;
  `checkUserExists` returns `{ exists, expired }`).
- **Mutations** return `{ ok: true, … }` or `{ error, status?, body? }`.

```js
if (!res.ok) {
  console.error('[leisure/api] getEvents error:', res.status);
  return null;
}
```

## Server Actions wrap the clients

Mutations live in `packages/<module>/lib/actions/*` (`'use server'`). The pattern:
get the session → enforce a capability helper → call the client with the token →
`revalidatePath()`. Action files by module:

| Module | Action files |
| --- | --- |
| core | `otp.js`, `registration.js`, `users.js`, `translation.js` |
| engagement | `questions.js`, `securityAlerts.js`, `vote.js` |
| leisure | `events.js`, `cityPlaces.js`, `cityRoutes.js`, `publicSpaces.js`, `spaceReservations.js`, `spaceResources.js`, `stripeCheckout.js` |
| mobility | `mobility.js`, `mobilityStripeCheckout.js` |

Payments use a lazy server-side Stripe SDK (`core/lib/payments/stripe.js`)
constructed on first use so builds can run without `STRIPE_SECRET_KEY`.

AI-assisted translation on the create/edit forms uses Google Gemini through a plain
`fetch` client (`core/lib/ai/gemini.js`) behind the `translateText`
(`core/lib/actions/translation.js`) Server Action. See
[AI translation](./ai-translation.md) for the full flow.