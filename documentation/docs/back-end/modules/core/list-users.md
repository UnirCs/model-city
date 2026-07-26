---
title: List users
sidebar_label: List users
sidebar_position: 4
---

# List users

`GET /api/core/users` → `ListUsersUseCase`

Lists users paginated (20 per page) for the backoffice. **Admin only**: method-level
authorization requires `PLATFORM_ADMIN`, which the `ModelCityAccessAspect` resolves through
`CoreClient` before the method runs; a non-admin gets `403`. Four optional, combinable filters
(via `UserSpecs`): `citizen` (`true` → citizens only, `false` → staff only, absent → all),
`name` (case-insensitive substring), `neighbourhoodId` and `role`.

The result is cached in `userList` keyed by the four filters plus pagination. Returns a `Page`
of `UserSummaryDto` (a lightweight projection with status and neighbourhood).

## Inputs

**`GET /api/core/users?citizen=false&name=&neighbourhoodId=&role=&page=0`** — no body.

| Query param | Type | Notes |
| --- | --- | --- |
| `citizen` | boolean | `true` citizens · `false` staff · omit → all |
| `name` | string | case-insensitive substring on the name |
| `neighbourhoodId` | long | filter citizens by neighbourhood |
| `role` | string | filter staff by role (dash format, e.g. `MODEL-CITY-OPERATOR`) |
| `page` | int | 0-based; page size 20 |

## Outputs

- **`200 OK`** — `Page<UserSummaryDto>`.
- **`403 Forbidden`** — the requester is not an admin.

```json
{
  "content": [
    {
      "id": "auth0|aaa111",
      "name": "Marta Ruiz",
      "email": "marta.ruiz@ayto.example.com",
      "role": "MODEL-CITY-BACKOFFICE",
      "status": "ACTIVE",
      "neighbourhoodId": null,
      "neighbourhoodName": null,
      "createdAt": "2026-03-10T09:00:00Z"
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
    actor C as Client
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant UC as UsersController
        participant LU as ListUsersUseCase
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>GW: GET /api/core/users?citizen=&name=&neighbourhoodId=&role= + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>UC: GET /users (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not PLATFORM_ADMIN
        ASP-->>C: 403 Forbidden
    else admin
        UC->>LU: execute(sub, citizen, name, neighbourhoodId, role, pageable)
        Note over LU: if cached in userList[filters:page:size], returned without hitting the DB
        alt no filters
            LU->>UR: findAll(pageable)
        else with filters
            LU->>UR: findAll(spec(UserSpecs), pageable)
        end
        UR-->>LU: Page<User>
        LU-->>C: 200 Page<UserSummaryDto>
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
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant UC as UsersController
        participant LU as ListUsersUseCase
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>SEC: GET /api/core/users?citizen=&name=&neighbourhoodId=&role= + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>UC: GET /users (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not PLATFORM_ADMIN
        ASP-->>C: 403 Forbidden
    else admin
        UC->>LU: execute(sub, citizen, name, neighbourhoodId, role, pageable)
        Note over LU: if cached in userList[filters:page:size], returned without hitting the DB
        alt no filters
            LU->>UR: findAll(pageable)
        else with filters
            LU->>UR: findAll(spec(UserSpecs), pageable)
        end
        UR-->>LU: Page<User> [single modelcity DB]
        LU-->>C: 200 Page<UserSummaryDto>
    end
```
