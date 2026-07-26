---
title: List citizen's reservations
sidebar_label: List citizen's reservations
sidebar_position: 5
---

# List citizen's reservations

`GET /api/mobility/users/{userId}/street-reservations` → `GetUserStreetReservationsUseCase`

Returns the citizen's reservations: the active ones plus the **last 30 days** of history,
paginated (10 per page by default). Citizen endpoint: the `sub` must match `{userId}` (`403`).
Each `StreetReservationDto`'s `active` flag is computed on the fly (`expiresAt > now`). Not
cached.

## Inputs

**`GET /api/mobility/users/{userId}/street-reservations?page=0&size=10`** — no body.

## Outputs

- **`200 OK`** — `Page<StreetReservationDto>`.
- **`403 Forbidden`** — the `sub` does not match `{userId}`.

```json
{
  "content": [
    {
      "id": 8801,
      "userSub": "auth0|abc",
      "carId": 501,
      "licensePlate": "1234ABC",
      "carNickname": "El coche de casa",
      "latitude": 40.4168,
      "longitude": -3.7038,
      "createdAt": "2026-06-17T10:30:00Z",
      "expiresAt": "2026-06-17T11:30:00Z",
      "renewedFromId": null,
      "active": true,
      "status": "PAID",
      "pricePaid": 1.35,
      "currency": "eur",
      "stripeCheckoutSessionId": "cs_test_a1b2c3d4e5"
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
        participant RC as UserStreetReservationController
        participant UC as GetUserStreetReservationsUseCase
        participant RS as StreetReservationStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    C->>GW: GET /api/mobility/users/{userId}/street-reservations?page&size + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: GET /users/{userId}/street-reservations (X-Auth-Sub=sub)
    RC->>UC: execute(userId, sub, pageable)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        UC->>RS: findUserHistory(userId, now-30d, pageable)
        RS->>RR: findByUserSubAndCreatedAtAfter(userId, now-30d, pageable)
        RR-->>RS: Page<StreetReservation>
        RS-->>UC: Page<StreetReservationView>
        UC-->>C: 200 Page<StreetReservationDto>
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
        participant RC as UserStreetReservationController
        participant UC as GetUserStreetReservationsUseCase
        participant RS as StreetReservationStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    C->>SEC: GET /api/mobility/users/{userId}/street-reservations?page&size + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: GET /users/{userId}/street-reservations (X-Auth-Sub=sub)
    RC->>UC: execute(userId, sub, pageable)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        UC->>RS: findUserHistory(userId, now-30d, pageable)
        RS->>RR: findByUserSubAndCreatedAtAfter(userId, now-30d, pageable) [single modelcity DB]
        RR-->>RS: Page<StreetReservation>
        RS-->>UC: Page<StreetReservationView>
        UC-->>C: 200 Page<StreetReservationDto>
    end
```
