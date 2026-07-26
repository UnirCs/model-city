---
title: Delete user
sidebar_label: Delete user
sidebar_position: 7
---

# Delete user

`DELETE /api/core/users/{userId}` → `DeleteUserUseCase`

Permanently removes a user record. **Admin only** (`PLATFORM_ADMIN`, verified by
`ModelCityAccessAspect` through `CoreClient`). As a safeguard, **admin users cannot be
deleted**: if the target is a `PLATFORM_ADMIN`, `403`. If the target does not exist, `404`. On
success, `204 No Content`.

After deleting it records a `USER_DELETED` audit event and invalidates the `userProfile`,
`citizenExists` and `userList` caches.

:::note[Topology difference]

In the **monolith**, the single database with **real foreign keys** cascades the delete
(`ON DELETE CASCADE`) to the citizen's personal data in other verticals (cars, reservations,
tickets, votes) and anonymizes (`ON DELETE SET NULL`) their audit trails. In **microservices**,
each vertical is in its own database, so that propagation does not happen at the database level
(the references are soft). See [Data model](../../architecture/data-model.md).

:::

## Inputs

**`DELETE /api/core/users/{userId}`** — no body.

## Outputs

- **`204 No Content`** — user deleted (no body).
- **`403 Forbidden`** — the target is an admin.
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
        participant DU as DeleteUserUseCase
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>GW: DELETE /api/core/users/{userId} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>UC: DELETE /users/{userId} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not PLATFORM_ADMIN
        ASP-->>C: 403 Forbidden
    else admin
        UC->>DU: execute(sub, userId)
        DU->>UR: findById(userId)
        alt target does not exist
            UR-->>C: 404 Not Found
        else target is an admin
            DU-->>C: 403 Forbidden (an admin cannot be deleted)
        else deletable
            DU->>UR: delete(target)
            DU->>TG: userDeleted (audit → core_trails)
            Note over DU: invalidates cache userProfile, citizenExists, userList
            DU-->>C: 204 No Content
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
        participant DU as DeleteUserUseCase
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>SEC: DELETE /api/core/users/{userId} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>UC: DELETE /users/{userId} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not PLATFORM_ADMIN
        ASP-->>C: 403 Forbidden
    else admin
        UC->>DU: execute(sub, userId)
        DU->>UR: findById(userId)
        alt target does not exist
            UR-->>C: 404 Not Found
        else target is an admin
            DU-->>C: 403 Forbidden (an admin cannot be deleted)
        else deletable
            DU->>UR: delete(target) [cascade via real FKs in modelcity]
            DU->>TG: userDeleted (audit → core_trails)
            Note over DU: invalidates cache userProfile, citizenExists, userList
            DU-->>C: 204 No Content
        end
    end
```
