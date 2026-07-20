/** @type {import('next').NextConfig} */
const nextConfig = {
  reactCompiler: true,
  output: 'standalone',
  // Feature-module packages live in the workspace (packages/*) and ship source,
  // so Next must transpile them (React Compiler, JSX, modern syntax).
  transpilePackages: [
    '@modelcity/core',
    '@modelcity/leisure',
    '@modelcity/engagement',
    '@modelcity/mobility',
  ],
  // Map plain env var names (no NEXT_PUBLIC_ prefix) to the NEXT_PUBLIC_ names the
  // client-side code uses. Resolved at build time from --build-arg (Docker) or from
  // .env.production (local dev). Defaults keep local development working out of the box.
  env: {
    NEXT_PUBLIC_MICROSERVICE_ALB_URL:   process.env.MICROSERVICE_ALB_URL   ?? 'https://localhost:8443',
    NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY: process.env.STRIPE_PUBLISHABLE_KEY ?? '',
    NEXT_PUBLIC_BACKGROUND_IMAGE_URL:    process.env.BACKGROUND_IMAGE_URL  ?? 'https://www.spain.info/export/sites/segtur/.content/imagenes/rutas/escapada-aranjuez/palacio-real-aranjuez-s1089121742.jpg',
    // Feature-module toggles (mirror the back-end microservices). Default ON;
    // set to "false" to hide a module from navigation and the home grid and to
    // 404 its routes. See src/lib/config/modules.js.
    NEXT_PUBLIC_MODULE_LEISURE:    process.env.MODULE_LEISURE    ?? 'true',
    NEXT_PUBLIC_MODULE_ENGAGEMENT: process.env.MODULE_ENGAGEMENT ?? 'true',
    NEXT_PUBLIC_MODULE_MOBILITY:   process.env.MODULE_MOBILITY   ?? 'true',
  },
};

export default nextConfig;
