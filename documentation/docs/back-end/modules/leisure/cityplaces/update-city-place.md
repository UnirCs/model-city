---
title: Replace city place
sidebar_label: Replace city place
sidebar_position: 4
---

# Replace city place

`PUT /api/leisure/city-places/{id}` → `UpdateCityPlaceUseCase`

Fully replaces an existing city place. **Admin or backoffice**. If the city place does not
exist, `404`. Returns `200` with `CityPlaceDto`.

Audits `CITY_PLACE_UPDATED` and evicts `cityPlace`, `cityPlaces` and `cityRoutePlaces`.

## Inputs

**`PUT /api/leisure/city-places/{id}`** — same body as `POST /api/leisure/city-places`.

## Outputs

- **`200 OK`** — `CityPlaceDto`.
- **`400 Bad Request`** — validation error.
- **`403 Forbidden`** — not admin or backoffice.
- **`404 Not Found`** — city place not found.

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
        participant UU as UpdateCityPlaceUseCase
        participant PS as CityPlaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>GW: PUT /api/leisure/city-places/{id} {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: PUT /city-places/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>UU: execute(id, sub, request, locale)
        UU->>PS: findById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>PS: update(id, request)
            PS->>PR: save(CityPlace)
            PR-->>PS: CityPlace
            PS-->>UU: CityPlaceView
            UU->>TG: cityPlaceUpdated (audit → leisure_trails)
            Note over UU: evicts cityPlace + cityPlaces + cityRoutePlaces
            UU-->>C: 200 CityPlaceDto
        end
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
        participant UU as UpdateCityPlaceUseCase
        participant PS as CityPlaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>SEC: PUT /api/leisure/city-places/{id} {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: PUT /city-places/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>UU: execute(id, sub, request, locale)
        UU->>PS: findById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>PS: update(id, request)
            PS->>PR: save(CityPlace) [single modelcity DB]
            PR-->>PS: CityPlace
            PS-->>UU: CityPlaceView
            UU->>TG: cityPlaceUpdated (audit → leisure_trails)
            Note over UU: evicts cityPlace + cityPlaces + cityRoutePlaces
            UU-->>C: 200 CityPlaceDto
        end
    end
```
