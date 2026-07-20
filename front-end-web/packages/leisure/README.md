# @modelcity/leisure

Leisure feature module of the **Model City** front-end platform: events, sports
spaces (reservations, resources) and tourism (city places, routes/trails).

Part of the Model City release train — install every `@modelcity/*` package at
the **same version**. Requires [`@modelcity/core`](https://www.npmjs.com/package/@modelcity/core).

## Usage

Ships **source** (JSX + RSC directives); add it to `transpilePackages` in the
consuming app. Declare the module in the project's `modules.config.mjs` and run
`modelcity gen` to emit its route shims — city projects scaffolded with
`npm create model-city-app` get this wiring out of the box.

## License

MIT — see [LICENSE](./LICENSE).
