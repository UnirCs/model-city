---
title: City place detail
sidebar_label: City place detail
sidebar_position: 2
---

# City place detail

`GET /api/leisure/city-places/{id}` → `GetCityPlaceUseCase`
`GET /api/leisure/city-places/{id}?translations=full` → `GetCityPlaceForEditUseCase`

Returns the full detail of a city place. **Any authenticated user**. If not found, `404`.

The `?translations=full` variant returns every locale for each localizable field
(admin editing). Localizable fields: `name`, `description`, `address`, `accessInfo`,
`accessibilityInfo`.

The standard detail is cached in `cityPlace` keyed by `locale-id`; the full-translations
variant is not cached.

## Inputs

**`GET /api/leisure/city-places/{id}`** — no body.

## Outputs

- **`200 OK`** — `CityPlaceDto`.
- **`404 Not Found`** — city place not found.

```json
{
  "id": 10,
  "name": "Reina Sofía Museum",
  "latitude": 40.4087,
  "longitude": -3.6945,
  "description": "National museum of 20th-century art.",
  "address": "Calle de Santa Isabel, 52",
  "photoUrls": [
    "https://cdn.modelcity.example/places/reina-sofia-1.jpg",
    "https://cdn.modelcity.example/places/reina-sofia-2.jpg"
  ],
  "accessInfo": "Metro Atocha",
  "accessibilityInfo": "Step-free access",
  "category": "MUSEUM",
  "visitDurationMinutes": 120
}
```

`translations=full` example (truncated):

```json
{
  "id": 10,
  "name": "Reina Sofía Museum",
  "description": "National museum of 20th-century art.",
  "translations": {
    "name": {
      "es": "Museo Reina Sofía",
      "en": "Reina Sofía Museum"
    },
    "description": {
      "es": "Museo nacional de arte del siglo XX.",
      "en": "National museum of 20th-century art."
    }
  }
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
        participant GU as GetCityPlaceUseCase
        participant GF as GetCityPlaceForEditUseCase
        participant PS as CityPlaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>GW: GET /api/leisure/city-places/{id}?translations=full + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: GET /city-places/{id}
    alt translations=full
        PC->>GF: execute(id, locale)
        GF->>PS: findById(id)
        PS->>PR: findById(id)
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GF: CityPlaceView
            GF-->>C: 200 CityPlaceDto (all translations)
        end
    else standard
        PC->>GU: execute(id, locale)
        Note over GU: cache cityPlace[locale-id] if present
        GU->>PS: findById(id)
        PS->>PR: findById(id)
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GU: CityPlaceView
            GU-->>C: 200 CityPlaceDto
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
        participant PC as CityPlaceController
        participant GU as GetCityPlaceUseCase
        participant GF as GetCityPlaceForEditUseCase
        participant PS as CityPlaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>SEC: GET /api/leisure/city-places/{id}?translations=full + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: GET /city-places/{id}
    alt translations=full
        PC->>GF: execute(id, locale)
        GF->>PS: findById(id)
        PS->>PR: findById(id) [single modelcity DB]
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GF: CityPlaceView
            GF-->>C: 200 CityPlaceDto (all translations)
        end
    else standard
        PC->>GU: execute(id, locale)
        Note over GU: cache cityPlace[locale-id] if present
        GU->>PS: findById(id)
        PS->>PR: findById(id)
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GU: CityPlaceView
            GU-->>C: 200 CityPlaceDto
        end
    end
```
