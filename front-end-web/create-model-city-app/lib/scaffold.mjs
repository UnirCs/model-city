/**
 * Scaffolds a Model City city project from the packaged template.
 *
 * The template has two halves:
 *   - `template/synced/`  — derived from the reference app at pack time by
 *     `sync-template.mjs` (configs, public assets, favicons and
 *     `base-package.json`, the source of the peer-stack versions).
 *   - `template/static/`  — city-specific files that do not exist in the
 *     monorepo (city Dockerfile, `.env` example, `overrides/` README).
 *     Files prefixed with `_` are renamed to their dotfile form (npm strips
 *     `.gitignore` and friends from published tarballs).
 *
 * Generated per city: `package.json` (name + selected modules pinned to this
 * archetype's release-train version), `modules.config.mjs` (imports only the
 * contracted modules) and a quick-start `README.md`.
 */
import { cpSync, mkdirSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = dirname(fileURLToPath(import.meta.url));
const TEMPLATE = join(HERE, '..', 'template');
const SELF = JSON.parse(readFileSync(join(HERE, '..', 'package.json'), 'utf8'));

/** The platform's feature modules a city can contract. */
export const FEATURE_MODULES = [
  { id: 'leisure', flag: 'MODULE_LEISURE', description: 'events, sports spaces and tourism' },
  { id: 'engagement', flag: 'MODULE_ENGAGEMENT', description: 'participation (consultations) and security (alerts)' },
  { id: 'mobility', flag: 'MODULE_MOBILITY', description: 'citizen stays/cars and staff ticket/sanction operations' },
];

// Peer-stack dependencies that only one module needs — dropped when that
// module is not contracted.
const MODULE_ONLY_DEPS = { leisure: ['qrcode.react'] };

// Dev tooling a city project needs (subset of the reference app's
// devDependencies — the rest is monorepo-only QA tooling).
const DEV_DEPS = [
  '@tailwindcss/postcss',
  'tailwindcss',
  'babel-plugin-react-compiler',
  'eslint',
  'eslint-config-next',
  'eslint-plugin-jsx-a11y',
];

// npm scripts a city project keeps (the reference app's release/dep-graph
// scripts are monorepo-only).
const SCRIPTS = ['dev', 'build', 'start', 'gen:modules', 'prebuild', 'predev', 'lint', 'a11y:lint'];

/**
 * @param {object} opts
 * @param {string} opts.targetDir absolute path to create the project in
 * @param {string} opts.name city project name (npm package name)
 * @param {string[]} opts.modules ids of the contracted feature modules
 */
export function scaffold({ targetDir, name, modules }) {
  const base = JSON.parse(readFileSync(join(TEMPLATE, 'synced', 'base-package.json'), 'utf8'));
  const version = SELF.version;

  // ── Template copy ─────────────────────────────────────────────────────────
  mkdirSync(targetDir, { recursive: true });
  for (const entry of readdirSync(join(TEMPLATE, 'synced'))) {
    if (entry === 'base-package.json') continue;
    cpSync(join(TEMPLATE, 'synced', entry), join(targetDir, dotName(entry)), { recursive: true });
  }
  for (const entry of readdirSync(join(TEMPLATE, 'static'))) {
    cpSync(join(TEMPLATE, 'static', entry), join(targetDir, dotName(entry)), { recursive: true });
  }

  // ── package.json ──────────────────────────────────────────────────────────
  const dependencies = { ...base.dependencies };
  for (const [mod, extras] of Object.entries(MODULE_ONLY_DEPS)) {
    if (!modules.includes(mod)) for (const dep of extras) delete dependencies[dep];
  }
  dependencies['@modelcity/core'] = version;
  for (const mod of modules) dependencies[`@modelcity/${mod}`] = version;

  const pkg = {
    name,
    version: '0.1.0',
    private: true,
    scripts: Object.fromEntries(SCRIPTS.map((s) => [s, base.scripts[s]])),
    dependencies: sorted(dependencies),
    devDependencies: sorted({
      '@modelcity/cli': version,
      ...Object.fromEntries(DEV_DEPS.map((d) => [d, base.devDependencies[d]])),
    }),
  };
  writeFileSync(join(targetDir, 'package.json'), `${JSON.stringify(pkg, null, 2)}\n`);

  // ── modules.config.mjs — the city's declaration of contracted modules ─────
  const config = [
    '/**',
    ' * City module registry — the single source of truth for which platform',
    ' * feature modules this city contracts and how the build-time codegen',
    ' * (`modelcity gen`) composes them.',
    ' *',
    ' * To add a module later: `npm install @modelcity/<id>@<platform version>`,',
    ' * import its manifest here and append it to FEATURE_MODULES. To drop one,',
    ' * do the reverse. A contracted module can also be disabled per build by',
    ' * setting its `MODULE_*` env flag to exactly "false".',
    ' *',
    ' * Display/registration order is the order below (drives nav section order',
    ' * and i18n loader merge precedence).',
    ' */',
    ...modules.map((m) => `import ${m} from '@modelcity/${m}/module.manifest.mjs';`),
    '',
    '/** All contracted feature modules, in registration/display order. */',
    `export const FEATURE_MODULES = [${modules.join(', ')}];`,
    '',
    '/** The always-on core module (its routes are always generated). */',
    "export const CORE_MODULE = { id: 'core', routeGroup: '(core)' };",
    '',
    '/** A feature module is enabled unless its flag is exactly "false". */',
    'export function isModuleEnabled(mod) {',
    "  return process.env[mod.envFlag] !== 'false';",
    '}',
    '',
    '/** The feature modules selected for this build. */',
    'export function enabledFeatureModules() {',
    '  return FEATURE_MODULES.filter(isModuleEnabled);',
    '}',
    '',
  ].join('\n');
  writeFileSync(join(targetDir, 'modules.config.mjs'), config);

  // ── README.md ─────────────────────────────────────────────────────────────
  const moduleRows = FEATURE_MODULES
    .filter((m) => modules.includes(m.id))
    .map((m) => `| \`@modelcity/${m.id}\` | ${m.description} | \`${m.flag}\` |`);
  const readme = [
    `# ${name}`,
    '',
    `City front-end built on the **Model City** platform (release train \`${version}\`),`,
    'scaffolded with `create-model-city-app`.',
    '',
    '## Quick start',
    '',
    '```bash',
    'cp .env.example .env.local   # fill in Auth0, gateway and Stripe values',
    'npm install',
    'npm run dev',
    '```',
    '',
    '`npm run dev` / `npm run build` first run `modelcity gen`, which generates',
    'the route shims, the composition root and the Tailwind source manifest for',
    'the contracted modules (see `modules.config.mjs`).',
    '',
    '## Contracted modules',
    '',
    '| Package | Provides | Build toggle |',
    '| --- | --- | --- |',
    ...moduleRows,
    '',
    'Set a toggle to exactly `false` (env var or Docker `--build-arg`) to exclude',
    'that module from a build without uninstalling it.',
    '',
    '## Customising',
    '',
    'City branding and reference data — city name, coat of arms, the',
    'authenticated-view backdrop, the landing photos, the default map focus and',
    'the neighbourhood catalogue — live in [`modelcity.config.js`](./modelcity.config.js)',
    '(imported by platform components via `@modelcity/config`). Edit that file to',
    'rebrand the portal; no rebuild flags or env vars are involved.',
    '',
    'Platform files are **not** edited in place — drop replacements under',
    '[`overrides/`](./overrides/README.md) (same path as in the `@modelcity/*`',
    'package). Route files are picked up by `modelcity gen`; everything else is',
    'redirected through the `jsconfig.json` paths fallback.',
    '',
    '## Upgrading the platform',
    '',
    'All `@modelcity/*` packages share one version. Upgrade them together:',
    '',
    '```bash',
    'npm install @modelcity/core@<v> @modelcity/cli@<v>' +
      (modules.length ? ' \\\n  ' + modules.map((m) => `@modelcity/${m}@<v>`).join(' ') : ''),
    '```',
    '',
  ].join('\n');
  writeFileSync(join(targetDir, 'README.md'), readme);

  return { version, modules };
}

/** `_gitignore` → `.gitignore` (npm strips dotfile names from tarballs). */
function dotName(entry) {
  return entry.startsWith('_') ? `.${entry.slice(1)}` : entry;
}

function sorted(obj) {
  return Object.fromEntries(Object.entries(obj).sort(([a], [b]) => a.localeCompare(b)));
}
