---
title: Delete reservable resource
sidebar_label: Delete reservable resource
sidebar_position: 9
---

# Delete reservable resource

`DELETE /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}` → `DeleteReservableResourceUseCase`

Soft-deletes a reservable resource. **Admin or operator**. If not found, `404`. On success,
`204 No Content`.

Audits `RESERVABLE_RESOURCE_DELETED` and evicts `reservableResources`.

## Inputs

**`DELETE /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}`** — no body.

## Outputs

- **`204 No Content`** — resource soft-deleted.
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
        participant DU as DeleteReservableResourceUseCase
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
    end

    C->>GW: DELETE /api/leisure/public-spaces/{id}/resources/{rid} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RRC: DELETE /public-spaces/{id}/resources/{rid}
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RRC->>DU: execute(publicSpaceId, resourceId, sub)
        DU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>RR: softDelete(resourceId, publicSpaceId)
            RR->>RSR: soft delete
            DU->>TG: reservableResourceDeleted (audit → leisure_trails)
            Note over DU: evicts reservableResources
            DU-->>C: 204 No Content
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
        participant DU as DeleteReservableResourceUseCase
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
    end

    C->>SEC: DELETE /api/leisure/public-spaces/{id}/resources/{rid} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RRC: DELETE /public-spaces/{id}/resources/{rid}
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RRC->>DU: execute(publicSpaceId, resourceId, sub)
        DU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>RR: softDelete(resourceId, publicSpaceId)
            RR->>RSR: soft delete [single modelcity DB]
            DU->>TG: reservableResourceDeleted (audit → leisure_trails)
            Note over DU: evicts reservableResources
            DU-->>C: 204 No Content
        end
    end
```
