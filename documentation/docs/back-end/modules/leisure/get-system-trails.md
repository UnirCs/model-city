---
title: Query system trails
sidebar_label: Query system trails
sidebar_position: 11
---

# Query system trails

`GET /api/leisure/system-trails` → `SystemTrailController`

Admin-only query over the leisure vertical audit log. **Admin only**. Supports filters by
`eventType`, `responsibleUserId`, `from` and `to` (ISO-8601 `OffsetDateTime`), and `page`.

All leisure operations that mutate state generate a trail via
`com.modelcity.leisure.trails.SystemTrailGenerator`.

## Inputs

**`GET /api/leisure/system-trails?eventType=CITY_PLACE_CREATED&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<SystemTrailDto>`.
- **`403 Forbidden`** — not admin.

```json
{
  "content": [
    {
      "id": 10000,
      "eventType": "CITY_PLACE_CREATED",
      "operationType": "CREATE",
      "responsibleUserId": "auth0|admin01",
      "responsibleUserRole": "MODEL-CITY-PLATFORM-ADMIN",
      "resourceType": "CITY_PLACE",
      "resourceId": "10",
      "payload": {
        "placeId": 10,
        "name": "Reina Sofía Museum",
        "latitude": 40.4087,
        "longitude": -3.6945,
        "category": "MUSEUM"
      },
      "createdAt": "2026-07-24T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

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
        participant STC as SystemTrailController
        participant GU as GetSystemTrailsUseCase
        participant STS as LeisureSystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant STR as LeisureSystemTrailRepository
    end

    C->>GW: GET /api/leisure/system-trails?eventType&from&to&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>STC: GET /system-trails (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        STC->>GU: execute(SystemTrailQuery, page)
        GU->>STS: findBy(query, pageable)
        STS->>STR: findAll(spec, pageable)
        STR-->>STS: Page<LeisureSystemTrail>
        STS-->>GU: Page<SystemTrailDto>
        GU-->>C: 200 Page<SystemTrailDto>
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
        participant STC as SystemTrailController
        participant GU as GetSystemTrailsUseCase
        participant STS as LeisureSystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant STR as LeisureSystemTrailRepository
    end

    C->>SEC: GET /api/leisure/system-trails?eventType&from&to&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>STC: GET /system-trails (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        STC->>GU: execute(SystemTrailQuery, page)
        GU->>STS: findBy(query, pageable)
        STS->>STR: findAll(spec, pageable) [single modelcity DB]
        STR-->>STS: Page<LeisureSystemTrail>
        STS-->>GU: Page<SystemTrailDto>
        GU-->>C: 200 Page<SystemTrailDto>
    end
```
