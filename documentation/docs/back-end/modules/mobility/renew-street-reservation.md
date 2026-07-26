---
title: Renew SER-zone reservation
sidebar_label: Renew street reservation
sidebar_position: 6
---

# Renew SER-zone reservation

`POST /api/mobility/users/{userId}/street-reservations/{reservationId}/renewals` →
`RenewStreetReservationUseCase`

Renews an active reservation by **creating a new one** linked to the original
(`renewedFromId`), rather than modifying the existing one. Citizen endpoint: the `sub` must
match `{userId}` (`403`). The original reservation is validated to exist (`404`), belong to the
citizen (`403`) and still be **active** (`expiresAt > now`); if it already expired, `422`. The
body's car is also checked to exist (`404`) and be theirs (`403`). The new reservation starts at
`now` with the new duration and its own `checkoutSessionId`/`price`. Returns `201`. Records
`STREET_RESERVATION_RENEWED`.

## Inputs

**`POST /api/mobility/users/{userId}/street-reservations/{reservationId}/renewals`**

```json
{
  "carId": 501,
  "latitude": 40.4168,
  "longitude": -3.7038,
  "durationMinutes": 30,
  "checkoutSessionId": "cs_test_f6g7h8i9",
  "price": 0.70
}
```

## Outputs

- **`201 Created`** — a new `StreetReservationDto` with `renewedFromId` pointing to the original.
- **`403 Forbidden`** — the reservation/car belongs to another user, or `sub` != `{userId}`.
- **`404 Not Found`** — the reservation or car does not exist.
- **`422 Unprocessable Content`** — the original reservation is no longer active.

```json
{
  "id": 8802,
  "userSub": "auth0|abc",
  "carId": 501,
  "licensePlate": "1234ABC",
  "carNickname": "El coche de casa",
  "latitude": 40.4168,
  "longitude": -3.7038,
  "createdAt": "2026-06-17T11:25:00Z",
  "expiresAt": "2026-06-17T11:55:00Z",
  "renewedFromId": 8801,
  "active": true,
  "status": "PENDING",
  "pricePaid": 0.70,
  "currency": "eur",
  "stripeCheckoutSessionId": "cs_test_f6g7h8i9"
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
        participant UC as RenewStreetReservationUseCase
        participant RS as StreetReservationStore
        participant CS as CarStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    C->>GW: POST .../street-reservations/{reservationId}/renewals {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: POST .../renewals (X-Auth-Sub=sub)
    RC->>UC: execute(userId, sub, reservationId, request)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        UC->>RS: findById(reservationId)
        alt reservation does not exist
            RS-->>C: 404 Not Found
        else belongs to another user
            UC-->>C: 403 Forbidden
        else original already expired
            UC-->>C: 422 Unprocessable Content
        else active
            UC->>CS: findById(carId)
            alt car missing / not owned
                CS-->>C: 404 / 403
            else valid
                UC->>RS: create(..., renewedFromId=original.id, ...)
                RS->>RR: save(StreetReservation: PENDING)
                RS-->>UC: StreetReservationView (PENDING)
                UC->>TG: streetReservationRenewed (audit → mobility_trails)
                UC-->>C: 201 StreetReservationDto
            end
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
        participant RC as UserStreetReservationController
        participant UC as RenewStreetReservationUseCase
        participant RS as StreetReservationStore
        participant CS as CarStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    C->>SEC: POST .../street-reservations/{reservationId}/renewals {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: POST .../renewals (X-Auth-Sub=sub)
    RC->>UC: execute(userId, sub, reservationId, request)
    alt sub != userId
        UC-->>C: 403 Forbidden
    else matches
        UC->>RS: findById(reservationId)
        alt reservation does not exist
            RS-->>C: 404 Not Found
        else belongs to another user
            UC-->>C: 403 Forbidden
        else original already expired
            UC-->>C: 422 Unprocessable Content
        else active
            UC->>CS: findById(carId)
            alt car missing / not owned
                CS-->>C: 404 / 403
            else valid
                UC->>RS: create(..., renewedFromId=original.id, ...)
                RS->>RR: save(StreetReservation: PENDING) [single modelcity DB]
                RS-->>UC: StreetReservationView (PENDING)
                UC->>TG: streetReservationRenewed (audit → mobility_trails)
                UC-->>C: 201 StreetReservationDto
            end
        end
    end
```
