---
title: Register car
sidebar_label: Register car
sidebar_position: 2
---

# Register car

`POST /api/mobility/users/{userId}/cars` → `CreateCarUseCase`

Registers a new car for a citizen. Citizen endpoint: the token `sub` must match the path
`{userId}` (`403` otherwise). The license plate is normalized (`trim` + uppercase) and must be
unique system-wide; if it already exists, `409`. Returns `201` with the created car. Records the
`CAR_REGISTERED` event and invalidates the first page of the user's `userCars` cache
(`#userId:0:5`).

Since this is a citizen endpoint, `mobility` does **not** call `core`: the ownership check is
done in the use case by comparing `sub` with `{userId}`.

## Inputs

**`POST /api/mobility/users/{userId}/cars`**

```json
{
  "licensePlate": "1234 ABC",
  "nickname": "El coche de casa",
  "brand": "Seat",
  "model": "León"
}
```

## Outputs

- **`201 Created`** — `CarDto` (plate normalized to uppercase).
- **`403 Forbidden`** — the `sub` does not match `{userId}`.
- **`409 Conflict`** — the license plate is already registered.

```json
{
  "id": 501,
  "ownerSub": "auth0|abc",
  "licensePlate": "1234ABC",
  "nickname": "El coche de casa",
  "brand": "Seat",
  "model": "León",
  "createdAt": "2026-06-17T10:30:00Z"
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
        participant UC as CreateCarUseCase
        participant CS as CarStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant CR as CarRepository
    end

    C->>GW: POST /api/mobility/users/{userId}/cars {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>CT: POST /users/{userId}/cars (X-Auth-Sub=sub)
    CT->>UC: execute(userId, sub, request)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        UC->>CS: existsByLicensePlate(plate)
        CS->>CR: existsByLicensePlate(plate)
        alt plate already registered
            CS-->>C: 409 Conflict
        else free
            UC->>CS: create(sub, plate, request)
            CS->>CR: save(Car)
            CS-->>UC: CarView
            UC->>TG: carRegistered (audit → mobility_trails)
            Note over UC: invalidates cache userCars[userId:0:5]
            UC-->>C: 201 CarDto
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
        participant CT as UserCarController
        participant UC as CreateCarUseCase
        participant CS as CarStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant CR as CarRepository
    end

    C->>SEC: POST /api/mobility/users/{userId}/cars {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>CT: POST /users/{userId}/cars (X-Auth-Sub=sub)
    CT->>UC: execute(userId, sub, request)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        UC->>CS: existsByLicensePlate(plate)
        CS->>CR: existsByLicensePlate(plate)
        alt plate already registered
            CS-->>C: 409 Conflict
        else free
            UC->>CS: create(sub, plate, request)
            CS->>CR: save(Car) [single modelcity DB, FK owner_sub → users]
            CS-->>UC: CarView
            UC->>TG: carRegistered (audit → mobility_trails)
            Note over UC: invalidates cache userCars[userId:0:5]
            UC-->>C: 201 CarDto
        end
    end
```
