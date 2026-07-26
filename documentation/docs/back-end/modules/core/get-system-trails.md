---
title: Query the audit log
sidebar_label: Get system trails
sidebar_position: 14
---

# Query the audit log (core)

`GET /api/core/system-trails` → `GetSystemTrailsUseCase`

**Admin-only** (`PLATFORM_ADMIN`) read of the core vertical's audit log (users + OTP). Returns
a page of 20 events ordered by `occurredAt` descending. Optional filters: `eventType`,
`responsibleUserId`, `from`, `to` (range on `occurredAt`) and `page`.

Events are **written** automatically: each write use case invokes the `SystemTrailGenerator`
(package `com.modelcity.core.trails`) after the operation and within the same transaction. The
generator builds the envelope (id, timestamp, `correlationId` from the MDC), serializes the
`payload` to JSON and persists it via `CoreSystemTrailStore` into the `core_trails` table. This
read queries that same store. See [Audit trails](../../architecture/audit-trails.md).

Vertical event types: `USER_REGISTERED`, `USER_UPDATED`, `USER_DELETED`, `USER_STATUS_CHANGED`,
`AGENT_INVITED`, `OPERATION_AUTHORIZATION_CREATED|VERIFIED|BURNT`.

## Inputs

**`GET /api/core/system-trails?eventType=USER_DELETED&responsibleUserId=&from=&to=&page=0`** — no
body.

## Outputs

- **`200 OK`** — `Page<SystemTrailDto>` (20 per page, `occurredAt` descending).
- **`403 Forbidden`** — the requester is not an admin.

```json
{
  "content": [
    {
      "eventId": "b1e2c3d4-0000-1111-2222-333344445555",
      "eventType": "USER_DELETED",
      "operationType": "DELETE",
      "occurredAt": "2026-06-17T10:30:00Z",
      "responsibleUserId": "auth0|admin",
      "responsibleUserRole": "MODEL-CITY-PLATFORM-ADMIN",
      "resourceType": "user",
      "resourceId": "auth0|6627f0a1c2d3e4f5a6b7c8d9",
      "payload": { "email": "lucia.fernandez@example.com" }
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
    actor FE as Front-end (admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant SC as SystemTrailController
        participant UC as GetSystemTrailsUseCase
        participant ST as CoreSystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant TR as CoreSystemTrailRepository
    end

    FE->>GW: GET /api/core/system-trails?eventType&responsibleUserId&from&to&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>SC: GET /system-trails (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>FE: 403 Forbidden
    else admin
        SC->>UC: execute(SystemTrailQuery, page)
        UC->>ST: search(query, pageable[size=20, occurredAt desc])
        ST->>TR: findAll(spec, pageable)
        TR-->>ST: Page<CoreTrail>
        ST-->>UC: Page<SystemTrailView>
        UC-->>SC: Page<SystemTrailDto>
        SC-->>FE: 200 Page<SystemTrailDto>
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor FE as Front-end (admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant SC as SystemTrailController
        participant UC as GetSystemTrailsUseCase
        participant ST as CoreSystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant TR as CoreSystemTrailRepository
    end

    FE->>SEC: GET /api/core/system-trails?eventType&responsibleUserId&from&to&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>SC: GET /system-trails (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>FE: 403 Forbidden
    else admin
        SC->>UC: execute(SystemTrailQuery, page)
        UC->>ST: search(query, pageable[size=20, occurredAt desc])
        ST->>TR: findAll(spec, pageable) [single modelcity DB]
        TR-->>ST: Page<CoreTrail>
        ST-->>UC: Page<SystemTrailView>
        UC-->>SC: Page<SystemTrailDto>
        SC-->>FE: 200 Page<SystemTrailDto>
    end
```
