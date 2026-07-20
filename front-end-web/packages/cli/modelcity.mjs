#!/usr/bin/env node
/**
 * `modelcity` — build-time CLI for Model City front-end projects.
 *
 * Runs from the consumer project's root (a city app or the reference app in
 * the platform monorepo — both resolve `@modelcity/*` through `node_modules`,
 * as npm workspaces symlink the local packages there).
 */
import { run as gen } from './lib/gen.mjs';

const USAGE = `Usage: modelcity <command>

Commands:
  gen   Generate the route shims, the composition root and the Tailwind
        source manifest for the modules declared in ./modules.config.mjs.
`;

const [command] = process.argv.slice(2);

switch (command) {
  case 'gen':
    await gen();
    break;
  case undefined:
  case 'help':
  case '--help':
  case '-h':
    console.log(USAGE);
    break;
  default:
    console.error(`[modelcity] unknown command '${command}'\n\n${USAGE}`);
    process.exit(1);
}
