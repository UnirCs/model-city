---
title: Project structure
sidebar_label: Project structure
sidebar_position: 2
---

# Project structure

The repository is an **npm-workspaces monorepo**. The reusable code is split into
one package per feature area under `packages/`, and a single **orchestrator** app
at the repo root (`src/`) owns the App Router tree and composes the packages. This
mirrors the back-end's multi-module Maven layout; see
[Modular architecture](./modular-architecture.md) for the rationale.

## Top-level layout

```text
package.json                 # workspace root: "workspaces": ["packages/*", "create-model-city-app"]
jsconfig.json                # @/* → ./src/*  and  @modelcity/* → ./overrides/* then ./packages/*
next.config.mjs              # transpilePackages: the @modelcity/* packages
modules.config.mjs           # module registry (reads packages/*/module.manifest.mjs)
packages/                    # the reusable libraries (the "Maven modules")
├── core/                    # @modelcity/core — always-on shared library
│   ├── lib/                 # config, i18n engine, nav model, auth, api, a11y, payments, observability, ai, seo
│   ├── components/          # atoms, molecules, organisms, templates, providers (core tier)
│   ├── routes*/             # gated + public + root + proxy route sources
│   └── styles/globals.css   # the shared stylesheet + design tokens
├── leisure/                 # @modelcity/leisure — events, sports-spaces, tourism
├── engagement/              # @modelcity/engagement — participation, security
├── mobility/                # @modelcity/mobility — mobility ops
└── cli/                     # @modelcity/cli — the `modelcity gen` codegen
src/                         # the orchestrator app (the single deployable)
├── proxy.js                 # SHIM → core/routes-src/proxy (Middleware: lang redirects + Auth0; generated, overridable)
├── lib/composition/         # composition root: registers modules into core engines
└── app/                     # App Router (RSC) — only the favicons are hand-written; everything else is a SHIM
    ├── favicon.ico          # Hand-written (tracked)
    ├── layout.js            # SHIM → core/routes-root/layout (<html> shell; prelude: registerModules + globals.css)
    ├── not-found.js         # SHIM → core/routes-root/not-found
    ├── robots.js            # SHIM → core/routes-root/robots
    ├── sitemap.js           # SHIM → core/routes-root/sitemap
    └── [lang]/              # Locale segment (es | en | fr)
        ├── layout.js        # SHIM → core/routes-public/layout (providers + lang shell)
        ├── loading.js       # SHIM → core/routes-public/loading (locale-level fallback)
        ├── page.js          # SHIM → core/routes-public/page (public landing)
        ├── register/        # SHIM → core/routes-public/register (ungated)
        ├── help/glossary/   # SHIM → core/routes-public/help/glossary (public)
        └── (app)/           # Authenticated portal (session-gated)
            ├── layout.js              # SHIM → core/routes-app/layout (session + registration gate + AppShell)
            ├── (core)/                # SHIMS → core/routes (home, profile, help, administration)
            ├── (citizen-engagement)/  # SHIMS → engagement/routes — historical folder name, see the naming caveat in Modularity
            ├── (leisure)/             # SHIMS → leisure/routes (events, sports-spaces, tourism)
            └── (mobility)/            # SHIMS → mobility/routes (mobility)
```

Every file under `src/app/` is a **generated shim** (git-ignored) that re-exports
route logic from a package — **only the favicons remain hand-written**. This
includes the true root shell (`layout.js`, `not-found.js`, `robots.js`,
`sitemap.js`) and the locale-level `[lang]/layout.js` + `[lang]/loading.js`; even
the `(app)` gate is a shim. The parenthesised folders under `(app)/` are **route
groups** — they organise files and attach a per-module layout guard **without**
appearing in the URL (`(app)/(leisure)/events` serves `/{lang}/events`).

Core supplies route logic from **four** directories: `routes/` (gated, →
`(app)/(core)`), `routes-app/` (the `(app)` gate's own layout/loading, →
`(app)/`), `routes-public/` (landing, register, glossary **and** the `[lang]`
layout/loading, emitted at the bare `[lang]` root **outside** the gate), and
`routes-root/` (the root shell above `[lang]`, → `src/app/`). The shared
stylesheet lives in `packages/core/styles/globals.css` and is imported by the root
layout shim.

:::info[Codegen]

The shims are produced by the `@modelcity/cli` codegen (`modelcity gen`, exposed
as `npm run gen:modules` and run automatically on `predev` / `prebuild`). See
[Modular architecture](./modular-architecture.md) and [City overrides](../extensibility-guide/index.md).

:::

## Import aliases

`jsconfig.json` maps two alias families, so every import is absolute:

- `@/*` → `./src/*` — the orchestrator's own files (routes, proxy, composition root).
- `@modelcity/<pkg>/*` → an **ordered fallback**: `./overrides/<pkg>/*` first, then
  `./packages/<pkg>/*` — the reusable packages, with the per-city override layer
  taking precedence (see [City overrides](../extensibility-guide/index.md)). The packages are
  also linked into `node_modules/@modelcity/*` by npm workspaces.

```js
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import AppShell from '@modelcity/core/components/templates/AppShell';
import { getEvent } from '@modelcity/leisure/lib/api/client';
```

## Module-first principle

| Tier | Path shape | Example |
| --- | --- | --- |
| Routes | `src/app/[lang]/(app)/(<module>)/...` | `(leisure)/events/page.js` |
| Components | `packages/<pkg>/components/<tier>/...` | `leisure/components/molecules/RouteMap.js` |
| Logic | `packages/<pkg>/lib/{api,actions,auth,utils,i18n,nav}/...` | `leisure/lib/api/client.js` |

`@modelcity/core` is the always-on home for shared concerns: the i18n engine
(`core/lib/i18n/dictionaries.js`), auth primitives (`core/lib/auth/roles.js`),
module configuration (`core/lib/config/modules.js`), the navigation model
(`core/lib/nav/sections.js`), the app shell
(`core/components/templates/AppShell.js`) and the global providers.

**Critically, `core` does not depend on any feature module.** The i18n engine and
nav model expose registration hooks (`registerServiceLoaders`,
`registerNavContributor`); the orchestrator's composition root
(`src/lib/composition/registerModules.js`, imported for its side effects by
`src/app/layout.js`) wires the active modules in. This `core ⇏ module` /
`module ⇏ module` boundary is enforced by `dependency-cruiser`
(`npm run dep:check`).

The placement rule for new code: **shared / cross-module / shell → `core/`;
domain-specific → its owning module.** See
[Design system & tokens](./design-system.md#placement-rule) for the component
variant.
