/**
 * Build-time module codegen (`modelcity gen`).
 *
 * Generates, from the modules selected for this build:
 *   1. The App Router route shims under `src/app/[lang]/(app)/(<group>)/…`,
 *      re-exporting each route's logic from its package
 *      (`@modelcity/<id>/routes/…`).
 *   2. The composition root `src/lib/composition/registerModules.js`,
 *      registering only the enabled modules' i18n loaders, nav contributors
 *      and home services into `core`.
 *   3. The Tailwind source manifest `src/styles/modules.css`, importing the
 *      (possibly overridden) core stylesheet and adding one `@source` per
 *      enabled feature module — their component sources live outside the app
 *      root (in `node_modules` for a city project, in the workspace for the
 *      platform monorepo), so Tailwind's auto-detection never sees them.
 *
 * A feature module disabled via its `MODULE_*` flag is simply not emitted: its
 * route group is left empty (so its URLs 404) and it is never imported (so its
 * code never enters the bundle) — real, Maven-reactor-style build-time
 * selection. The `core` module is always generated.
 *
 * Everything is read from the consumer project's working directory: the module
 * registry `./modules.config.mjs`, the override root `./overrides` (or
 * `CITY_OVERRIDES_DIR`) and the output tree `./src`. Module packages are
 * resolved through the project's `node_modules` — real installs in a city
 * project, workspace symlinks in the platform monorepo — so the CLI behaves
 * identically in both.
 *
 * All outputs are generated artifacts (git-ignored). Run via `npm run
 * gen:modules`; it also runs automatically on `prebuild` / `predev`.
 */
import { existsSync, readFileSync, writeFileSync, rmSync, mkdirSync, readdirSync, statSync } from 'node:fs';
import { createRequire } from 'node:module';
import { dirname, join, relative } from 'node:path';
import { pathToFileURL } from 'node:url';

