---
title: Enable / disable user
sidebar_label: Set user status
sidebar_position: 6
---

# Enable / disable user

`PATCH /api/core/users/{userId}` → `SetUserStatusUseCase`

Changes a user account's status (`ACTIVE` / `DISABLED`). **Admin only** (`PLATFORM_ADMIN`,
verified by `ModelCityAccessAspect` through `CoreClient`). As a safeguard, **platform admins
cannot be disabled**: if the target is a `PLATFORM_ADMIN`, `403`. If the target does not exist,
`404`. On success, `204 No Content`.

After the change it records a `USER_STATUS_CHANGED` audit event (with the previous and new
status) and invalidates the `userProfile` and `userList` caches.

## Inputs

**`PATCH /api/core/users/{userId}`**

```json
{ "status": "DISABLED" }
```

## Outputs

- **`204 No Content`** — status changed (no body).
- **`403 Forbidden`** — the target is a platform admin.
- **`404 Not Found`** — the target does not exist.

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
        participant SU as SetUserStatusUseCase
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>GW: PATCH /api/core/users/{userId} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>UC: PATCH /users/{userId} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not PLATFORM_ADMIN
        ASP-->>C: 403 Forbidden
    else admin
        UC->>SU: execute(sub, userId, status)
        SU->>UR: findById(userId)
        alt target does not exist
            UR-->>C: 404 Not Found
        else target is an admin
            SU-->>C: 403 Forbidden (an admin cannot be disabled)
        else modifiable
            SU->>UR: save(target with new status)
            SU->>TG: userStatusChanged (audit → core_trails, previous & new status)
            Note over SU: invalidates cache userProfile, userList
            SU-->>C: 204 No Content
        end
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
        participant SU as SetUserStatusUseCase
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>SEC: PATCH /api/core/users/{userId} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>UC: PATCH /users/{userId} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not PLATFORM_ADMIN
        ASP-->>C: 403 Forbidden
    else admin
        UC->>SU: execute(sub, userId, status)
        SU->>UR: findById(userId)
        alt target does not exist
            UR-->>C: 404 Not Found
        else target is an admin
            SU-->>C: 403 Forbidden (an admin cannot be disabled)
        else modifiable
            SU->>UR: save(target with new status) [single modelcity DB]
            SU->>TG: userStatusChanged (audit → core_trails, previous & new status)
            Note over SU: invalidates cache userProfile, userList
            SU-->>C: 204 No Content
        end
    end
```
