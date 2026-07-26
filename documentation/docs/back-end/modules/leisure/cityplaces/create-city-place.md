---
title: Create city place
sidebar_label: Create city place
sidebar_position: 3
---

# Create city place

`POST /api/leisure/city-places` → `CreateCityPlaceUseCase`

Creates a new point of interest. **Admin or backoffice**. The request is validated with
`jakarta.validation`: `name` and `description` are non-empty maps with mandatory `es`,
`latitude`/`longitude` are required, and `photoUrls` has at most 3 entries.

Returns `201` with `CityPlaceDto`. Audits `CITY_PLACE_CREATED` and evicts `cityPlaces` and
`cityRoutePlaces`.

## Inputs

**`POST /api/leisure/city-places`**

```json
{
  "name": {
    "es": "Museo Reina Sofía",
    "en": "Reina Sofía Museum"
  },
  "latitude": 40.4087,
  "longitude": -3.6945,
  "description": {
    "es": "Museo nacional de arte del siglo XX.",
    "en": "National museum of 20th-century art."
  },
  "address": {
    "es": "Calle de Santa Isabel, 52",
    "en": "52 Calle de Santa Isabel"
  },
  "photoUrls": [
    "https://cdn.modelcity.example/places/reina-sofia-1.jpg",
    "https://cdn.modelcity.example/places/reina-sofia-2.jpg"
  ],
  "accessInfo": {
    "es": "Metro Atocha",
    "en": "Atocha Metro"
  },
  "accessibilityInfo": {
    "es": "Acceso sin barreras",
    "en": "Step-free access"
  },
  "category": "MUSEUM",
  "visitDurationMinutes": 120
}
```

## Outputs

- **`201 Created`** — `CityPlaceDto`.
- **`400 Bad Request`** — validation error.
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
        participant PC as CityPlaceController
        participant CU as CreateCityPlaceUseCase
        participant PS as CityPlaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>GW: POST /api/leisure/city-places {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: POST /city-places (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>CU: execute(sub, request, locale)
        CU->>PS: create(request)
        PS->>PR: save(CityPlace)
        PR-->>PS: CityPlace
        PS-->>CU: CityPlaceView
        CU->>TG: cityPlaceCreated (audit → leisure_trails)
        Note over CU: evicts cityPlaces + cityRoutePlaces
        CU-->>C: 201 CityPlaceDto
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
        participant PC as CityPlaceController
        participant CU as CreateCityPlaceUseCase
        participant PS as CityPlaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>SEC: POST /api/leisure/city-places {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: POST /city-places (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>CU: execute(sub, request, locale)
        CU->>PS: create(request)
        PS->>PR: save(CityPlace) [single modelcity DB]
        PR-->>PS: CityPlace
        PS-->>CU: CityPlaceView
        CU->>TG: cityPlaceCreated (audit → leisure_trails)
        Note over CU: evicts cityPlaces + cityRoutePlaces
        CU-->>C: 201 CityPlaceDto
    end
```
