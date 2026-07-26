---
title: Query the audit log
sidebar_label: Get system trails
sidebar_position: 14
---

# Query the audit log (mobility)

`GET /api/mobility/system-trails` → `GetSystemTrailsUseCase`

**Admin-only** (`PLATFORM_ADMIN`) read of the mobility vertical's audit log. Returns 20 events
per page ordered by `occurredAt` descending. Filters: `eventType`, `responsibleUserId`, `from`,
`to`, `page`.

Writes are automatic: `CreateCarUseCase`, `Create`/`RenewStreetReservationUseCase`,
`CreateSanctionUseCase` and `StripeWebhookController` call the `SystemTrailGenerator`
(`com.modelcity.mobility.trails`), which persists via `MobilitySystemTrailStore` into the
`mobility_trails` table. Webhook events record `responsibleUserId` as `SYSTEM`.

Vertical event types: `CAR_REGISTERED`, `STREET_RESERVATION_CREATED|RENEWED|CONFIRMED|CANCELLED`,
`SANCTION_ISSUED`.

## Inputs

**`GET /api/mobility/system-trails?eventType=SANCTION_ISSUED&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<SystemTrailDto>` (20 per page, `occurredAt` descending).
- **`403 Forbidden`** — the requester is not an admin.

```json
{
  "content": [
    {
      "eventId": "c2d3e4f5-1111-2222-3333-444455556666",
      "eventType": "SANCTION_ISSUED",
      "operationType": "CREATE",
      "occurredAt": "2026-06-17T10:30:00Z",
      "responsibleUserId": "auth0|agent-99",
      "responsibleUserRole": "MODEL-CITY-MOBILITY-AGENT",
      "resourceType": "sanction",
      "resourceId": "7001",
      "payload": { "licensePlate": "1234ABC" }
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
        participant ST as MobilitySystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant TR as MobilitySystemTrailRepository
    end

    FE->>GW: GET /api/mobility/system-trails?eventType&responsibleUserId&from&to&page + JWT
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
        TR-->>ST: Page<MobilityTrail>
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
        participant ST as MobilitySystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant TR as MobilitySystemTrailRepository
    end

    FE->>SEC: GET /api/mobility/system-trails?eventType&responsibleUserId&from&to&page + JWT
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
        TR-->>ST: Page<MobilityTrail>
        ST-->>UC: Page<SystemTrailView>
        UC-->>SC: Page<SystemTrailDto>
        SC-->>FE: 200 Page<SystemTrailDto>
    end
```
