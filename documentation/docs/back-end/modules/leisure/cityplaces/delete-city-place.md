---
title: Delete city place
sidebar_label: Delete city place
sidebar_position: 5
---

# Delete city place

`DELETE /api/leisure/city-places/{id}` → `DeleteCityPlaceUseCase`

Deletes a city place. **Admin or backoffice**. If not found, `404`. On success, returns
`204 No Content`.

Audits `CITY_PLACE_DELETED` and evicts `cityPlace`, `cityPlaces` and `cityRoutePlaces`.

## Inputs

**`DELETE /api/leisure/city-places/{id}`** — no body.

## Outputs

- **`204 No Content`** — city place deleted.
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
        participant DU as DeleteCityPlaceUseCase
        participant PS as CityPlaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>GW: DELETE /api/leisure/city-places/{id} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: DELETE /city-places/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>DU: execute(id, sub)
        DU->>PS: findById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>PS: deleteById(id)
            PS->>PR: deleteById(id)
            DU->>TG: cityPlaceDeleted (audit → leisure_trails)
            Note over DU: evicts cityPlace + cityPlaces + cityRoutePlaces
            DU-->>C: 204 No Content
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
        participant DU as DeleteCityPlaceUseCase
        participant PS as CityPlaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as CityPlaceRepository
    end

    C->>SEC: DELETE /api/leisure/city-places/{id} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: DELETE /city-places/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>DU: execute(id, sub)
        DU->>PS: findById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>PS: deleteById(id)
            PS->>PR: deleteById(id) [single modelcity DB]
            DU->>TG: cityPlaceDeleted (audit → leisure_trails)
            Note over DU: evicts cityPlace + cityPlaces + cityRoutePlaces
            DU-->>C: 204 No Content
        end
    end
```
