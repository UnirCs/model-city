---
title: Create public space
sidebar_label: Create public space
sidebar_position: 3
---

# Create public space

`POST /api/leisure/public-spaces` → `CreatePublicSpaceUseCase`

Creates a new public space. **Admin only**. `name` and `description` are required multi-locale
maps (`es` mandatory); `latitude`/`longitude` are constrained to valid ranges; `photoUrls` has
at most 3 entries. Returns `201` with `PublicSpaceDto`.

Audits `PUBLIC_SPACE_CREATED` and evicts `publicSpaces`.

## Inputs

**`POST /api/leisure/public-spaces`**

```json
{
  "name": {
    "es": "Centro Deportivo Central",
    "en": "Central Sports Centre"
  },
  "description": {
    "es": "Centro polideportivo público en el centro.",
    "en": "Multi-sports public centre in the city centre."
  },
  "address": {
    "es": "Avenida del Deporte 1",
    "en": "1 Avenida del Deporte"
  },
  "latitude": 40.415,
  "longitude": -3.705,
  "photoUrls": ["https://cdn.modelcity.example/spaces/centro-deporte-1.jpg"]
}
```

## Outputs

- **`201 Created`** — `PublicSpaceDto`.
- **`400 Bad Request`** — validation error.
- **`403 Forbidden`** — not admin.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant PSC as PublicSpaceController
        participant CU as CreatePublicSpaceUseCase
        participant PS as PublicSpaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>GW: POST /api/leisure/public-spaces {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PSC: POST /public-spaces (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PSC->>CU: execute(sub, request, locale)
        CU->>PS: create(request)
        PS->>PR: save(PublicSpace)
        PR-->>PS: PublicSpace
        PS-->>CU: PublicSpaceView
        CU->>TG: publicSpaceCreated (audit → leisure_trails)
        Note over CU: evicts publicSpaces
        CU-->>C: 201 PublicSpaceDto
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant PSC as PublicSpaceController
        participant CU as CreatePublicSpaceUseCase
        participant PS as PublicSpaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>SEC: POST /api/leisure/public-spaces {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PSC: POST /public-spaces (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PSC->>CU: execute(sub, request, locale)
        CU->>PS: create(request)
        PS->>PR: save(PublicSpace) [single modelcity DB]
        PR-->>PS: PublicSpace
        PS-->>CU: PublicSpaceView
        CU->>TG: publicSpaceCreated (audit → leisure_trails)
        Note over CU: evicts publicSpaces
        CU-->>C: 201 PublicSpaceDto
    end
```
