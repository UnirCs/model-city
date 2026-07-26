---
title: List reservable resources
sidebar_label: List reservable resources
sidebar_position: 6
---

# List reservable resources

`GET /api/leisure/public-spaces/{publicSpaceId}/resources` → `GetReservableResourcesUseCase`
`GET /api/leisure/public-spaces/{publicSpaceId}/resources?translations=full` → `GetReservableResourcesForEditUseCase`

Returns paginated active reservable resources of a public space. **Any authenticated user**.
Default page size is **4**. If `translations=full` is passed, every locale of `name` and
`description` is returned (admin editing).

Cached in `reservableResources` keyed by
`locale-{publicSpaceId}:{page}:{size}`.

If the public space is not found, `404`.

## Inputs

**`GET /api/leisure/public-spaces/{publicSpaceId}/resources?page=0&size=4`** — no body.

## Outputs

- **`200 OK`** — `Page<ReservableResourceDto>`.
- **`404 Not Found`** — public space not found.

```json
{
  "content": [
    {
      "id": 200,
      "publicSpaceId": 20,
      "name": "Main football pitch",
      "description": "Full-size artificial turf pitch.",
      "resourceType": "PITCH"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 4
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
        participant RRC as ReservableResourceController
        participant GU as GetReservableResourcesUseCase
        participant GF as GetReservableResourcesForEditUseCase
        participant PS as PublicSpaceStore
        participant RR as ReservableResourceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PSR as PublicSpaceRepository
        participant RSR as ReservableResourceRepository
    end

    C->>GW: GET /api/leisure/public-spaces/{id}/resources?translations=full&page=0 + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RRC: GET /public-spaces/{id}/resources
    RRC->>PS: findActiveById(publicSpaceId)
    PS->>PSR: findByIdAndDeletedFalse(id)
    alt not found
        PS-->>C: 404 Not Found
    else found
        alt translations=full
            RRC->>GF: execute(publicSpaceId, pageable, locale)
            GF->>RR: findActiveByPublicSpace(id, pageable)
            RR->>RSR: findByPublicSpaceIdAndDeletedFalse(id, pageable)
            RR-->>GF: Page<ReservableResourceView>
            GF-->>C: 200 Page<ReservableResourceDto> (all translations)
        else standard
            RRC->>GU: execute(publicSpaceId, pageable, locale)
            Note over GU: cache reservableResources[locale-id:page:size] if present
            GU->>RR: findActiveByPublicSpace(id, pageable)
            RR->>RSR: findByPublicSpaceIdAndDeletedFalse(id, pageable)
            RR-->>GU: Page<ReservableResourceView>
            GU-->>C: 200 Page<ReservableResourceDto>
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
        participant RRC as ReservableResourceController
        participant GU as GetReservableResourcesUseCase
        participant GF as GetReservableResourcesForEditUseCase
        participant PS as PublicSpaceStore
        participant RR as ReservableResourceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PSR as PublicSpaceRepository
        participant RSR as ReservableResourceRepository
    end

    C->>SEC: GET /api/leisure/public-spaces/{id}/resources?translations=full&page=0 + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RRC: GET /public-spaces/{id}/resources
    RRC->>PS: findActiveById(publicSpaceId)
    PS->>PSR: findByIdAndDeletedFalse(id) [single modelcity DB]
    alt not found
        PS-->>C: 404 Not Found
    else found
        alt translations=full
            RRC->>GF: execute(publicSpaceId, pageable, locale)
            GF->>RR: findActiveByPublicSpace(id, pageable)
            RR->>RSR: findByPublicSpaceIdAndDeletedFalse(id, pageable)
            RR-->>GF: Page<ReservableResourceView>
            GF-->>C: 200 Page<ReservableResourceDto> (all translations)
        else standard
            RRC->>GU: execute(publicSpaceId, pageable, locale)
            Note over GU: cache reservableResources[locale-id:page:size] if present
            GU->>RR: findActiveByPublicSpace(id, pageable)
            RR->>RSR: findByPublicSpaceIdAndDeletedFalse(id, pageable)
            RR-->>GU: Page<ReservableResourceView>
            GU-->>C: 200 Page<ReservableResourceDto>
        end
    end
```
