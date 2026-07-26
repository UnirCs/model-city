---
title: Delete city route
sidebar_label: Delete city route
sidebar_position: 7
---

# Delete city route

`DELETE /api/leisure/city-routes/{id}` → `DeleteCityRouteUseCase`

Deletes a city route. **Admin or backoffice**. If not found, `404`. On success, returns
`204 No Content`.

Audits `CITY_ROUTE_DELETED` and evicts `cityRoute`, `cityRoutes` and `cityRoutePlaces`.

## Inputs

**`DELETE /api/leisure/city-routes/{id}`** — no body.

## Outputs

- **`204 No Content`** — route deleted.
- **`403 Forbidden`** — not admin or backoffice.
- **`404 Not Found`** — route not found.

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
        participant DU as DeleteCityRouteUseCase
        participant RS as CityRouteStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>GW: DELETE /api/leisure/city-routes/{id} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: DELETE /city-routes/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RC->>DU: execute(id, sub)
        DU->>RS: findById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>RS: deleteById(id)
            RS->>RR: deleteById(id)
            DU->>TG: cityRouteDeleted (audit → leisure_trails)
            Note over DU: evicts cityRoute + cityRoutes + cityRoutePlaces
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
        participant RC as CityRouteController
        participant DU as DeleteCityRouteUseCase
        participant RS as CityRouteStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>SEC: DELETE /api/leisure/city-routes/{id} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: DELETE /city-routes/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RC->>DU: execute(id, sub)
        DU->>RS: findById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>RS: deleteById(id)
            RS->>RR: deleteById(id) [single modelcity DB]
            DU->>TG: cityRouteDeleted (audit → leisure_trails)
            Note over DU: evicts cityRoute + cityRoutes + cityRoutePlaces
            DU-->>C: 204 No Content
        end
    end
```
