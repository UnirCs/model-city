---
title: Scaffold the code base
sidebar_label: Scaffold the code base
sidebar_position: 2
---

# Scaffold the code base

You never fork Model City. A concrete city's code base is **generated** from the
platform's published artifacts:

- the **back-end** from one of two Maven archetypes (microservices or monolith), and
- the **front-end** from the `create-model-city-app` npm generator.

Both generators produce a project that depends on the platform packages (Maven
`*-domain` / commons artifacts, or the `@modelcity/*` npm packages) and expose
only the city-specific pieces — branding, module selection and override
directories — for you to edit.

## 1. Front-end: `create-model-city-app`

The front-end generator is the npm analogue of the back-end archetypes. It
scaffolds a Next.js app that consumes the `@modelcity/*` platform packages, with
build-time module selection and a per-city `overrides/` extension mechanism.

### Prerequisites

- **Node.js ≥ 20**.

### Generate the project

```bash
npm create model-city-app@latest my-city
```

You will be prompted for the feature modules the city contracts (`core` is always
included). To skip the prompt, pass the options:

| Option | Effect |
| --- | --- |
| `--name=<name>` | npm package name (default: the directory name) |
| `--modules=<list>` | comma-separated modules (`leisure`, `engagement`, `mobility`), or `all` / `none` |
| `--yes` | accept all defaults, no prompts (CI-friendly; implies `--modules=all`) |

For example, a non-interactive generation with two modules:

```bash
npm create model-city-app@latest my-city -- --modules=leisure,mobility --yes
```

### What you get

- **`package.json`** with the `@modelcity/*` packages pinned to the archetype's
  release-train version, plus the exact peer stack (Next, React, Auth0, Stripe,
  MapLibre) the platform is built against. Module-only dependencies (e.g.
  `qrcode.react` for `leisure`) are included only when that module is contracted.
- **`modules.config.mjs`** — the city's declaration of contracted modules and the
  single source of truth for the build-time codegen (`modelcity gen`).
- Project wiring derived from the platform reference app at publish time:
  `next.config.mjs`, `postcss.config.mjs` (Tailwind v4), `eslint.config.mjs`,
  `jsconfig.json` (with the `overrides/` fallback), `public/` and `.gitignore`.
- City-specific static wiring: `Dockerfile`, `.dockerignore` and `.env.example`.
- An empty **`overrides/`** directory documenting the extension mechanism.

### Run it

```bash
cd my-city
cp .env.example .env.local   # fill in Auth0, gateway and Stripe values
npm install
npm run dev
```

`npm run dev` / `npm run build` first run `modelcity gen`, which generates the
route shims, the composition root and the Tailwind source manifest for the
contracted modules.

### Add or drop a module later

```bash
npm install @modelcity/engagement@<platform version>
```

Then import its manifest in `modules.config.mjs` and append it to
`FEATURE_MODULES`. A contracted module can also be disabled per build by setting
its `MODULE_*` env flag (e.g. `MODULE_ENGAGEMENT`) to exactly `false`.

### Customise without editing platform files

Platform files are **not** edited in place. Drop replacements under `overrides/`
at the same path as in the `@modelcity/*` package. Route files are picked up by
`modelcity gen`; everything else is redirected through the `jsconfig.json` paths
fallback.

## 2. Back-end: Maven archetypes

The back-end ships **two** archetypes from the same domain code. Pick the topology
you want to deploy:

| Archetype `artifactId` | Produces |
| --- | --- |
| `model-city-back-end-microservices-archetype` | Aggregator with a Eureka registry, an API gateway and one Spring Boot app per vertical (`core`, `engagement`, `leisure`, `mobility`) |
| `model-city-back-end-monolith-archetype` | The same verticals assembled into a single deployable |

Both live under the platform group `io.github.unircs`. The boilerplate
(persistence adapters, entities, repositories, `@SpringBootApplication`, topology
config and resources) ships **inside** the archetype, so generation needs no
external reference project — only the city-specific pieces (standalone poms,
example override, branding) are templated.

### Prerequisites

- **JDK** and **Maven** installed (`mvn -v`).

### Archetype properties

Both archetypes ask for the same properties:

| Property | Default | Meaning |
| --- | --- | --- |
| `cityName` | `Example City` | Human-readable city name (used in the README/branding) |
| `cityKey` | `examplecity` | Short machine key for the city |
| `modelcityVersion` | `1.0.0-SNAPSHOT` | Version of the Model City platform (`*-domain` / commons) to depend on |
| `auth0Audience` | `https://model-city.example.org` | Substituted into the README; set the same value as `AUTH0_AUDIENCE` at runtime |
| `mailCityName` | `Ayuntamiento de Example City` | Email footer name; set as `MAIL_CITY_NAME` at runtime |
| `mailAddress` | `Plaza Mayor, 1. 00000 Example City.` | Email footer address; set as `MAIL_ADDRESS` at runtime |

You also provide the standard Maven coordinates for the generated project:
`groupId`, `artifactId` and `version`.

### Generate the project

Run `archetype:generate` in interactive mode and pick the archetype, or drive it
non-interactively. Microservices:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.github.unircs \
  -DarchetypeArtifactId=model-city-back-end-microservices-archetype \
  -DarchetypeVersion=<Archetype version> \
  -DgroupId=com.mycity \
  -DartifactId=my-city-back-end \
  -Dversion=1.0.0-SNAPSHOT \
  -DcityName="My City" \
  -DcityKey=mycity \
  -DmodelcityVersion=1.0.0-SNAPSHOT \
  -DinteractiveMode=false
```

Monolith — same command with the monolith archetype:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.github.unircs \
  -DarchetypeArtifactId=model-city-back-end-monolith-archetype \
  -DarchetypeVersion=<Archetype version> \
  -DgroupId=com.mycity \
  -DartifactId=my-city-back-end \
  -Dversion=1.0.0-SNAPSHOT \
  -DcityName="My City" \
  -DcityKey=mycity \
  -DmodelcityVersion=1.0.0-SNAPSHOT \
  -DinteractiveMode=false
```

### Build and run

```bash
cd my-city-back-end
mvn -q -DskipTests package
```

Before running the services you need the local database and cache up
([Local services](./local-services.md)) and the integration secrets from the
Auth0, Stripe and Gmail guides exported as environment variables.

## Next steps

With both projects scaffolded, continue with:

1. [Local services (PostgreSQL & Valkey)](./local-services.md)
2. [Auth0](./auth0.md), [Stripe](./stripe.md) and [Gmail](./gmail.md) for the secrets
3. [AWS deployment](./aws-deployment.md) when you are ready to go to the cloud
