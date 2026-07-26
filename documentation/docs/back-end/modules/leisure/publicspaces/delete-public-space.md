---
title: Delete public space
sidebar_label: Delete public space
sidebar_position: 5
---

# Delete public space

`DELETE /api/leisure/public-spaces/{id}` → `DeletePublicSpaceUseCase`

Soft-deletes a public space and all its reservable resources. **Admin only**. If not found or
already deleted, `404`. On success, `204 No Content`.

Audits `PUBLIC_SPACE_DELETED` and evicts `publicSpace`, `publicSpaces` and
`reservableResources`.

## Inputs

**`DELETE /api/leisure/public-spaces/{id}`** — no body.

## Outputs

- **`204 No Content`** — public space soft-deleted.
- **`403 Forbidden`** — not admin.
- **`404 Not Found`** — public space not found.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant PSC as PublicSpaceController
        participant DU as DeletePublicSpaceUseCase
        participant PS as PublicSpaceStore
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>GW: DELETE /api/leisure/public-spaces/{id} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PSC: DELETE /public-spaces/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PSC->>DU: execute(id, sub)
        DU->>PS: findActiveById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>PS: softDelete(id)
            PS->>PR: soft delete
            DU->>RR: softDeleteByPublicSpace(id)
            DU->>TG: publicSpaceDeleted (audit → leisure_trails)
            Note over DU: evicts publicSpace + publicSpaces + reservableResources
            DU-->>C: 204 No Content
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant PSC as PublicSpaceController
        participant DU as DeletePublicSpaceUseCase
        participant PS as PublicSpaceStore
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>SEC: DELETE /api/leisure/public-spaces/{id} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PSC: DELETE /public-spaces/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PSC->>DU: execute(id, sub)
        DU->>PS: findActiveById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>PS: softDelete(id)
            PS->>PR: soft delete [single modelcity DB]
            DU->>RR: softDeleteByPublicSpace(id)
            DU->>TG: publicSpaceDeleted (audit → leisure_trails)
            Note over DU: evicts publicSpace + publicSpaces + reservableResources
            DU-->>C: 204 No Content
        end
    end
```
