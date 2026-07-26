---
title: Override the Proxy / Middleware
sidebar_label: Override the proxy
sidebar_position: 11
---

# Override the Proxy / Middleware

**Goal:** change the single request entry point — here add a security header and
narrow the matcher — by overriding the proxy.

- **Override file:** `overrides/core/routes-src/proxy.js`
- **Regen needed:** **yes** — `routes-src/` is a generated source; the `src/proxy.js`
  shim carries the `matcher` literally, so it must be re-emitted.

The proxy is a generated shim (`packages/core/routes-src/proxy.js`), so it overrides
like anything else. It has a special contract: a **`proxy` named export** (not a
default) plus an optional **`config`** object that Next statically analyses.

## Recipe

Prefer layering city behaviour on top of the upstream Auth0/locale logic rather than
reimplementing it:

```js
// overrides/core/routes-src/proxy.js
import { auth0 } from '@modelcity/core/lib/auth/auth0';

export async function proxy(request) {
  const res = await auth0.middleware(request);
  res.headers.set('x-city', 'aranjuez');
  res.headers.set('x-frame-options', 'DENY');
  return res;
}

export const config = {
  // Re-declared literally into the generated src/proxy.js and statically analysed by
  // Next — keep it a plain, single-level object literal (array of strings).
  matcher: ['/((?!_next/static|_next/image|favicon.ico).*)'],
};
```

`gen:modules` logs `override route (replace): core/routes-src/proxy`. The generated
`src/proxy.js` re-exports the city `proxy` and carries the city `matcher`.

:::caution[`config` must be a flat literal]

The codegen copies the `matcher` literal verbatim into `src/proxy.js`. A plain
`{ matcher: ['…'] }` is fine; deeply nested or computed values can confuse the
capture — keep it flat, as upstream does.

:::

:::danger[Override sparingly]

The proxy is the app's only entry point (locale redirects **and** Auth0 session
handling). Reimplementing it wholesale risks breaking login or i18n — keep the
upstream `auth0.middleware(...)`/locale logic and layer on top, as above.

:::

## Verify

`npm run gen:modules` prints the `replace` line; the build's
`functions-config-manifest.json` carries your matcher; `npm run dev` shows the extra
headers and login/locale routing still work.
