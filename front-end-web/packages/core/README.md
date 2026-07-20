# @modelcity/core

Always-on shared library of the **Model City** front-end platform: app scaffolding
(root/public/app-gate route sources), auth & roles (Auth0), the i18n engine
(`es`/`en`/`fr`), navigation, API-client infrastructure and the design-system
core components (atoms → templates).

Part of the Model City release train — install every `@modelcity/*` package at
the **same version**.

## Usage

This package ships **source** (JSX + React Server Components directives). The
consuming Next.js app must transpile it:

```js
// next.config.mjs
const nextConfig = {
  transpilePackages: ['@modelcity/core' /* + feature modules */],
};
```

City projects are expected to be scaffolded with `npm create model-city-app`,
which wires this package, the feature modules, the `modelcity` codegen CLI and
the per-city override mechanism together.

## License

MIT — see [LICENSE](./LICENSE).
