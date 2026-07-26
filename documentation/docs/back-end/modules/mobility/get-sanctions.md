---
title: List sanctions (management)
sidebar_label: List sanctions (management)
sidebar_position: 10
---

# List sanctions (management)

`GET /api/mobility/sanctions` → `GetSanctionsUseCase`

Lists sanctions for management. **Platform admin or mobility agent**. Returns
`SanctionSummaryDto` — a projection **without the evidence image** to avoid heavy payloads in
the list. Optional filters: `licensePlate` and a `from`/`to` window over `created_at`. Paginated
20 per page, ordered by `createdAt` descending. Not cached.

## Inputs

**`GET /api/mobility/sanctions?licensePlate=1234ABC&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<SanctionSummaryDto>` (without `imageBase64`).
- **`403 Forbidden`** — the requester is not an admin or mobility agent.

```json
{
  "content": [
    {
      "id": 7001,
      "licensePlate": "1234ABC",
      "latitude": 40.4168,
      "longitude": -3.7038,
      "agentSub": "auth0|agent-99",
      "createdAt": "2026-06-17T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / mobility agent)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant SC as SanctionController
        participant UC as GetSanctionsUseCase
        participant SS as SanctionStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>GW: GET /api/mobility/sanctions?licensePlate&from&to&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>SC: GET /sanctions (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        SC->>UC: execute(licensePlate, from, to, page)
        UC->>SS: search(licensePlate, from, to, PageRequest(page, 20, createdAt desc))
        SS->>SR: findAll(spec, pageable)
        SR-->>SS: Page<Sanction>
        SS-->>UC: Page<SanctionView>
        UC-->>C: 200 Page<SanctionSummaryDto>
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / mobility agent)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant SC as SanctionController
        participant UC as GetSanctionsUseCase
        participant SS as SanctionStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>SEC: GET /api/mobility/sanctions?licensePlate&from&to&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>SC: GET /sanctions (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        SC->>UC: execute(licensePlate, from, to, page)
        UC->>SS: search(licensePlate, from, to, PageRequest(page, 20, createdAt desc))
        SS->>SR: findAll(spec, pageable) [single modelcity DB]
        SR-->>SS: Page<Sanction>
        SS-->>UC: Page<SanctionView>
        UC-->>C: 200 Page<SanctionSummaryDto>
    end
```
