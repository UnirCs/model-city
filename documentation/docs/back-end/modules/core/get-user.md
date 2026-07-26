---
title: Get user profile
sidebar_label: Get user
sidebar_position: 3
---

# Get user profile

`GET /api/core/users/{userId}` → `GetUserUseCase`

Returns a user's full profile (personal data, neighbourhood and role). Access control is
**ownership-based**: a citizen can only read **their own** profile (`userId == sub`); a
`PLATFORM_ADMIN` can read anyone's. A non-admin requesting another profile gets `403`; if the
requester does not exist, `401`; if the target does not exist, `404`.

The result is cached in `userProfile` keyed by `userId`, so repeated reads of the same profile
are served from Valkey without hitting the database. Writes to the user (sign-in, delete)
invalidate this entry.

## Inputs

**`GET /api/core/users/{userId}`** — no body.

## Outputs

- **`200 OK`** — `UserProfileDto`.
- **`401 Unauthorized`** — the requester does not exist.
- **`403 Forbidden`** — a citizen requests another user's profile.
- **`404 Not Found`** — the target user does not exist.

```json
{
  "id": "auth0|6627f0a1c2d3e4f5a6b7c8d9",
  "name": "Lucía Fernández",
  "email": "lucia.fernandez@example.com",
  "createdAt": "2026-05-02T08:14:33Z",
  "neighbourhood": { "id": 7, "displayName": "El Recreo Norte", "name": "el-recreo-norte" },
  "role": "MODEL-CITY-CITIZEN"
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
        participant UC as UsersController
        participant GU as GetUserUseCase
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>GW: GET /api/core/users/{userId} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>UC: GET /users/{userId} (X-Auth-Sub=sub)
    UC->>GU: execute(sub, userId)
    Note over GU: if cached in userProfile[userId], returned without hitting the DB
    alt sub != userId (requests another profile)
        GU->>UR: findById(sub)  (requester)
        alt requester does not exist
            UR-->>C: 401 Unauthorized
        else not an admin
            GU-->>C: 403 Forbidden
        end
    end
    GU->>UR: findById(userId)
    alt target does not exist
        UR-->>C: 404 Not Found
    else found
        UR-->>GU: User (+ Neighbourhood)
        GU-->>C: 200 UserProfileDto
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
        participant UC as UsersController
        participant GU as GetUserUseCase
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>SEC: GET /api/core/users/{userId} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>UC: GET /users/{userId} (X-Auth-Sub=sub)
    UC->>GU: execute(sub, userId)
    Note over GU: if cached in userProfile[userId], returned without hitting the DB
    alt sub != userId (requests another profile)
        GU->>UR: findById(sub)  (requester)
        alt requester does not exist
            UR-->>C: 401 Unauthorized
        else not an admin
            GU-->>C: 403 Forbidden
        end
    end
    GU->>UR: findById(userId) [single modelcity DB]
    alt target does not exist
        UR-->>C: 404 Not Found
    else found
        UR-->>GU: User (+ Neighbourhood)
        GU-->>C: 200 UserProfileDto
    end
```
