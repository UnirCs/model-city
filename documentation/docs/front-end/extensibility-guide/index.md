---
title: Extensibility & city overrides
sidebar_label: Overview
sidebar_position: 1
---

# Extensibility & city overrides

A city built on Model City can replace or add **any module file** — a component, a
screen, a server action, an API client, a helper, a dictionary, a stylesheet, even
the proxy — **without editing the upstream `packages/*` sources**. This section
explains the `overrides/` mechanism and then gives a **library of worked examples**,
one per kind of customisation.

:::tip[Start here, then copy an example]

Read this page once to understand the model, then jump to the example below that
matches what you want to change. Every example is a self-contained recipe with the
exact file path and ready-to-copy code.

:::

## 1. Why it exists

The App Router tree is **generated**, not authored:

- Route logic lives in the feature packages, e.g.
  `packages/leisure/routes/events/page.js`.
- The `@modelcity/cli` codegen (`modelcity gen`) walks each module's route source
  directory and emits a thin re-export *shim* under `src/app/[lang]/…`. The shims
  are git-ignored — they are pure build artifacts. For the events page:

  ```js
  export { default, generateMetadata } from '@modelcity/leisure/routes/events/page';
  export const dynamic = 'force-dynamic';
  ```

- **Every** module file — routes, components, lib helpers, i18n loaders, styles — is
  imported through the `@modelcity/<id>/…` specifier, which resolves via the
  `jsconfig.json` `paths` map (Next reads `jsconfig` paths natively under
  Turbopack), with the workspace + `transpilePackages` ensuring the package source
  is compiled.

The platform premise is *"each city applies its own customisations"* — the
`overrides/` layer provides that without a permanent diff against upstream.

## 2. The one rule

`overrides/` **mirrors the `packages/<id>/` layout**. The whole rule is one sentence:

> To override a file, copy its path from `packages/` to `overrides/` and edit it.

| Upstream file | City override file |
| --- | --- |
| `packages/core/components/atoms/Button.js` | `overrides/core/components/atoms/Button.js` |
| `packages/leisure/routes/events/page.js` | `overrides/leisure/routes/events/page.js` |
| `packages/leisure/lib/actions/events.js` | `overrides/leisure/lib/actions/events.js` |
| `packages/leisure/lib/api/client.js` | `overrides/leisure/lib/api/client.js` |
| `packages/core/styles/globals.css` | `overrides/core/styles/globals.css` |
| `packages/core/routes-src/proxy.js` (Proxy/Middleware) | `overrides/core/routes-src/proxy.js` |
| *(none — a new city route)* | `overrides/leisure/routes/festivals/page.js` |

The override directory is configurable for the codegen via the `CITY_OVERRIDES_DIR`
env var (default `overrides`). Upstream ships it containing only a `README.md`, so
the directory and convention exist out of the box.

## 3. How resolution works (the `jsconfig` paths fallback)

The mechanism for **every non-route file (and every replaced route)** is the ordered
fallback list in `jsconfig.json` — each module alias tries `overrides/` first:

```jsonc
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./src/*"],
      "@modelcity/core/*":       ["./overrides/core/*",       "./packages/core/*"],
      "@modelcity/leisure/*":    ["./overrides/leisure/*",    "./packages/leisure/*"],
      "@modelcity/engagement/*": ["./overrides/engagement/*", "./packages/engagement/*"],
      "@modelcity/mobility/*":   ["./overrides/mobility/*",   "./packages/mobility/*"]
    }
  }
}
```

