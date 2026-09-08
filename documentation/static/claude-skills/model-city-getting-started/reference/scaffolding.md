# Reference — Scaffold the code base

Model City is never forked. Generate two independent projects from the platform's
published artifacts; both depend on the platform packages and expose only the
city-specific pieces (branding, module selection, override directories) for editing.

## Front-end: `create-model-city-app`

Prerequisite: **Node.js ≥ 20**.

```bash
npm create model-city-app@latest <dir-name> -- --modules=<comma-list-or-all-or-none> --yes
```

- `--name=<name>` — npm package name (default: directory name).
- `--modules=<list>` — comma-separated from `leisure`, `engagement`, `mobility`
  (`core` is implicit), or `all` / `none`.
- `--yes` — no prompts, CI-friendly, implies `--modules=all` unless `--modules` is
  also given.

What you get: `package.json` pinned to the release-train version plus the exact peer
stack (Next.js, React, Auth0, Stripe, MapLibre); `modules.config.mjs` (source of truth
for `modelcity gen` codegen); `modelcity.config.js` (branding — city name, coat of
arms, backdrop, landing photos, map focus, neighbourhood catalogue); an empty
`overrides/` directory (platform files are never edited in place — drop replacements
here at the same path as in the `@modelcity/*` package); `Dockerfile`,
`.dockerignore`, `.env.example`.

Run it:

```bash
cd <dir-name>
cp .env.example .env.local   # fill in Auth0, gateway and Stripe values later
npm install
npm run dev
```

`npm run dev` / `npm run build` first run `modelcity gen` (route shims, composition
root, Tailwind manifest for the contracted modules).

To add a module later: `npm install @modelcity/<module>@<platform version>`, import
its manifest in `modules.config.mjs`, append it to `FEATURE_MODULES`. A contracted
module can be disabled per build via its `MODULE_*` env flag set to exactly `false`.

## Back-end: Maven archetypes

Two archetypes from the same domain code, group `io.github.unircs`:

| `artifactId` | Produces |
| --- | --- |
| `model-city-back-end-microservices-archetype` | Aggregator: Eureka registry + API gateway + one Spring Boot app per vertical |
| `model-city-back-end-monolith-archetype` | Same verticals assembled into a single deployable |

Prerequisites: JDK + Maven (`mvn -v`).

Properties both archetypes ask for:

| Property | Default | Meaning |
| --- | --- | --- |
| `cityName` | `Example City` | Human-readable name (README/branding) |
| `cityKey` | `examplecity` | Short machine key |
| `modelcityVersion` | `1.0.0-SNAPSHOT` | Platform (`*-domain`/commons) version to depend on |
| `auth0Audience` | `https://model-city.example.org` | README placeholder; set as `AUTH0_AUDIENCE` at runtime |
| `mailCityName` | `Ayuntamiento de Example City` | Email footer; runtime `MAIL_CITY_NAME` |
| `mailAddress` | `Plaza Mayor, 1. 00000 Example City.` | Email footer; runtime `MAIL_ADDRESS` |

Plus standard Maven coordinates: `groupId`, `artifactId`, `version`.

Generate non-interactively (microservices shown; swap `-DarchetypeArtifactId` for the
monolith one otherwise identical):

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.github.unircs \
  -DarchetypeArtifactId=model-city-back-end-microservices-archetype \
  -DarchetypeVersion=<resolved in stage 1> \
  -DgroupId=com.mycity \
  -DartifactId=my-city-back-end \
  -Dversion=1.0.0-SNAPSHOT \
  -DcityName="My City" \
  -DcityKey=mycity \
  -DmodelcityVersion=<resolved in stage 1, or a -SNAPSHOT> \
  -DinteractiveMode=false
```

Build:

```bash
cd my-city-back-end
mvn -q -DskipTests package
```

Running the services needs the local database/cache (stage 3) and the integration
secrets (stage 4) exported as environment variables first.
