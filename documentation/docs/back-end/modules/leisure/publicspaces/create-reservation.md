---
title: Create reservation
sidebar_label: Create reservation
sidebar_position: 11
---

# Create reservation

`POST /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations` → `CreateReservationUseCase`

Books a time slot on a reservable resource. **Any authenticated user** (citizen). The use
case validates:

- `endTime` is after `startTime`.
- The slot is between **09:00 and 19:00**.
- Duration does not exceed **2 hours**.
- The slot does not overlap an existing reservation for the same resource and date.

The use case resolves the citizen name from `CoreClient` and stores it. Audits
`SPACE_RESERVATION_CREATED`.

## Inputs

**`POST /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations`**

```json
{
  "reservationDate": "2026-07-25",
  "startTime": "10:00",
  "endTime": "12:00"
}
```

## Outputs

- **`201 Created`** — `ReservationDto` (privileged view).
- **`400 Bad Request`** — validation error (bad time range, outside window, >2h).
- **`409 Conflict`** — overlapping slot.
- **`404 Not Found`** — resource not found.

```json
{
  "id": 1000,
  "resourceId": 200,
  "reservationDate": "2026-07-25",
  "startTime": "10:00",
  "endTime": "12:00",
  "citizenSub": "auth0|123456",
  "citizenName": "Ana García"
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Citizen
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant RES as ReservationController
        participant CU as CreateReservationUseCase
        participant RR as ReservableResourceStore
        participant SR as SpaceReservationStore
        participant CC as CoreClient
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
        participant SSR as SpaceReservationRepository
    end

    C->>GW: POST /api/leisure/public-spaces/{id}/resources/{rid}/reservations {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RES: POST /public-spaces/{id}/resources/{rid}/reservations
    RES->>CU: execute(publicSpaceId, resourceId, citizenSub, request)
    CU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
    RR->>RSR: findByIdAndPublicSpaceIdAndDeletedFalse(rid, id)
    alt not found
        CU-->>C: 404 Not Found
    else found
        Note over CU: validate end>start, 09-19 window, <=2h
        CU->>SR: findByResourceAndDate(resourceId, reservationDate)
        SR->>SSR: findByResourceIdAndReservationDate(rid, date)
        SSR-->>SR: existing reservations
        Note over CU: check overlap
        alt overlapping
            CU-->>C: 409 Conflict
        else ok
            CU->>CC: getUserName(citizenSub)
            CU->>SR: create(resourceId, citizenSub, name, date, start, end)
            SR->>SSR: save(SpaceReservation)
            SSR-->>SR: SpaceReservation
            SR-->>CU: SpaceReservationView
            CU->>TG: spaceReservationCreated (audit → leisure_trails)
            CU-->>C: 201 ReservationDto
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Citizen
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant RES as ReservationController
        participant CU as CreateReservationUseCase
        participant RR as ReservableResourceStore
        participant SR as SpaceReservationStore
        participant CC as CoreClient (in-process)
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
        participant SSR as SpaceReservationRepository
    end

    C->>SEC: POST /api/leisure/public-spaces/{id}/resources/{rid}/reservations {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RES: POST /public-spaces/{id}/resources/{rid}/reservations
    RES->>CU: execute(publicSpaceId, resourceId, citizenSub, request)
    CU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
    RR->>RSR: findByIdAndPublicSpaceIdAndDeletedFalse(rid, id) [single modelcity DB]
    alt not found
        CU-->>C: 404 Not Found
    else found
        Note over CU: validate end>start, 09-19 window, <=2h
        CU->>SR: findByResourceAndDate(resourceId, reservationDate)
        SR->>SSR: findByResourceIdAndReservationDate(rid, date)
        SSR-->>SR: existing reservations
        Note over CU: check overlap
        alt overlapping
            CU-->>C: 409 Conflict
        else ok
            CU->>CC: getUserName(citizenSub)
            CU->>SR: create(resourceId, citizenSub, name, date, start, end)
            SR->>SSR: save(SpaceReservation)
            SSR-->>SR: SpaceReservation
            SR-->>CU: SpaceReservationView
            CU->>TG: spaceReservationCreated (audit → leisure_trails)
            CU-->>C: 201 ReservationDto
        end
    end
```
