---
title: List public spaces
sidebar_label: List public spaces
sidebar_position: 1
---

# List public spaces

`GET /api/leisure/public-spaces` → `GetPublicSpacesUseCase`

Returns paginated active public spaces. **Any authenticated user**. Page size is **6**,
sorted by `id` ascending. Cached in `publicSpaces` keyed by `locale-page`.

## Inputs

**`GET /api/leisure/public-spaces?page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<PublicSpaceSummaryDto>`.

```json
{
  "content": [
    {
      "id": 20,
      "name": "Central Sports Centre",
      "address": "Avenida del Deporte 1",
      "photoUrl": "https://cdn.modelcity.example/spaces/centro-deporte-1.jpg"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 6
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
        participant GU as GetPublicSpacesUseCase
        participant PS as PublicSpaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>GW: GET /api/leisure/public-spaces?page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PSC: GET /public-spaces
    PSC->>GU: execute(page, locale)
    Note over GU: cache publicSpaces[locale-page] if present
    GU->>PS: findActive(PageRequest(0,6,id asc))
    PS->>PR: findByDeletedFalse(pageable)
    PR-->>PS: Page<PublicSpace>
    PS-->>GU: Page<PublicSpaceView>
    GU-->>C: 200 Page<PublicSpaceSummaryDto>
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
        participant GU as GetPublicSpacesUseCase
        participant PS as PublicSpaceStore
    end
    box rgb(224,247,224) DB · third parties
        participant PR as PublicSpaceRepository
    end

    C->>SEC: GET /api/leisure/public-spaces?page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PSC: GET /public-spaces
    PSC->>GU: execute(page, locale)
    Note over GU: cache publicSpaces[locale-page] if present
    GU->>PS: findActive(PageRequest(0,6,id asc))
    PS->>PR: findByDeletedFalse(pageable) [single modelcity DB]
    PR-->>PS: Page<PublicSpace>
    PS-->>GU: Page<PublicSpaceView>
    GU-->>C: 200 Page<PublicSpaceSummaryDto>
```
