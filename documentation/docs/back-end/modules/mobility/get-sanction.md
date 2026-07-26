---
title: Sanction detail (management)
sidebar_label: Sanction detail (management)
sidebar_position: 11
---

# Sanction detail (management)

`GET /api/mobility/sanctions/{id}` → `GetSanctionUseCase`

Returns a sanction in full **with the evidence image** (`SanctionDto`). **Platform admin or
mobility agent**. If it does not exist, `404` (`ResourceNotFoundException`). Cached in
`sanction` keyed by the id.

## Inputs

**`GET /api/mobility/sanctions/{id}`** — no body.

## Outputs

- **`200 OK`** — `SanctionDto` (with image).
- **`403 Forbidden`** — the requester is not an admin or mobility agent.
- **`404 Not Found`** — no sanction with that id.

```json
{
  "id": 7001,
  "licensePlate": "1234ABC",
  "latitude": 40.4168,
  "longitude": -3.7038,
  "agentSub": "auth0|agent-99",
  "createdAt": "2026-06-17T10:30:00Z",
  "imageBase64": "iVBORw0KGgoAAAANSUhEUgAA..."
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
        participant UC as GetSanctionUseCase
        participant SS as SanctionStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>GW: GET /api/mobility/sanctions/{id} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>SC: GET /sanctions/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        SC->>UC: execute(id)
        Note over UC: if cached in sanction[id], returned without hitting the DB
        UC->>SS: findById(id)
        SS->>SR: findById(id)
        alt does not exist
            SS-->>C: 404 Not Found
        else exists
            SS-->>UC: SanctionView
            UC-->>C: 200 SanctionDto
        end
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
        participant UC as GetSanctionUseCase
        participant SS as SanctionStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>SEC: GET /api/mobility/sanctions/{id} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>SC: GET /sanctions/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        SC->>UC: execute(id)
        Note over UC: if cached in sanction[id], returned without hitting the DB
        UC->>SS: findById(id)
        SS->>SR: findById(id) [single modelcity DB]
        alt does not exist
            SS-->>C: 404 Not Found
        else exists
            SS-->>UC: SanctionView
            UC-->>C: 200 SanctionDto
        end
    end
```
