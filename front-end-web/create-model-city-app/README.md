# create-model-city-app

Scaffolds a **Model City** city front-end — the npm analogue of the back-end's
Maven archetypes. The generated project is a Next.js app that consumes the
`@modelcity/*` platform packages from the npm registry, with build-time module
selection (`modelcity gen`) and a per-city `overrides/` extension mechanism.

## Usage

```bash
npm create model-city-app@latest my-city
```

Options:

| Option | Effect |
| --- | --- |
| `--name=<name>` | npm package name (default: the directory name) |
| `--modules=<list>` | comma-separated feature modules (`leisure`, `engagement`, `mobility`), `all` or `none`; skips the prompt |
| `--yes` | accept all defaults, no prompts (CI-friendly) |

## What you get

- `package.json` with the platform packages pinned to this archetype's
  release-train version and the exact peer stack (Next, React, Auth0, Stripe,
  MapLibre) the platform is built against.
- `modules.config.mjs` — the city's declaration of contracted modules.
- Project wiring derived from the platform's reference app at publish time
  (via `sync-template.mjs`): `next.config.mjs`, `postcss.config.mjs`
  (Tailwind v4), `eslint.config.mjs`, `jsconfig.json` with the overrides
  fallback, `public/` and `.gitignore`.
- Static wiring maintained in the archetype itself (not derived from the
  reference app): `Dockerfile`, `.dockerignore` and `.env.example`.
- An empty `overrides/` directory documenting the extension mechanism.

## Links

- [Model City platform repository](https://github.com/UnirCs/model-city) — architecture, the rest of the release train and the platform packages this archetype wires in.

## Author

Created and maintained by [Jesús Pérez Melero](https://www.linkedin.com/in/jesusperezmelero/), author of the Model City platform.

## License

MIT — see [LICENSE](./LICENSE).
