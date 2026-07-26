# `overrides/` — per-city customisation layer

Drop a file here to **replace or add** any module file without editing
`packages/*`. The rule is one sentence:

> Copy a file's path from `packages/` to `overrides/` and edit it.

```
packages/leisure/routes/events/page.js   →   overrides/leisure/routes/events/page.js
packages/core/components/atoms/Button.js →   overrides/core/components/atoms/Button.js
packages/core/routes-src/proxy.js        →   overrides/core/routes-src/proxy.js   (Proxy/Middleware)
```

How it works:

- **Resolution** — `jsconfig.json` maps each `@modelcity/<id>/*` to
  `["./overrides/<id>/*", "./packages/<id>/*"]`. Any import resolves to your
  override when the file exists, otherwise to the upstream package. This covers
  **every** file: components, `lib` helpers, dictionaries, styles, and routes you
  *replace*.
- **Routes** — replacing an existing route works through resolution alone. To
  **add** a route upstream does not ship, the codegen (`npm run gen:modules`,
  auto-run on `predev`/`prebuild`) emits its App Router shim; it logs
  `override route (add|replace): …` so you can see what was picked up.

An override must keep the **same export surface** as the file it shadows (same
default/named exports for a component or helper, same shape for a JSON
dictionary). Override the smallest file that isolates your change.

Full design, contract, and edge cases:
[`../../documentation/architecture/route-overrides.md`](../docs/architecture/route-overrides.md).
