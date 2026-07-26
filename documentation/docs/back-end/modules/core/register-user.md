---
title: Sign-in / JIT provisioning
sidebar_label: Register user
sidebar_position: 2
---

# Sign-in / JIT provisioning

`POST /api/core/users` → `RegisterUserUseCase`

Entry point when a citizen signs in from the app. The user record is **provisioned
just-in-time (JIT)**: the Auth0 `sub` arrives in the `X-Auth-Sub` header and the body supplies
`name`, `email` and `neighbourhoodName` (the neighbourhood's internal kebab-case name). If the
user does not exist yet it is created — its application role is resolved by querying Auth0,
falling back to `MODEL_CITY_CITIZEN` if the lookup fails or no role is assigned — and if it
already exists, its name, email and neighbourhood are refreshed. Returns `204 No Content`.

The neighbourhood is resolved by name against `NeighbourhoodRepository`; if it does not exist,
`404`. The operation is transactional and, on completion, records an audit event
(`USER_REGISTERED` or `USER_UPDATED`) through the `SystemTrailGenerator` and **invalidates** the
`citizenExists`, `userProfile` (key `#sub`) and `userList` caches.

## Inputs

**`POST /api/core/users`**

```json
{
  "name": "Lucía Fernández",
  "email": "lucia.fernandez@example.com",
  "neighbourhoodName": "el-recreo-norte"
}
```

## Outputs

- **`204 No Content`** — no body.
- **`404 Not Found`** — the neighbourhood does not exist (`ApiErrorResponse`).

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (app)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant UC as UsersController
        participant RU as RegisterUserUseCase
        participant A0F as Auth0ManagementFacade
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant NR as NeighbourhoodRepository
        participant A0 as Auth0 Mgmt API
        participant UR as UserRepository
    end

    C->>GW: POST /api/core/users {name,email,neighbourhoodName} + JWT
    Note over GW: validates JWT (Auth0 JWKS) and injects X-Auth-Sub
    GW->>UC: POST /users (X-Auth-Sub=sub)
    UC->>RU: execute(sub, request)
    RU->>NR: findByName(neighbourhoodName)
    alt neighbourhood not found
        NR-->>RU: empty
        RU-->>C: 404 Not Found (ResourceNotFound)
    else found
        NR-->>RU: Neighbourhood
        RU->>UR: findById(sub)
        alt new user
            UR-->>RU: empty
            RU->>A0F: getUserRoles(sub)
            A0F->>A0: GET user roles
            A0-->>A0F: roles
            A0F-->>RU: UserRole (fallback CITIZEN)
        else existing user
            UR-->>RU: User
        end
        RU->>UR: save(User with name/email/neighbourhood)
        RU->>TG: userRegistered / userUpdated (audit → core_trails)
        Note over RU: invalidates cache citizenExists, userProfile, userList
        RU-->>C: 204 No Content
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (app)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant UC as UsersController
        participant RU as RegisterUserUseCase
        participant A0F as Auth0ManagementFacade
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant NR as NeighbourhoodRepository
        participant A0 as Auth0 Mgmt API
        participant UR as UserRepository
    end

    C->>SEC: POST /api/core/users {name,email,neighbourhoodName} + JWT
    Note over SEC: validates JWT (Auth0 JWKS) and injects X-Auth-Sub
    SEC->>UC: POST /users (X-Auth-Sub=sub)
    UC->>RU: execute(sub, request)
    RU->>NR: findByName(neighbourhoodName)
    alt neighbourhood not found
        NR-->>RU: empty
        RU-->>C: 404 Not Found (ResourceNotFound)
    else found
        NR-->>RU: Neighbourhood
        RU->>UR: findById(sub)
        alt new user
            UR-->>RU: empty
            RU->>A0F: getUserRoles(sub)
            A0F->>A0: GET user roles
            A0-->>A0F: roles
            A0F-->>RU: UserRole (fallback CITIZEN)
        else existing user
            UR-->>RU: User
        end
        RU->>UR: save(User with name/email/neighbourhood) [single modelcity DB]
        RU->>TG: userRegistered / userUpdated (audit → core_trails)
        Note over RU: invalidates cache citizenExists, userProfile, userList
        RU-->>C: 204 No Content
    end
```