export async function run() {
  const ROOT = process.cwd();
  const SRC_BASE = join(ROOT, 'src');
  const APP_BASE = join(SRC_BASE, 'app');
  const LANG_BASE = join(APP_BASE, '[lang]');
  const APP_GROUP_BASE = join(LANG_BASE, '(app)');
  // City override root (mirrors `@modelcity/<id>/…`). A city drops a route file
  // here to replace or add a route; its body is also redirected by the jsconfig
  // `paths` fallback, which is what makes every *non-route* module file
  // overridable too. See docs/architecture/route-overrides.md.
  const OVERRIDES = join(ROOT, process.env.CITY_OVERRIDES_DIR ?? 'overrides');

  // The module registry is the consumer project's own file — the city's
  // declaration of contracted modules (the Maven-reactor `<modules>` analogue).
  const configPath = join(ROOT, 'modules.config.mjs');
  if (!existsSync(configPath)) {
    console.error(`[modelcity gen] no modules.config.mjs found in ${ROOT} — run from the project root.`);
    process.exit(1);
  }
  const { CORE_MODULE, FEATURE_MODULES, enabledFeatureModules } = await import(pathToFileURL(configPath).href);

  // Resolve each module package through the project's node_modules (city:
  // real installs; monorepo: workspace symlinks). `require.resolve` is used
  // because the packages ship plain files with no `exports` map.
  const projectRequire = createRequire(join(ROOT, 'package.json'));
  const moduleRoots = new Map();
  function moduleRoot(id) {
    if (!moduleRoots.has(id)) {
      moduleRoots.set(id, dirname(projectRequire.resolve(`@modelcity/${id}/package.json`)));
    }
    return moduleRoots.get(id);
  }

  // Besides its gated `routes/` (→ `(app)/(core)/…`), `core` owns extra route
  // sources that emit shims at "root" levels the codegen does not own wholesale:
  //   - `routes-app/`    → `(app)/…`  : the `(app)` group's own layout/loading,
  //                                     i.e. the session/registration gate itself.
  //   - `routes-public/` → `[lang]/…` : public routes (landing, register, glossary)
  //                                     that must NOT sit behind the `(app)` gate,
  //                                     plus the `[lang]` layout/loading themselves.
  //   - `routes-root/`   → `src/app/…`: the true root app shell above `[lang]` —
  //                                     root layout, not-found, robots and sitemap.
  //   - `routes-src/`    → `src/…`    : files beside `app/` — currently the
  //                                     Proxy/Middleware (`src/proxy.js`).
  const APP_DIR = 'routes-app';
  const PUBLIC_DIR = 'routes-public';
  const ROOT_DIR = 'routes-root';
  const SRC_DIR = 'routes-src';

  const enabled = enabledFeatureModules();
  const routedModules = [CORE_MODULE, ...enabled];

  /**
   * Route sources: which package directory feeds which output base.
   *   - every routed module's `routes/` → `(app)/(<group>)/…` (gated);
   *   - core's `routes-app/`            → `(app)/…`            (the gate itself);
   *   - core's `routes-public/`         → `[lang]/…`           (public, ungated).
   */
  const sources = [
    ...routedModules.map((mod) => ({
      id: mod.id,
      dir: 'routes',
      outBase: join(APP_GROUP_BASE, mod.routeGroup),
    })),
    { id: CORE_MODULE.id, dir: APP_DIR, outBase: APP_GROUP_BASE },
    { id: CORE_MODULE.id, dir: PUBLIC_DIR, outBase: LANG_BASE },
    { id: CORE_MODULE.id, dir: ROOT_DIR, outBase: APP_BASE },
    { id: CORE_MODULE.id, dir: SRC_DIR, outBase: SRC_BASE },
  ];

  // Clean every known gated route group first (core + ALL feature modules, not
  // just the enabled ones), so a module disabled in this build has its stale
  // shims removed and its URLs 404 / its code drops out of the bundle.
  for (const mod of [CORE_MODULE, ...FEATURE_MODULES]) {
    rmSync(join(APP_GROUP_BASE, mod.routeGroup), { recursive: true, force: true });
  }

  // Clean the "root-level" shims core owns directly inside `(app)/` and `[lang]/`.
  // Remove ONLY the top-level names each source materialises (e.g. `layout.js`,
  // `loading.js`, `page.js`, `register`, `help`) — never the structural siblings
  // or the module route groups.
  for (const [dir, outBase] of [[APP_DIR, APP_GROUP_BASE], [PUBLIC_DIR, LANG_BASE], [ROOT_DIR, APP_BASE], [SRC_DIR, SRC_BASE]]) {
    const roots = [join(moduleRoot(CORE_MODULE.id), dir), join(OVERRIDES, CORE_MODULE.id, dir)];
    for (const name of new Set(
      roots.flatMap((root) => walk(root).map((file) => relative(root, file).split(/[\\/]/)[0])),
    )) {
      rmSync(join(outBase, name), { recursive: true, force: true });
    }
  }

  let shimCount = 0;
  for (const src of sources) {
    // Merge the upstream package routes with any city overrides, keyed by the
    // route-relative path. An override at the same path replaces the route; an
    // override-only path is a new city route. The shim's `@modelcity/…` spec is
    // stable — the jsconfig `paths` fallback redirects its body to the override
    // when present — so we only need the *winning* file's content (to detect the
    // export surface) and to emit a shim for added routes.
    const rels = new Map();
    const pkgBase = join(moduleRoot(src.id), src.dir);
    for (const file of walk(pkgBase)) {
      rels.set(relative(pkgBase, file), file);
    }
    const ovrBase = join(OVERRIDES, src.id, src.dir);
    for (const file of walk(ovrBase)) {
      const rel = relative(ovrBase, file);
      console.log(`[modelcity gen] override route (${rels.has(rel) ? 'replace' : 'add'}): ${src.id}/${src.dir}/${rel}`);
      rels.set(rel, file);
    }

    for (const [rel, file] of rels) {
      const spec = `@modelcity/${src.id}/${src.dir}/${rel.replace(/\.js$/, '')}`;
      const shimPath = join(src.outBase, rel);
      mkdirSync(dirname(shimPath), { recursive: true });
      writeFileSync(shimPath, makeShim(spec, readFileSync(file, 'utf8')));
      shimCount++;
    }
  }

  // Composition root — register only the enabled feature modules into core.
  const L = [
    '// GENERATED by `modelcity gen` — do not edit. Run `npm run gen:modules`.',
    "import { registerServiceLoaders } from '@modelcity/core/lib/i18n/dictionaries';",
    "import { registerNavContributor } from '@modelcity/core/lib/nav/sections';",
    "import { registerHomeServices } from '@modelcity/core/lib/config/services';",
    ...enabled.map((m) => `import { loaders as ${m.id}Loaders } from '@modelcity/${m.id}/lib/i18n';`),
    ...enabled.map((m) => `import { ${m.id}NavSections } from '@modelcity/${m.id}/lib/nav/sections';`),
    ...enabled.map((m) => `import { ${m.id}HomeServices } from '@modelcity/${m.id}/lib/home/services';`),
    '',
    'registerServiceLoaders({',
    ...['es', 'en', 'fr'].map(
      (lng) => `  ${lng}: { ${enabled.map((m) => `...${m.id}Loaders.${lng}`).join(', ')} },`,
    ),
    '});',
    '',
    ...enabled.map((m) => `registerNavContributor(${m.id}NavSections);`),
    '',
    ...enabled.map((m) => `registerHomeServices(${m.id}HomeServices);`),
    '',
  ].join('\n');
  mkdirSync(join(SRC_BASE, 'lib/composition'), { recursive: true });
  writeFileSync(join(SRC_BASE, 'lib/composition/registerModules.js'), L);

  // Tailwind source manifest — the single Tailwind entry the root layout
  // imports. It pulls in the (possibly city-overridden) core stylesheet, which
  // carries `@import "tailwindcss"`, and adds one `@source` per enabled feature
  // module so their component classes are scanned even though they live outside
  // the app root. Paths are emitted relative to the manifest so the output is
  // machine-independent.
  const stylesDir = join(SRC_BASE, 'styles');
  mkdirSync(stylesDir, { recursive: true });
  const cssRel = (target) => relative(stylesDir, target).replaceAll('\\', '/');
  const coreGlobalsOverride = join(OVERRIDES, CORE_MODULE.id, 'styles/globals.css');
  const coreGlobals = existsSync(coreGlobalsOverride)
    ? coreGlobalsOverride
    : join(moduleRoot(CORE_MODULE.id), 'styles/globals.css');
  const css = [
    '/* GENERATED by `modelcity gen` — do not edit. Run `npm run gen:modules`. */',
    `@import "${cssRel(coreGlobals)}";`,
    ...enabled.map((m) => `@source "${cssRel(join(moduleRoot(m.id), 'components'))}";`),
    '',
  ].join('\n');
  writeFileSync(join(stylesDir, 'modules.css'), css);

  const names = enabled.map((m) => m.id).join(', ') || '(none)';
  console.log(`[modelcity gen] enabled feature modules: ${names}; generated ${shimCount} route shims + composition root + Tailwind manifest.`);
}

