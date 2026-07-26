---
title: Create city route
sidebar_label: Create city route
sidebar_position: 5
---

# Create city route

`POST /api/leisure/city-routes` → `CreateCityRouteUseCase`

Creates a new city route with an ordered list of city places. **Admin or backoffice**.
`name`, `description`, `targetAudience` and `cityPlaceIds` are required. Returns `201` with
`CityRouteDto`.

Audits `CITY_ROUTE_CREATED` and evicts `cityRoutes`.

## Inputs

**`POST /api/leisure/city-routes`**

```json
{
  "name": {
    "es": "Ruta del arte",
    "en": "Art route"
  },
  "description": {
    "es": "Paseo por los principales museos y galerías.",
    "en": "A walk through the city's main museums and galleries."
  },
  "targetAudience": "FAMILIES",
  "imageUrl": "https://cdn.modelcity.example/routes/art.jpg",
  "estimatedDurationMinutes": 180,
  "cityPlaceIds": [10, 11]
}
```

## Outputs

- **`201 Created`** — `CityRouteDto` (with resolved places).
- **`400 Bad Request`** — validation error or any `cityPlaceIds` not found (store-dependent).
- **`403 Forbidden`** — not admin or backoffice.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / backoffice)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant RC as CityRouteController
        participant CU as CreateCityRouteUseCase
        participant RS as CityRouteStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>GW: POST /api/leisure/city-routes {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: POST /city-routes (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RC->>CU: execute(sub, request, locale)
        CU->>RS: create(request)
        RS->>RR: save(CityRoute + routePlaces)
        RR-->>RS: CityRoute
        RS-->>CU: CityRouteView
        CU->>TG: cityRouteCreated (audit → leisure_trails)
        Note over CU: evicts cityRoutes
        CU-->>C: 201 CityRouteDto
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / backoffice)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant RC as CityRouteController
        participant CU as CreateCityRouteUseCase
        participant RS as CityRouteStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>SEC: POST /api/leisure/city-routes {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: POST /city-routes (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RC->>CU: execute(sub, request, locale)
        CU->>RS: create(request)
        RS->>RR: save(CityRoute + routePlaces) [single modelcity DB]
        RR-->>RS: CityRoute
        RS-->>CU: CityRouteView
        CU->>TG: cityRouteCreated (audit → leisure_trails)
        Note over CU: evicts cityRoutes
        CU-->>C: 201 CityRouteDto
    end
```
