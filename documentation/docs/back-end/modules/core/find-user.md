---
title: Check citizen exists
sidebar_label: Find user
sidebar_position: 5
---

# Check citizen exists

`HEAD /api/core/users/{sub}` → `FindUserUseCase`

A **lightweight** existence check, so the app or front-end can tell whether an Auth0 `sub` is
already provisioned — e.g. to decide between the registration and the normal flow before
requesting an OTP. Being a `HEAD` request it returns **no body**, only the status code:
`200 OK` if the user exists, `404 Not Found` otherwise.

The result is cached in `citizenExists` keyed by `sub`; the entry is invalidated on sign-in and
delete.

## Inputs

**`HEAD /api/core/users/{sub}`** — no body.

## Outputs

- **`200 OK`** — the user exists (no body).
- **`404 Not Found`** — it does not exist (no body).

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
        participant FU as FindUserUseCase
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>GW: HEAD /api/core/users/{sub} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>UC: HEAD /users/{sub}
    UC->>FU: execute(sub)
    Note over FU: if cached in citizenExists[sub], returned without hitting the DB
    FU->>UR: existsById(sub)
    UR-->>FU: boolean
    alt exists
        FU-->>C: 200 OK
    else does not exist
        FU-->>C: 404 Not Found
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
        participant FU as FindUserUseCase
    end
    box rgb(224,247,224) DB · third parties
        participant UR as UserRepository
    end

    C->>SEC: HEAD /api/core/users/{sub} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>UC: HEAD /users/{sub}
    UC->>FU: execute(sub)
    Note over FU: if cached in citizenExists[sub], returned without hitting the DB
    FU->>UR: existsById(sub) [single modelcity DB]
    UR-->>FU: boolean
    alt exists
        FU-->>C: 200 OK
    else does not exist
        FU-->>C: 404 Not Found
    end
```