/** Recursively collect `.js` files under a directory (empty if it doesn't exist). */
function walk(dir) {
  let entries;
  try { entries = readdirSync(dir); } catch { return []; }
  const out = [];
  for (const name of entries) {
    const p = join(dir, name);
    if (statSync(p).isDirectory()) out.push(...walk(p));
    else if (name.endsWith('.js')) out.push(p);
  }
  return out;
}

const HANDLED = new Set([
  'default', 'proxy', 'config', 'generateMetadata', 'generateStaticParams', 'metadata', 'dynamic',
]);

/**
 * Builds a route shim that re-exports a route file's actual surface:
 *   - any `// @shim-prelude: <statement>` directive lines are emitted verbatim at
 *     the very top of the shim. They are side-effect statements that must run from
 *     the orchestrator's app directory rather than from the core module — e.g. the
 *     generated composition root (`@/lib/composition/registerModules`) or the
 *     generated Tailwind manifest (`@/styles/modules.css`) — so they cannot live
 *     in the package file itself;
 *   - `default` (when present) + `proxy` (the Proxy/Middleware function) +
 *     `generateMetadata` / `generateStaticParams` / static `metadata` if present
 *     are re-exported (live ES bindings — Next evaluates them, so a named
 *     re-export is enough);
 *   - `dynamic` (a route config) and `config` (the Proxy `matcher`) are
 *     statically analysed by Next, so they are re-declared literally — a
 *     re-export would be invisible to that analysis. The `config` matcher object
 *     is captured as a single-level object literal.
 * Warns on any other named export so the generator can be extended deliberately.
 */
function makeShim(spec, src) {
  const named = [];
  if (/export\s+default\b/.test(src)) named.push('default');
  if (/export\s+(?:const|async\s+function|function)\s+proxy\b/.test(src)) named.push('proxy');
  if (/export\s+(?:const|async\s+function|function)\s+generateMetadata\b/.test(src)) named.push('generateMetadata');
  if (/export\s+async\s+function\s+generateStaticParams\b/.test(src)) named.push('generateStaticParams');
  if (/export\s+const\s+metadata\b/.test(src)) named.push('metadata');

  let out = '';
  for (const m of src.matchAll(/^\/\/\s*@shim-prelude:\s*(.+?)\s*$/gm)) out += `${m[1]}\n`;
  if (named.length) out += `export { ${named.join(', ')} } from '${spec}';\n`;

  const dyn = src.match(/export\s+const\s+dynamic\s*=\s*(['"][^'"]+['"])\s*;/);
  if (dyn) out += `export const dynamic = ${dyn[1]};\n`;

  const cfg = src.match(/export\s+const\s+config\s*=\s*(\{[\s\S]*?\n\});/);
  if (cfg) out += `export const config = ${cfg[1]};\n`;

  for (const m of src.matchAll(/export\s+(?:const|async\s+function|function)\s+([A-Za-z][A-Za-z0-9]*)/g)) {
    if (!HANDLED.has(m[1])) console.warn(`[modelcity gen] WARN unhandled export '${m[1]}' in ${spec}`);
  }
  return out;
}
