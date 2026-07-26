---
title: Public space detail
sidebar_label: Public space detail
sidebar_position: 2
---

# Public space detail

`GET /api/leisure/public-spaces/{id}` → `GetPublicSpaceUseCase`
`GET /api/leisure/public-spaces/{id}?translations=full` → `GetPublicSpaceForEditUseCase`

Returns the full detail of an active public space. **Any authenticated user**. If not found or
soft-deleted, `404`.

`?translations=full` returns all locales of `name`, `description` and `address` (admin
editing) and is not cached.

Standard detail is cached in `publicSpace` keyed by `locale-id`.

## Inputs

**`GET /api/leisure/public-spaces/{id}`** — no body.

## Outputs

- **`200 OK`** — `PublicSpaceDto`.
- **`404 Not Found`** — public space not found.

```json
{
  "id": 20,
  "name": "Central Sports Centre",
  "description": "Multi-sports public centre in the city centre.",
  "address": "Avenida del Deporte 1",
  "latitude": 40.415,
  "longitude": -3.705,
  "photoUrls": ["https://cdn.modelcity.example/spaces/centro-deporte-1.jpg"]
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
        participant PSC as PublicSpaceController
        participant GU as GetPublicSpaceUseCase
        participant GF as GetPublicSpaceForEditUseCase
        participant PS as PublicSpaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>GW: GET /api/leisure/public-spaces/{id}?translations=full + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PSC: GET /public-spaces/{id}
    alt translations=full
        PSC->>GF: execute(id, locale)
        GF->>PS: findActiveById(id)
        PS->>PR: findByIdAndDeletedFalse(id)
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GF: PublicSpaceView
            GF-->>C: 200 PublicSpaceDto (all translations)
        end
    else standard
        PSC->>GU: execute(id, locale)
        Note over GU: cache publicSpace[locale-id] if present
        GU->>PS: findActiveById(id)
        PS->>PR: findByIdAndDeletedFalse(id)
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GU: PublicSpaceView
            GU-->>C: 200 PublicSpaceDto
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
        participant PSC as PublicSpaceController
        participant GU as GetPublicSpaceUseCase
        participant GF as GetPublicSpaceForEditUseCase
        participant PS as PublicSpaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>SEC: GET /api/leisure/public-spaces/{id}?translations=full + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PSC: GET /public-spaces/{id}
    alt translations=full
        PSC->>GF: execute(id, locale)
        GF->>PS: findActiveById(id)
        PS->>PR: findByIdAndDeletedFalse(id) [single modelcity DB]
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GF: PublicSpaceView
            GF-->>C: 200 PublicSpaceDto (all translations)
        end
    else standard
        PSC->>GU: execute(id, locale)
        Note over GU: cache publicSpace[locale-id] if present
        GU->>PS: findActiveById(id)
        PS->>PR: findByIdAndDeletedFalse(id)
        alt not found
            PS-->>C: 404 Not Found
        else found
            PS-->>GU: PublicSpaceView
            GU-->>C: 200 PublicSpaceDto
        end
    end
```
