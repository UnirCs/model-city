#!/usr/bin/env node
/**
 * `create-model-city-app` — scaffolds a Model City city front-end
 * (the npm analogue of the back-end's Maven archetypes).
 *
 * Usage:
 *   npm create model-city-app@latest <dir> [options]
 *
 * Options:
 *   --name=<name>       npm package name (default: the directory name)
 *   --modules=<list>    comma-separated feature modules, `all` or `none`
 *                       (default: prompt; with --yes: all)
 *   --yes               accept all defaults, no prompts (CI-friendly)
 */
import { existsSync, readdirSync } from 'node:fs';
import { basename, resolve } from 'node:path';
import readline from 'node:readline/promises';
import { FEATURE_MODULES, scaffold } from './lib/scaffold.mjs';

const args = process.argv.slice(2);
const flags = new Map(
  args.filter((a) => a.startsWith('--')).map((a) => {
    const [k, v] = a.slice(2).split('=');
    return [k, v ?? true];
  }),
);
const positional = args.filter((a) => !a.startsWith('--'));

if (flags.has('help') || flags.has('h')) {
  console.log(`Usage: npm create model-city-app@latest <dir> [--name=<name>] [--modules=<list>|all|none] [--yes]`);
  process.exit(0);
}

const dir = positional[0] ?? 'model-city-app';
const targetDir = resolve(process.cwd(), dir);
if (existsSync(targetDir) && readdirSync(targetDir).length > 0) {
  console.error(`[create-model-city-app] ${dir} already exists and is not empty.`);
  process.exit(1);
}

const name = typeof flags.get('name') === 'string' ? flags.get('name') : basename(targetDir);

/** Resolve the contracted modules from --modules / --yes / interactive prompt. */
async function selectModules() {
  const all = FEATURE_MODULES.map((m) => m.id);
  const flag = flags.get('modules');
  if (typeof flag === 'string') {
    if (flag === 'all') return all;
    if (flag === 'none') return [];
    const ids = flag.split(',').map((s) => s.trim()).filter(Boolean);
    const unknown = ids.filter((id) => !all.includes(id));
    if (unknown.length) {
      console.error(`[create-model-city-app] unknown module(s): ${unknown.join(', ')} (available: ${all.join(', ')})`);
      process.exit(1);
    }
    return ids;
  }
  if (flags.has('yes') || !process.stdin.isTTY) return all;

  const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
  const selected = [];
  console.log('Select the feature modules this city contracts (core is always included):');
  for (const mod of FEATURE_MODULES) {
    const answer = await rl.question(`  include ${mod.id} — ${mod.description}? [Y/n] `);
    if (!/^n(o)?$/i.test(answer.trim())) selected.push(mod.id);
  }
  rl.close();
  return selected;
}

const modules = await selectModules();
const { version } = scaffold({ targetDir, name, modules });

console.log(`
[create-model-city-app] created ${name} in ${dir} (platform ${version};` +
  ` modules: ${modules.join(', ') || '(none)'}).

Next steps:
  cd ${dir}
  cp .env.example .env.local   # fill in Auth0, gateway and Stripe values
  npm install
  npm run dev
`);
