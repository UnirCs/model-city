---
title: Update reservable resource
sidebar_label: Update reservable resource
sidebar_position: 8
---

# Update reservable resource

`PUT /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}` → `UpdateReservableResourceUseCase`

Fully replaces a reservable resource. **Admin or operator**. If the public space or resource
is not found, `404`. Returns `200` with `ReservableResourceDto`.

Audits `RESERVABLE_RESOURCE_UPDATED` and evicts `reservableResources`.

## Inputs

**`PUT /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}`**

Same body as `POST /api/leisure/public-spaces/{publicSpaceId}/resources`.

## Outputs

- **`200 OK`** — `ReservableResourceDto`.
- **`400 Bad Request`** — validation error.
- **`403 Forbidden`** — not admin or operator.
- **`404 Not Found`** — resource not found.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / operator)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant RRC as ReservableResourceController
        participant UU as UpdateReservableResourceUseCase
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
    end

    C->>GW: PUT /api/leisure/public-spaces/{id}/resources/{rid} {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RRC: PUT /public-spaces/{id}/resources/{rid}
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RRC->>UU: execute(publicSpaceId, resourceId, sub, request, locale)
        UU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>RR: update(resourceId, publicSpaceId, request)
            RR->>RSR: save(ReservableResource)
            RSR-->>RR: ReservableResource
            RR-->>UU: ReservableResourceView
            UU->>TG: reservableResourceUpdated (audit → leisure_trails)
            Note over UU: evicts reservableResources
            UU-->>C: 200 ReservableResourceDto
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / operator)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant RRC as ReservableResourceController
        participant UU as UpdateReservableResourceUseCase
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
    end

    C->>SEC: PUT /api/leisure/public-spaces/{id}/resources/{rid} {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RRC: PUT /public-spaces/{id}/resources/{rid}
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RRC->>UU: execute(publicSpaceId, resourceId, sub, request, locale)
        UU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>RR: update(resourceId, publicSpaceId, request)
            RR->>RSR: save(ReservableResource) [single modelcity DB]
            RSR-->>RR: ReservableResource
            RR-->>UU: ReservableResourceView
            UU->>TG: reservableResourceUpdated (audit → leisure_trails)
            Note over UU: evicts reservableResources
            UU-->>C: 200 ReservableResourceDto
        end
    end
```
