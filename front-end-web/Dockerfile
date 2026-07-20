# syntax=docker/dockerfile:1

FROM node:20-slim AS base
WORKDIR /app

ENV NEXT_TELEMETRY_DISABLED=1

FROM base AS deps
COPY package.json package-lock.json ./
# Workspace manifests are required for `npm ci` to link the workspaces.
COPY packages/core/package.json ./packages/core/
COPY packages/leisure/package.json ./packages/leisure/
COPY packages/engagement/package.json ./packages/engagement/
COPY packages/mobility/package.json ./packages/mobility/
COPY packages/cli/package.json ./packages/cli/
COPY create-model-city-app/package.json ./create-model-city-app/
RUN npm ci

FROM base AS builder
# Build-time config — injected by the CI/deploy script via --build-arg.
# next.config.mjs maps these to NEXT_PUBLIC_* so no code changes are needed.
ARG MICROSERVICE_ALB_URL
ARG STRIPE_PUBLISHABLE_KEY
ARG BACKGROUND_IMAGE_URL
# Feature-module toggles (default "true" when the build-arg is omitted).
ARG MODULE_LEISURE=true
ARG MODULE_ENGAGEMENT=true
ARG MODULE_MOBILITY=true

ENV MICROSERVICE_ALB_URL=$MICROSERVICE_ALB_URL
ENV STRIPE_PUBLISHABLE_KEY=$STRIPE_PUBLISHABLE_KEY
ENV BACKGROUND_IMAGE_URL=$BACKGROUND_IMAGE_URL
ENV MODULE_LEISURE=$MODULE_LEISURE
ENV MODULE_ENGAGEMENT=$MODULE_ENGAGEMENT
ENV MODULE_MOBILITY=$MODULE_MOBILITY

COPY --from=deps /app/node_modules ./node_modules
COPY package.json package-lock.json ./
COPY next.config.mjs jsconfig.json postcss.config.mjs modules.config.mjs ./
COPY public ./public
COPY packages ./packages
COPY src ./src
# `npm run build` runs `prebuild` → `modelcity gen` (packages/cli), which reads
# the MODULE_* env above to generate the route shims, composition root and
# Tailwind source manifest for the selected modules before `next build`.
RUN npm run build

FROM node:20-slim AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV HOSTNAME=0.0.0.0
ENV PORT=3000

RUN groupadd --system --gid 1001 nodejs \
  && useradd --system --uid 1001 --gid nodejs nextjs

COPY --from=builder --chown=nextjs:nodejs /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

CMD ["node", "server.js"]
