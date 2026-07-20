# Model City — Front-End Web

Front-end web application for the **Model City** platform, built with [Next.js](https://nextjs.org) (App Router), [Tailwind CSS v4](https://tailwindcss.com), and [Auth0](https://auth0.com).

## Overview

The app serves as the citizen-facing interface of the Model City project. It lets users browse neighbourhood services, manage their profile, and complete an onboarding registration flow. All UI is available in **Spanish, English, and French** through a `[lang]` dynamic route segment.

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Next.js 16 (App Router) |
| Styling | Tailwind CSS v4 |
| Auth | Auth0 (`@auth0/nextjs-auth0`) |
| Language | JavaScript (React 19) |

## Project Structure

```
src/
├── app/
│   ├── [lang]/          # Internationalised routes (es / en / fr)
│   │   ├── (app)/       # Authenticated area: home, profile
│   │   └── (onboarding)/# Registration flow
│   └── api/             # API routes
├── components/          # UI components (atoms → molecules → organisms)
├── lib/
│   ├── auth0.js         # Auth0 helpers
│   └── i18n/            # Dictionary loader, locale files, i18n utilities
└── data-mocks/          # Static JSON fixtures used during development
```

## Getting Started

```bash
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000). The proxy automatically redirects bare paths to the preferred language (e.g. `/es/home`).

## Available Scripts

| Script | Description |
|---|---|
| `npm run dev` | Start the development server |
| `npm run build` | Create a production build |
| `npm run start` | Serve the production build |
| `npm run lint` | Run ESLint |
