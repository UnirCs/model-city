---
title: Front-end
sidebar_label: Overview
sidebar_position: 1
---

# Front-end documentation

The Model City front-end is a **Next.js 16 (App Router)** application that consumes
the `@modelcity/*` platform packages from the npm registry, with build-time module
selection (`modelcity gen`) and a per-city `overrides/` extension mechanism. A
concrete city's app is scaffolded with
[`create-model-city-app`](../how-to-start/scaffolding.md#1-front-end-create-model-city-app).

This documentation has three subsections:

## [Architecture](./architecture/)

How the front-end is put together: the npm-workspaces monorepo, the modular
package split and codegen, rendering (RSC + Server Actions), i18n, auth & roles,
the data/API layer, the design system, AI translation, and publishing. Start with
the [Architecture overview](./architecture/).

## [Atomic Design](./atomic-design/)

The one-page-per-component reference for the design system — every atom, molecule,
organism, template and provider (all 142 components), verified against the real
`packages/*` source. Start with the [component overview](./atomic-design/).

## [Extensibility Guide](./extensibility-guide/)

How a city replaces or adds **any** module file — a component, a screen, a server
action, an API client, the proxy, `globals.css`, i18n, navigation, roles… — through
the `overrides/` layer, with one worked, copy-ready example per kind of
customisation. Start with the
[extensibility overview](./extensibility-guide/index.md).

## Related

- To scaffold and run a city front-end, see
  [Scaffold the code base](../how-to-start/scaffolding.md).
- For the environment variables the front-end needs, see
  [Auth0](../how-to-start/auth0.md) and [Stripe](../how-to-start/stripe.md).