For any import `@modelcity/leisure/<path>`, the resolver tries
`overrides/leisure/<path>` first and uses it **iff the file exists**, otherwise it
falls through to `packages/leisure/<path>`. No `next.config.mjs` change, no `@city`
alias, no per-file wiring. Turbopack (Next.js 16's default bundler) honours the
*first-existing* array semantics.

## 4. Do I need to run the codegen?

| Kind of override | Regen? |
| --- | --- |
| Component, provider, template, `lib/*` helper, API client, server action, nav, roles, config, i18n JSON, `globals.css` | **No** — resolution swaps them at import time |
| **Replace** an existing route (`routes/**`) | No (the existing shim falls through), but harmless to run |
| **Add** a brand-new route, `loading.js`, `layout.js` | **Yes** — a new shim must be emitted |
| **Override the proxy** (`routes-src/proxy.js`) | **Yes** — the `src/proxy.js` shim carries the matcher literally |

Run it with `npm run gen:modules` (it also runs automatically on `predev` /
`prebuild`).

## 5. The override contract

An override is a **drop-in replacement**: it must expose the **same module surface**
the rest of the app expects from the original — the same default/named exports for a
component or helper, the same shape for a JSON dictionary. For a **route file** the
codegen understands a default export (required), optional `generateMetadata` /
`generateStaticParams` / static `metadata`, and an optional `export const dynamic`
(re-declared literally into the shim). For the **proxy** it understands a `proxy`
named export plus an optional `config` object (re-declared literally). Any other
named export from a route triggers a `WARN`.

:::caution[Keep city-local components out of `routes/`]

The codegen emits a shim for **every** `.js` under a routes directory. Place a new
city component under the mirrored `overrides/<id>/components/…` path and import it via
`@modelcity/<id>/components/…` (resolved by the fallback, no shim) — do **not**
colocate it next to a route file.

:::

## 6. Limitations & edge cases

- **Whole-file granularity.** An override replaces an entire file. Override the
  *smallest* file that isolates your change (a leaf component or helper), not a big
  parent — that way you inherit upstream fixes to everything else.
- **Upstream renames/removals.** Overrides are keyed by path. If upstream renames
  `events/page.js`, your override silently becomes a *new* route at the old URL.
  Re-check overrides after an upstream sync; the codegen's `override route (add)` log
  helps spot a replace that has degraded into an add.
- **Module flags still win.** A disabled module (`MODULE_LEISURE=false`) emits no
  routes at all, including its overrides.
- **i18n is whole-file.** Overriding a locale JSON replaces it entirely; there is no
  per-key merge (yet) — copy upstream's file and edit.
- **Proxy `config` must be a single-level object literal** — the codegen copies its
  literal into the generated `src/proxy.js`.

## 7. Standardisation note

The feature modules are normalised to one uniform skeleton, so overriding a given
concern is the same path in every module:

- `components/{atoms,molecules,organisms}/…`
- `lib/api/client.js` — a single server-side API client per module.
- `lib/utils/format.js` — a single formatting/utility module (where needed).
- `lib/{actions,auth,home,i18n,nav}/…` and `i18n/locales/{es,en,fr}/<ns>.json`.

`@modelcity/core` keeps its own shape (it is the shared library, not a feature
module): its `lib/api/` holds several distinct cross-cutting clients (`client.js`,
`certClient.js`, `systemTrails.js`) and it owns `styles/globals.css` and the four
route sources (`routes/`, `routes-app/`, `routes-public/`, `routes-root/`) plus the
proxy (`routes-src/`).

## Examples

One copy-ready recipe per kind of customisation. Each names the exact override file,
whether the codegen must run, full example code, and how to verify it.

**Components & UI**

- [Restyle a design-system component](./examples/restyle-a-component.md)
- [Replace a feature organism](./examples/replace-an-organism.md)
- [Add a city-local component](./examples/add-a-city-local-component.md)
- [Override a global provider](./examples/override-a-provider.md)
- [Override the AppShell template](./examples/override-the-app-shell.md)
- [Rebrand the design tokens (`globals.css`)](./examples/override-globals-css.md)

**Screens & routing**

- [Replace a screen (route)](./examples/replace-a-screen.md)
- [Add a brand-new screen](./examples/add-a-new-screen.md)
- [Add a loading skeleton to a screen](./examples/add-a-loading-skeleton.md)
- [Override a public (ungated) route](./examples/override-a-public-route.md)
- [Override the Proxy / Middleware](./examples/override-the-proxy.md)
- [Add a whole new section (end-to-end)](./examples/add-a-section-end-to-end.md)

**Logic, data & configuration**

- [Override a server action](./examples/override-a-server-action.md)
- [Override an API client](./examples/override-an-api-client.md)
- [Override formatting utilities](./examples/override-format-utils.md)
- [Override role & capability helpers](./examples/override-roles.md)
- [Override the navigation](./examples/override-navigation.md)
- [Override the home service catalogue](./examples/override-home-catalogue.md)
- [Override an i18n dictionary](./examples/override-i18n-dictionary.md)
- [Override public SEO & metadata](./examples/override-seo-metadata.md)
