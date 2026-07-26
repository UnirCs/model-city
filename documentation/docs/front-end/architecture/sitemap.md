---
title: Sitemap & navigation
sidebar_label: Sitemap & navigation
sidebar_position: 3
---

# Sitemap & navigation

This page maps every navigable page under `src/app/[lang]/` to its module source
(`packages/<module>/routes/**`, see [Project structure](./project-structure.md))
and to the role gating that governs it, as derived from each module's navigation
contributor (`packages/<module>/lib/nav/sections.js`, composed by
`core/lib/nav/sections.js`).

## Page tree

```mermaid
flowchart TD
    Root(("/")) -->|"Proxy: language detection + Auth0"| Lang["/{lang}"]

    subgraph Public["Public zone — no session (routes-public)"]
        Landing["/{lang} (landing)"]
        Register["/{lang}/register"]
        Glossary["/{lang}/help/glossary"]
    end

    Lang --> Landing
    Lang --> Register
    Lang --> Glossary

    Landing -->|"Auth0 login"| Gate
    Register -->|"after registration"| Gate

    Gate{{"(app) layout — session + registration gate + AppShell"}}

    Gate --> CoreMod
    Gate --> LeisureMod
    Gate --> EngagementMod
    Gate --> MobilityMod

    subgraph CoreMod["core — always on"]
        Home["/home"]
        Profile["/profile"]
        HelpApp["/help"]
        Admin{{"/administration"}}:::staff
        Admin --> AdminCitizens["/administration/citizens + /[id]"]:::staff
        Admin --> AdminWorkers["/administration/workers + /[id]"]:::staff
        Admin --> AdminRecords["/administration/records"]:::staff
    end

    subgraph LeisureMod["leisure — events, sports, tourism"]
        Events["/events"]
        Events --> EventsMine["/events/my-tickets"]
        Events --> EventDetail["/events/[id] + /checkout(+/return) + /tickets"]
        Events --> EventsNew["/events/new + /[id]/edit"]:::staff

        Sports["/sports-spaces"]
        Sports --> SportsDetail["/sports-spaces/[id] + /resources/[rid]"]
        Sports --> SportsNew["/sports-spaces/new + /[id]/edit"]:::staff

        TourRoutes["/tourism/routes"]
        TourRoutes --> TourRouteDetail["/tourism/routes/[id] + /places/[placeId]"]
        TourRoutes --> TourRouteNew["/tourism/routes/new + /[id]/edit"]:::staff

        TourLoc["/tourism/locations"]
        TourLoc --> TourLocDetail["/tourism/locations/[id]"]
        TourLoc --> TourLocNew["/tourism/locations/new + /[id]/edit"]:::staff
    end

    subgraph EngagementMod["citizen-engagement — participation & security"]
        Questions["/participation/questions"]
        Questions --> QuestionDetail["/participation/questions/[id]"]
        Questions --> QuestionNew["/participation/questions/new"]

        Alerts["/security/alerts"]
        Alerts --> AlertNew["/security/alerts/new"]
        Alerts --> AlertManage["/security/alerts/manage"]:::staff
    end

    subgraph MobilityMod["mobility — mobility operations"]
        MyStays["/mobility/my-stays"]
        Cars["/mobility/cars"]
        MySanctions["/mobility/my-sanctions"]
        Reserve["/mobility/reserve"]
        ParkingReturn["/mobility/parking-checkout/return"]
        Tickets["/mobility/tickets"]:::staff
        Sanctions["/mobility/sanctions"]:::staff
        Sanctions --> SanctionsNew["/mobility/sanctions/new"]:::staff
    end

    classDef staff fill:#ffe0b3,stroke:#c9781f,color:#5c3d0e;
```

Orange nodes are staff-only sections/pages, gated by capability helpers in
`core/lib/auth/roles.js` and each module's own `lib/auth/roles.js`.

## Reading notes

- **URLs are the source of truth here, not the folder tree.** Every path under
  `src/app/[lang]/...` is a *generated shim* (see [City overrides](../extensibility-guide/index.md))
  that re-exports the real route logic from `packages/<module>/routes/**`. The
  diagram uses the resulting URL, which is what matters for navigation and for a
  city override (`overrides/<module>/routes/**`) that keeps the same path.
- **The `(app)` gate** (`packages/core/routes-app/layout.js`) enforces session +
  registration before any module route renders; everything below `Gate` sits
  behind it. `Landing`, `Register` and `Glossary` are emitted by `routes-public/`
  at the bare `[lang]` root, outside the gate.
- **Role-gating source of truth** is each module's `lib/nav/sections.js`:
  - `core`: `canAccessAdministration` gates the whole `/administration` section.
  - `leisure`: `canViewGeneralSections` / `canViewBookings` split the `/events`
    and `/sports-spaces` items; `isStaffUser` hides `/events/my-tickets` from staff.
  - `citizen-engagement`: `canManageSecurityAlerts` gates `/security/alerts/manage`.
  - `mobility`: `isStaffUser` / `canViewMobilityOps` split citizen pages
    (`/mobility/my-stays`, `/mobility/cars`, `/mobility/my-sanctions`) from
    staff-ops pages (`/mobility/tickets`, `/mobility/sanctions`).
- **Not shown**: the root shell above `[lang]` (`layout.js`, `not-found.js`,
  `robots.js`, `sitemap.js`, generated from `core/routes-root/`) — these are not
  user-navigable pages.
- A module disabled via `MODULE_*` flags ([Modularity](./modularity.md)) removes
  its whole subgraph from both the nav and the generated routes.
