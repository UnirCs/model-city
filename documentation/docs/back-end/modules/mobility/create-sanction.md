---
title: Issue sanction
sidebar_label: Issue sanction
sidebar_position: 9
---

# Issue sanction

`POST /api/mobility/sanctions` → `CreateSanctionUseCase`

Records a new parking sanction issued by an agent. **Platform admin or mobility agent** (role
verified by `ModelCityAccessAspect` through `CoreClient`). The body carries the license plate,
coordinates and the evidence **image in base64** (`imageBase64`, mandatory). The `agentSub` is
taken from `X-Auth-Sub` and stored as the author. Returns `201` with the created sanction
(including the image). Records `SANCTION_ISSUED` and fully invalidates the `userSanctions`
cache.

In the monolith, `agent_sub` is a real foreign key to `users`.

## Inputs

**`POST /api/mobility/sanctions`**

```json
{
  "licensePlate": "1234ABC",
  "latitude": 40.4168,
  "longitude": -3.7038,
  "imageBase64": "iVBORw0KGgoAAAANSUhEUgAA..."
}
```

## Outputs

- **`201 Created`** — `SanctionDto` (includes the evidence image).
- **`403 Forbidden`** — the requester is not an admin or mobility agent.

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
    actor C as Client (agent)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant SC as SanctionController
        participant UC as CreateSanctionUseCase
        participant SS as SanctionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>GW: POST /api/mobility/sanctions {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>SC: POST /sanctions (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        SC->>UC: execute(agentSub, request)
        UC->>SS: create(agentSub, request)
        SS->>SR: save(Sanction)
        SS-->>UC: SanctionView
        UC->>TG: sanctionIssued (audit → mobility_trails)
        Note over UC: invalidates cache userSanctions
        UC-->>C: 201 SanctionDto
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (agent)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant SC as SanctionController
        participant UC as CreateSanctionUseCase
        participant SS as SanctionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>SEC: POST /api/mobility/sanctions {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>SC: POST /sanctions (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        SC->>UC: execute(agentSub, request)
        UC->>SS: create(agentSub, request)
        SS->>SR: save(Sanction) [single modelcity DB, FK agent_sub → users]
        SS-->>UC: SanctionView
        UC->>TG: sanctionIssued (audit → mobility_trails)
        Note over UC: invalidates cache userSanctions
        UC-->>C: 201 SanctionDto
    end
```
