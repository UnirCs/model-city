---
title: List route places
sidebar_label: List route places
sidebar_position: 3
---

# List places in a city route

`GET /api/leisure/city-routes/{id}/city-places` → `GetCityRoutePlacesUseCase`

Returns the ordered list of city places belonging to a route. **Any authenticated user**.
Default page size is **10** but the controller accepts any `Pageable` (`page`, `size`, `sort`).
Cached in `cityRoutePlaces` keyed by
`locale-route-{routeId}:{pageNumber}:{pageSize}`.

If the route does not exist, `404`.

## Inputs

**`GET /api/leisure/city-routes/{id}/city-places?page=0&size=10`** — no body.

## Outputs

- **`200 OK`** — `Page<CityPlaceSummaryDto>`.
- **`404 Not Found`** — route not found.

```json
{
  "content": [
    {
      "id": 10,
      "name": "Reina Sofía Museum",
      "latitude": 40.4087,
      "longitude": -3.6945,
      "category": "MUSEUM",
      "coverPhotoUrl": "https://cdn.modelcity.example/places/reina-sofia-1.jpg"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 10
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
        participant GU as GetCityRoutePlacesUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>GW: GET /api/leisure/city-routes/{id}/city-places?page&size + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: GET /city-routes/{id}/city-places
    RC->>GU: execute(routeId, pageable, locale)
    Note over GU: cache cityRoutePlaces[locale-route-id:page:size] if present
    GU->>RS: findById(routeId)
    RS->>RR: findById(routeId)
    alt not found
        RS-->>C: 404 Not Found
    else found
        RS-->>GU: CityRouteView with routePlaces
        Note over GU: subList(offset, offset+size)
        GU-->>C: 200 Page<CityPlaceSummaryDto>
    end
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
        participant GU as GetCityRoutePlacesUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>SEC: GET /api/leisure/city-routes/{id}/city-places?page&size + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: GET /city-routes/{id}/city-places
    RC->>GU: execute(routeId, pageable, locale)
    Note over GU: cache cityRoutePlaces[locale-route-id:page:size] if present
    GU->>RS: findById(routeId)
    RS->>RR: findById(routeId) [single modelcity DB]
    alt not found
        RS-->>C: 404 Not Found
    else found
        RS-->>GU: CityRouteView with routePlaces
        Note over GU: subList(offset, offset+size)
        GU-->>C: 200 Page<CityPlaceSummaryDto>
    end
```
