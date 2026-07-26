---
title: My sanction detail
sidebar_label: My sanction detail
sidebar_position: 13
---

# My sanction detail

`GET /api/mobility/users/{userId}/sanctions/{id}` → `GetUserSanctionUseCase`

Returns a sanction in full **with the image** (`SanctionDto`) only if it belongs to one of the
caller's cars. Citizen endpoint: the `sub` must match `{userId}` (`403`). If the sanction does
not exist, `404`; if it exists but its plate is not among the user's car plates, `403`. Plate
comparison is case-insensitive (stored in uppercase). Cached in `sanction` keyed by the id
(same cache as the management detail).

## Inputs

**`GET /api/mobility/users/{userId}/sanctions/{id}`** — no body.

## Outputs

- **`200 OK`** — `SanctionDto` (with image).
- **`403 Forbidden`** — the sanction does not correspond to a car of the citizen.
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
    actor C as Client (citizen)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant SC as UserSanctionController
        participant UC as GetUserSanctionUseCase
        participant SS as SanctionStore
        participant CS as CarStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>GW: GET /api/mobility/users/{userId}/sanctions/{id} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>SC: GET /users/{userId}/sanctions/{id} (X-Auth-Sub=sub)
    SC->>UC: execute(userId, sub, sanctionId)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        Note over UC: if cached in sanction[sanctionId], returned without hitting the DB
        UC->>SS: findById(sanctionId)
        SS->>SR: findById(sanctionId)
        alt does not exist
            SS-->>C: 404 Not Found
        else exists
            UC->>CS: findByOwner(sub) → plates
            alt plate not owned by citizen
                UC-->>C: 403 Forbidden
            else owns the plate
                UC-->>C: 200 SanctionDto
            end
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (citizen)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant SC as UserSanctionController
        participant UC as GetUserSanctionUseCase
        participant SS as SanctionStore
        participant CS as CarStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>SEC: GET /api/mobility/users/{userId}/sanctions/{id} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>SC: GET /users/{userId}/sanctions/{id} (X-Auth-Sub=sub)
    SC->>UC: execute(userId, sub, sanctionId)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        Note over UC: if cached in sanction[sanctionId], returned without hitting the DB
        UC->>SS: findById(sanctionId)
        SS->>SR: findById(sanctionId) [single modelcity DB]
        alt does not exist
            SS-->>C: 404 Not Found
        else exists
            UC->>CS: findByOwner(sub) → plates
            alt plate not owned by citizen
                UC-->>C: 403 Forbidden
            else owns the plate
                UC-->>C: 200 SanctionDto
            end
        end
    end
```
