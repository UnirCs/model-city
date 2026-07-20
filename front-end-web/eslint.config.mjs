import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";

const eslintConfig = defineConfig([
  ...nextVitals,
  // Tighten WCAG 2.2 AAA coverage. `jsx-a11y` is registered by
  // eslint-config-next only for the `next` block's file glob, so this override
  // must target the same glob (rather than redefining the plugin, which flat
  // config forbids) for the rules to resolve.
  {
    files: ["**/*.{js,jsx,mjs,ts,tsx,mts,cts}"],
    rules: {
      "jsx-a11y/anchor-ambiguous-text": "warn",
      "jsx-a11y/control-has-associated-label": "warn",
      "jsx-a11y/no-aria-hidden-on-focusable": "error",
      "jsx-a11y/prefer-tag-over-role": "warn",
    },
  },
  // Override default ignores of eslint-config-next.
  globalIgnores([
    // Default ignores of eslint-config-next:
    ".next/**",
    "out/**",
    "build/**",
    "next-env.d.ts",
  ]),
]);

export default eslintConfig;
