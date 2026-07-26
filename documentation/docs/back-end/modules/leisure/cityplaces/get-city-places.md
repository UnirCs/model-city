---
title: List city places
sidebar_label: List city places
sidebar_position: 1
---

# List city places

`GET /api/leisure/city-places` → `GetCityPlacesUseCase`

Returns paginated city places. **Any authenticated user**. Optional filter by `category`.
Page size is **6**, sorted by `id` ascending. Cached in `cityPlaces` keyed by
`locale-category-page`. Writes to city places evict this cache (and `cityRoutePlaces`).

## Inputs

**`GET /api/leisure/city-places?category=MUSEUM&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<CityPlaceSummaryDto>`.

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
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 6
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
        participant PC as CityPlaceController
        participant GU as GetCityPlacesUseCase
        participant PS as CityPlaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>GW: GET /api/leisure/city-places?category&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: GET /city-places
    PC->>GU: execute(category, page, locale)
    Note over GU: cache cityPlaces[locale-category-page] if present
    GU->>PS: findByCategory(category, PageRequest(0,6,id asc)) or findAll
    PS->>PR: findAll(spec, pageable)
    PR-->>PS: Page<CityPlace>
    PS-->>GU: Page<CityPlaceView>
    GU-->>C: 200 Page<CityPlaceSummaryDto>
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
        participant PC as CityPlaceController
        participant GU as GetCityPlacesUseCase
        participant PS as CityPlaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>SEC: GET /api/leisure/city-places?category&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: GET /city-places
    PC->>GU: execute(category, page, locale)
    Note over GU: cache cityPlaces[locale-category-page] if present
    GU->>PS: findByCategory(category, PageRequest(0,6,id asc)) or findAll
    PS->>PR: findAll(spec, pageable) [single modelcity DB]
    PR-->>PS: Page<CityPlace>
    PS-->>GU: Page<CityPlaceView>
    GU-->>C: 200 Page<CityPlaceSummaryDto>
```
