---
title: List citizen's cars
sidebar_label: List citizen's cars
sidebar_position: 3
---

# List citizen's cars

`GET /api/mobility/users/{userId}/cars` → `GetUserCarsUseCase`

Lists a citizen's cars, paginated (5 per page by default). Citizen endpoint: the `sub` must
match `{userId}` (`403` otherwise). Returns a `Page` of `CarDto`. Cached in `userCars` keyed by
`userId:page:size`.

## Inputs

**`GET /api/mobility/users/{userId}/cars?page=0&size=5`** — no body.

## Outputs

- **`200 OK`** — `Page<CarDto>`.
- **`403 Forbidden`** — the `sub` does not match `{userId}`.

```json
{
  "content": [
    {
      "id": 501,
      "ownerSub": "auth0|abc",
      "licensePlate": "1234ABC",
      "nickname": "El coche de casa",
      "brand": "Seat",
      "model": "León",
      "createdAt": "2026-06-17T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 5,
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
        participant CT as UserCarController
        participant UC as GetUserCarsUseCase
        participant CS as CarStore
    end
    box rgb(224,247,224) DB · third parties
        participant CR as CarRepository
    end

    C->>GW: GET /api/mobility/users/{userId}/cars?page&size + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>CT: GET /users/{userId}/cars (X-Auth-Sub=sub)
    CT->>UC: execute(userId, sub, pageable)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        Note over UC: if cached in userCars[userId:page:size], returned without hitting the DB
        UC->>CS: findByOwner(userId, pageable)
        CS->>CR: findByOwnerSub(userId, pageable)
        CR-->>CS: Page<Car>
        CS-->>UC: Page<CarView>
        UC-->>C: 200 Page<CarDto>
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
        participant CT as UserCarController
        participant UC as GetUserCarsUseCase
        participant CS as CarStore
    end
    box rgb(224,247,224) DB · third parties
        participant CR as CarRepository
    end

    C->>SEC: GET /api/mobility/users/{userId}/cars?page&size + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>CT: GET /users/{userId}/cars (X-Auth-Sub=sub)
    CT->>UC: execute(userId, sub, pageable)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        Note over UC: if cached in userCars[userId:page:size], returned without hitting the DB
        UC->>CS: findByOwner(userId, pageable)
        CS->>CR: findByOwnerSub(userId, pageable) [single modelcity DB]
        CR-->>CS: Page<Car>
        CS-->>UC: Page<CarView>
        UC-->>C: 200 Page<CarDto>
    end
```
