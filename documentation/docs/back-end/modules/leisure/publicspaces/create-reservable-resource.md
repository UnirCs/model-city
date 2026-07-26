---
title: Create reservable resource
sidebar_label: Create reservable resource
sidebar_position: 7
---

# Create reservable resource

`POST /api/leisure/public-spaces/{publicSpaceId}/resources` → `CreateReservableResourceUseCase`

Adds a reservable resource to a public space. **Admin or operator**. `name` (multi-locale)
and `resourceType` are required. Returns `201` with `ReservableResourceDto`.

Audits `RESERVABLE_RESOURCE_CREATED` and evicts `reservableResources`.

## Inputs

**`POST /api/leisure/public-spaces/{publicSpaceId}/resources`**

```json
{
  "name": {
    "es": "Pista de fútbol principal",
    "en": "Main football pitch"
  },
  "description": {
    "es": "Pista de césped artificial de tamaño reglamentario.",
    "en": "Full-size artificial turf pitch."
  },
  "resourceType": "PITCH"
}
```

## Outputs

- **`201 Created`** — `ReservableResourceDto`.
- **`400 Bad Request`** — validation error.
- **`403 Forbidden`** — not admin or operator.
- **`404 Not Found`** — public space not found.

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
        participant CU as CreateReservableResourceUseCase
        participant PS as PublicSpaceStore
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PSR as PublicSpaceRepository
        participant RSR as ReservableResourceRepository
    end

    C->>GW: POST /api/leisure/public-spaces/{id}/resources {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RRC: POST /public-spaces/{id}/resources
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RRC->>CU: execute(publicSpaceId, sub, request, locale)
        CU->>PS: findActiveById(publicSpaceId)
        PS->>PSR: findByIdAndDeletedFalse(id)
        alt not found
            CU-->>C: 404 Not Found
        else found
            CU->>RR: create(publicSpaceId, request)
            RR->>RSR: save(ReservableResource)
            RSR-->>RR: ReservableResource
            RR-->>CU: ReservableResourceView
            CU->>TG: reservableResourceCreated (audit → leisure_trails)
            Note over CU: evicts reservableResources
            CU-->>C: 201 ReservableResourceDto
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
        participant CU as CreateReservableResourceUseCase
        participant PS as PublicSpaceStore
        participant RR as ReservableResourceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PSR as PublicSpaceRepository
        participant RSR as ReservableResourceRepository
    end

    C->>SEC: POST /api/leisure/public-spaces/{id}/resources {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RRC: POST /public-spaces/{id}/resources
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RRC->>CU: execute(publicSpaceId, sub, request, locale)
        CU->>PS: findActiveById(publicSpaceId)
        PS->>PSR: findByIdAndDeletedFalse(id) [single modelcity DB]
        alt not found
            CU-->>C: 404 Not Found
        else found
            CU->>RR: create(publicSpaceId, request)
            RR->>RSR: save(ReservableResource)
            RSR-->>RR: ReservableResource
            RR-->>CU: ReservableResourceView
            CU->>TG: reservableResourceCreated (audit → leisure_trails)
            Note over CU: evicts reservableResources
            CU-->>C: 201 ReservableResourceDto
        end
    end
```
