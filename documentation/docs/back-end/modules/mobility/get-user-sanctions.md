---
title: List my sanctions
sidebar_label: List my sanctions
sidebar_position: 12
---

# List my sanctions

`GET /api/mobility/users/{userId}/sanctions` → `GetUserSanctionsUseCase`

Lists the sanctions that affect the caller's own cars, **without the evidence image**
(`SanctionSummaryDto`), paginated (10 per page by default). Citizen endpoint: the `sub` must
match `{userId}` (`403`). The use case first fetches the plates of the user's cars; if there
are none it returns an empty page; otherwise it looks up sanctions for those plates ordered by
`createdAt` descending. Cached in `userSanctions` keyed by `userId:page:size`; invalidated when
a new sanction is issued.

## Inputs

**`GET /api/mobility/users/{userId}/sanctions?page=0&size=10`** — no body.

## Outputs

- **`200 OK`** — `Page<SanctionSummaryDto>` (without `imageBase64`); empty page if the user has
  no cars.
- **`403 Forbidden`** — the `sub` does not match `{userId}`.

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
  "size": 10,
  "first": true,
  "last": true
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
        participant UC as GetUserSanctionsUseCase
        participant CS as CarStore
        participant SS as SanctionStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>GW: GET /api/mobility/users/{userId}/sanctions?page&size + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>SC: GET /users/{userId}/sanctions (X-Auth-Sub=sub)
    SC->>UC: execute(userId, sub, pageable)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        Note over UC: if cached in userSanctions[userId:page:size], returned without hitting the DB
        UC->>CS: findByOwner(sub) → plates
        alt no cars
            UC-->>C: 200 empty Page
        else has cars
            UC->>SS: findByPlatesIn(plates, PageRequest(page, size, createdAt desc))
            SS->>SR: findByLicensePlateIn(plates, pageable)
            SR-->>SS: Page<Sanction>
            SS-->>UC: Page<SanctionView>
            UC-->>C: 200 Page<SanctionSummaryDto>
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
        participant UC as GetUserSanctionsUseCase
        participant CS as CarStore
        participant SS as SanctionStore
    end
    box rgb(224,247,224) DB · third parties
        participant SR as SanctionRepository
    end

    C->>SEC: GET /api/mobility/users/{userId}/sanctions?page&size + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>SC: GET /users/{userId}/sanctions (X-Auth-Sub=sub)
    SC->>UC: execute(userId, sub, pageable)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        Note over UC: if cached in userSanctions[userId:page:size], returned without hitting the DB
        UC->>CS: findByOwner(sub) → plates
        alt no cars
            UC-->>C: 200 empty Page
        else has cars
            UC->>SS: findByPlatesIn(plates, PageRequest(page, size, createdAt desc))
            SS->>SR: findByLicensePlateIn(plates, pageable) [single modelcity DB]
            SR-->>SS: Page<Sanction>
            SS-->>UC: Page<SanctionView>
            UC-->>C: 200 Page<SanctionSummaryDto>
        end
    end
```
