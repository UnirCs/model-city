---
title: City route detail
sidebar_label: City route detail
sidebar_position: 2
---

# City route detail

`GET /api/leisure/city-routes/{id}` → `GetCityRouteUseCase`
`GET /api/leisure/city-routes/{id}?translations=full` → `GetCityRouteForEditUseCase`

Returns a city route with its ordered list of places (summaries). **Any authenticated user**.
If not found, `404`.

`?translations=full` returns every locale of `name` and `description` (admin editing) and is
not cached.

Standard detail is cached in `cityRoute` keyed by `locale-id`.

## Inputs

**`GET /api/leisure/city-routes/{id}`** — no body.

## Outputs

- **`200 OK`** — `CityRouteDto`.
- **`404 Not Found`** — route not found.

```json
{
  "id": 100,
  "name": "Art route",
  "description": "A walk through the city's main museums and galleries.",
  "targetAudience": "FAMILIES",
  "imageUrl": "https://cdn.modelcity.example/routes/art.jpg",
  "estimatedDurationMinutes": 180,
  "placeCount": 2,
  "cityPlaces": [
    {
      "id": 10,
      "name": "Reina Sofía Museum",
      "latitude": 40.4087,
      "longitude": -3.6945,
      "category": "MUSEUM",
      "coverPhotoUrl": "https://cdn.modelcity.example/places/reina-sofia-1.jpg"
    },
    {
      "id": 11,
      "name": "Thyssen-Bornemisza",
      "latitude": 40.4163,
      "longitude": -3.6942,
      "category": "MUSEUM",
      "coverPhotoUrl": "https://cdn.modelcity.example/places/thyssen-1.jpg"
    }
  ]
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
        participant GU as GetCityRouteUseCase
        participant GF as GetCityRouteForEditUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>GW: GET /api/leisure/city-routes/{id}?translations=full + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: GET /city-routes/{id}
    alt translations=full
        RC->>GF: execute(id, locale)
        GF->>RS: findById(id)
        RS->>RR: findById(id)
        alt not found
            RS-->>C: 404 Not Found
        else found
            RS-->>GF: CityRouteView
            GF-->>C: 200 CityRouteDto (all translations)
        end
    else standard
        RC->>GU: execute(id, locale)
        Note over GU: cache cityRoute[locale-id] if present
        GU->>RS: findById(id)
        RS->>RR: findById(id)
        alt not found
            RS-->>C: 404 Not Found
        else found
            RS-->>GU: CityRouteView
            GU-->>C: 200 CityRouteDto
        end
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
        participant GU as GetCityRouteUseCase
        participant GF as GetCityRouteForEditUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>SEC: GET /api/leisure/city-routes/{id}?translations=full + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: GET /city-routes/{id}
    alt translations=full
        RC->>GF: execute(id, locale)
        GF->>RS: findById(id)
        RS->>RR: findById(id) [single modelcity DB]
        alt not found
            RS-->>C: 404 Not Found
        else found
            RS-->>GF: CityRouteView
            GF-->>C: 200 CityRouteDto (all translations)
        end
    else standard
        RC->>GU: execute(id, locale)
        Note over GU: cache cityRoute[locale-id] if present
        GU->>RS: findById(id)
        RS->>RR: findById(id)
        alt not found
            RS-->>C: 404 Not Found
        else found
            RS-->>GU: CityRouteView
            GU-->>C: 200 CityRouteDto
        end
    end
```
