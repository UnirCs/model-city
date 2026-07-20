#!/usr/bin/env node
/**
 * Release-train version bump for the Model City front-end platform.
 *
 * All publishable packages share one platform version (like the back-end's
 * `modelcity.version` release train). This script sets that version across
 * every package and rewrites the internal `@modelcity/*` dependency pins to
 * match, then prints the git steps to cut the release (commit + `web-v<v>`
 * tag). Pushing the tag triggers `.github/workflows/publish-web.yml`, which
 * builds the reference app as a quality gate and publishes the train to npm.
 *
 * Usage: node scripts/release.mjs <semver>   (e.g. 1.1.0)
 */
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..');

const version = process.argv[2];
if (!version || !/^\d+\.\d+\.\d+(-[0-9A-Za-z.-]+)?$/.test(version)) {
  console.error('Usage: node scripts/release.mjs <semver>   (e.g. 1.1.0)');
  process.exit(1);
}

const PACKAGES = ['cli', 'core', 'leisure', 'engagement', 'mobility'];
const INTERNAL = /^@modelcity\//;

const files = PACKAGES.map((p) => join(ROOT, 'packages', p, 'package.json'));
// The archetype rides the same train when present.
const archetype = join(ROOT, 'create-model-city-app', 'package.json');
if (existsSync(archetype)) files.push(archetype);

for (const file of files) {
  const pkg = JSON.parse(readFileSync(file, 'utf8'));
  pkg.version = version;
  for (const depType of ['dependencies', 'peerDependencies', 'optionalDependencies']) {
    for (const dep of Object.keys(pkg[depType] ?? {})) {
      if (INTERNAL.test(dep)) pkg[depType][dep] = version;
    }
  }
  writeFileSync(file, `${JSON.stringify(pkg, null, 2)}\n`);
  console.log(`[release] ${pkg.name} → ${version}`);
}

console.log(`
[release] Done. To cut the release:

  git add -A
  git commit -m "MINOR Release web-v${version}"
  git tag web-v${version}
  git push origin HEAD web-v${version}
`);
