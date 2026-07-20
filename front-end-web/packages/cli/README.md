# @modelcity/cli

Build-time codegen CLI for **Model City** front-end projects.

`modelcity gen` reads the project's `modules.config.mjs` (the city's
declaration of contracted modules) and emits, for the enabled modules only:

1. **Route shims** under `src/app/` re-exporting each route from its
   `@modelcity/*` package — a disabled module's URLs 404 and its code never
   enters the bundle (Maven-reactor-style build-time selection).
2. **The composition root** (`src/lib/composition/registerModules.js`)
   registering the enabled modules' i18n loaders, nav sections and home
   services into `@modelcity/core`.
3. **The Tailwind source manifest** (`src/styles/modules.css`) importing the
   (possibly city-overridden) core stylesheet and adding one `@source` per
   enabled module, so component classes shipped in `node_modules` are scanned.

All outputs are generated artifacts — git-ignore them and run the command on
`predev`/`prebuild`:

```json
"scripts": {
  "gen:modules": "modelcity gen",
  "predev": "npm run gen:modules",
  "prebuild": "npm run gen:modules"
}
```

City overrides are read from `./overrides` (or `CITY_OVERRIDES_DIR`): a file at
`overrides/<module>/routes/…` replaces or adds a route; non-route files are
overridden through the project's jsconfig `paths` fallback.

Part of the Model City release train — install every `@modelcity/*` package at
the **same version**. City projects scaffolded with `npm create model-city-app`
get all of this wiring out of the box.

## Links

- [Model City platform repository](https://github.com/UnirCs/model-city) — architecture, the rest of the release train and the `create-model-city-app` archetype.

## License

MIT — see [LICENSE](./LICENSE).
