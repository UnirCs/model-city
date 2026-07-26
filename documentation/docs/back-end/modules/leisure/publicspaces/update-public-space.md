---
title: Update public space
sidebar_label: Update public space
sidebar_position: 4
---

# Update public space

`PUT /api/leisure/public-spaces/{id}` → `UpdatePublicSpaceUseCase`

Fully replaces an active public space. **Admin only**. If not found or deleted, `404`.
Returns `200` with `PublicSpaceDto`.

Audits `PUBLIC_SPACE_UPDATED` and evicts `publicSpace` and `publicSpaces`.

## Inputs

**`PUT /api/leisure/public-spaces/{id}`** — same body as creation.

## Outputs

- **`200 OK`** — `PublicSpaceDto`.
- **`400 Bad Request`** — validation error.
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
        participant UU as UpdatePublicSpaceUseCase
        participant PS as PublicSpaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>GW: PUT /api/leisure/public-spaces/{id} {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PSC: PUT /public-spaces/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PSC->>UU: execute(id, sub, request, locale)
        UU->>PS: findActiveById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>PS: update(id, request)
            PS->>PR: save(PublicSpace)
            PR-->>PS: PublicSpace
            PS-->>UU: PublicSpaceView
            UU->>TG: publicSpaceUpdated (audit → leisure_trails)
            Note over UU: evicts publicSpace + publicSpaces
            UU-->>C: 200 PublicSpaceDto
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
        participant UU as UpdatePublicSpaceUseCase
        participant PS as PublicSpaceStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>SEC: PUT /api/leisure/public-spaces/{id} {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PSC: PUT /public-spaces/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        PSC->>UU: execute(id, sub, request, locale)
        UU->>PS: findActiveById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>PS: update(id, request)
            PS->>PR: save(PublicSpace) [single modelcity DB]
            PR-->>PS: PublicSpace
            PS-->>UU: PublicSpaceView
            UU->>TG: publicSpaceUpdated (audit → leisure_trails)
            Note over UU: evicts publicSpace + publicSpaces
            UU-->>C: 200 PublicSpaceDto
        end
    end
```
