---
title: Replace city route
sidebar_label: Replace city route
sidebar_position: 6
---

# Replace city route

`PUT /api/leisure/city-routes/{id}` → `UpdateCityRouteUseCase`

Fully replaces an existing city route, including its ordered list of places. **Admin or
backoffice**. If not found, `404`. Returns `200` with `CityRouteDto`.

Audits `CITY_ROUTE_UPDATED` and evicts `cityRoute`, `cityRoutes` and `cityRoutePlaces`.

## Inputs

**`PUT /api/leisure/city-routes/{id}`** — same body as creation.

## Outputs

- **`200 OK`** — `CityRouteDto`.
- **`400 Bad Request`** — validation error.
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
        participant UU as UpdateCityRouteUseCase
        participant RS as CityRouteStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>GW: PUT /api/leisure/city-routes/{id} {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: PUT /city-routes/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RC->>UU: execute(id, sub, request, locale)
        UU->>RS: findById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>RS: update(id, request)
            RS->>RR: save(CityRoute + routePlaces)
            RR-->>RS: CityRoute
            RS-->>UU: CityRouteView
            UU->>TG: cityRouteUpdated (audit → leisure_trails)
            Note over UU: evicts cityRoute + cityRoutes + cityRoutePlaces
            UU-->>C: 200 CityRouteDto
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
        participant UU as UpdateCityRouteUseCase
        participant RS as CityRouteStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>SEC: PUT /api/leisure/city-routes/{id} {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: PUT /city-routes/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RC->>UU: execute(id, sub, request, locale)
        UU->>RS: findById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>RS: update(id, request)
            RS->>RR: save(CityRoute + routePlaces) [single modelcity DB]
            RR-->>RS: CityRoute
            RS-->>UU: CityRouteView
            UU->>TG: cityRouteUpdated (audit → leisure_trails)
            Note over UU: evicts cityRoute + cityRoutes + cityRoutePlaces
            UU-->>C: 200 CityRouteDto
        end
    end
```
