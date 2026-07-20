#!/usr/bin/env node
/**
 * Derives `template/synced/` from the reference app (the front-end-web
 * monorepo root, one directory up) — the same anti-drift strategy the
 * back-end Maven archetypes use by copying the reference deployables' sources
 * at build time. Runs on `prepack`, so every published tarball carries a
 * template that matches the release it ships with.
 *
 * Verbatim copies: next/postcss/eslint configs, `public/`, the app favicons
 * and the reference root package.json (kept as `base-package.json`, the
 * source of the peer-stack versions at scaffold time).
 *
 * Transformed copies:
 *   - `jsconfig.json`: the `@modelcity/*` paths fallback is rewritten from the
 *     workspace (`./packages/…`) to installed packages
 *     (`./node_modules/@modelcity/…`).
 *   - `.gitignore` → `_gitignore` (npm strips `.gitignore` files from
 *     tarballs; the scaffolder renames it back): monorepo-only entries are
 *     dropped, and a city is expected to commit its own lockfile.
 */
import { cpSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = dirname(fileURLToPath(import.meta.url));
const REFERENCE = join(HERE, '..');
const SYNCED = join(HERE, 'template', 'synced');

rmSync(SYNCED, { recursive: true, force: true });
mkdirSync(join(SYNCED, 'src', 'app'), { recursive: true });

// ── Verbatim project files ──────────────────────────────────────────────────
for (const file of ['next.config.mjs', 'postcss.config.mjs', 'eslint.config.mjs']) {
  cpSync(join(REFERENCE, file), join(SYNCED, file));
}
cpSync(join(REFERENCE, 'public'), join(SYNCED, 'public'), { recursive: true });
for (const file of ['favicon.ico']) {
  const src = join(REFERENCE, 'src', 'app', file);
  if (existsSync(src)) cpSync(src, join(SYNCED, 'src', 'app', file));
}
// The reference root package.json — scaffold-time source of the peer-stack
// versions (next, react, auth0, stripe…), so they never drift from the
// versions the platform is actually built and tested against.
cpSync(join(REFERENCE, 'package.json'), join(SYNCED, 'base-package.json'));

// ── jsconfig.json: workspace paths → installed-package paths ────────────────
const jsconfig = readFileSync(join(REFERENCE, 'jsconfig.json'), 'utf8')
  .replaceAll('./packages/', './node_modules/@modelcity/');
writeFileSync(join(SYNCED, 'jsconfig.json'), jsconfig);

// ── .gitignore → _gitignore, minus monorepo-only entries ────────────────────
const MONOREPO_ONLY = [
  /^\/?designs(\/\*)?$/,      // design mockups live only in the platform repo
  /^\/?docs(\/\*)?$/,         // platform docs live only in the platform repo
  /^package-lock\.json$/,     // a city should commit its own lockfile
  /^tsconfig\.json$/,
  /^settings\.json$/,
];
const gitignore = readFileSync(join(REFERENCE, '.gitignore'), 'utf8')
  .split('\n')
  .filter((line) => !MONOREPO_ONLY.some((re) => re.test(line.trim())))
  .join('\n');
writeFileSync(join(SYNCED, '_gitignore'), gitignore);

console.log('[sync-template] template/synced refreshed from the reference app.');
