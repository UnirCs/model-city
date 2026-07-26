---
title: List city routes
sidebar_label: List city routes
sidebar_position: 1
---

# List city routes

`GET /api/leisure/city-routes` → `GetCityRoutesUseCase`

Returns paginated city routes. **Any authenticated user**. Page size is **3**, sorted by
`id` ascending. Cached in `cityRoutes` keyed by `locale-page`.

## Inputs

**`GET /api/leisure/city-routes?page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<CityRouteSummaryDto>`.

```json
{
  "content": [
    {
      "id": 100,
      "name": "Art route",
      "targetAudience": "FAMILIES",
      "imageUrl": "https://cdn.modelcity.example/routes/art.jpg",
      "estimatedDurationMinutes": 180,
      "placeCount": 3
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 3
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant RC as CityRouteController
        participant GU as GetCityRoutesUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>GW: GET /api/leisure/city-routes?page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: GET /city-routes
    RC->>GU: execute(page, locale)
    Note over GU: cache cityRoutes[locale-page] if present
    GU->>RS: findAll(PageRequest(0,3,id asc))
    RS->>RR: findAll(pageable)
    RR-->>RS: Page<CityRoute>
    RS-->>GU: Page<CityRouteView>
    GU-->>C: 200 Page<CityRouteSummaryDto>
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant RC as CityRouteController
        participant GU as GetCityRoutesUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>SEC: GET /api/leisure/city-routes?page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: GET /city-routes
    RC->>GU: execute(page, locale)
    Note over GU: cache cityRoutes[locale-page] if present
    GU->>RS: findAll(PageRequest(0,3,id asc))
    RS->>RR: findAll(pageable) [single modelcity DB]
    RR-->>RS: Page<CityRoute>
    RS-->>GU: Page<CityRouteView>
    GU-->>C: 200 Page<CityRouteSummaryDto>
```
