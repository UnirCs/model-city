---
title: List resource reservations
sidebar_label: List resource reservations
sidebar_position: 10
---

# List resource reservations

`GET /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations?date=2026-07-25` → `GetReservationsUseCase`

Returns the reservations for a resource on a specific date. **Any authenticated user**.
Default page size is **50**.

Admin/operator callers receive `citizenSub` and `citizenName`; other users get only the time
slot (`citizenSub` and `citizenName` are `null`).

If the resource is not found, `404`.

## Inputs

**`GET /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations?date=2026-07-25&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<ReservationDto>`.
- **`404 Not Found`** — resource not found.

### Public view

```json
{
  "content": [
    {
      "id": 1000,
      "resourceId": 200,
      "reservationDate": "2026-07-25",
      "startTime": "10:00",
      "endTime": "12:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 50
}
```

### Privileged view

```json
{
  "content": [
    {
      "id": 1000,
      "resourceId": 200,
      "reservationDate": "2026-07-25",
      "startTime": "10:00",
      "endTime": "12:00",
      "citizenSub": "auth0|123456",
      "citizenName": "Ana García"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 50
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
        participant RES as ReservationController
        participant GU as GetReservationsUseCase
        participant CC as CoreClient
        participant RR as ReservableResourceStore
        participant SR as SpaceReservationStore
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
        participant SSR as SpaceReservationRepository
    end

    C->>GW: GET /api/leisure/public-spaces/{id}/resources/{rid}/reservations?date=... + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RES: GET /public-spaces/{id}/resources/{rid}/reservations
    RES->>GU: execute(publicSpaceId, resourceId, date, sub, pageable)
    GU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
    RR->>RSR: findByIdAndPublicSpaceIdAndDeletedFalse(rid, id)
    alt not found
        RR-->>C: 404 Not Found
    else found
        GU->>CC: getUserRole(sub)
        GU->>SR: findByResourceAndDate(resourceId, date, pageable)
        SR->>SSR: findByResourceIdAndReservationDate(rid, date, pageable)
        SR-->>GU: Page<SpaceReservationView>
        Note over GU: map public or privileged view
        GU-->>C: 200 Page<ReservationDto>
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
        participant RES as ReservationController
        participant GU as GetReservationsUseCase
        participant CC as CoreClient (in-process)
        participant RR as ReservableResourceStore
        participant SR as SpaceReservationStore
    end
    box rgb(224,247,224) DB · third parties
        participant RSR as ReservableResourceRepository
        participant SSR as SpaceReservationRepository
    end

    C->>SEC: GET /api/leisure/public-spaces/{id}/resources/{rid}/reservations?date=... + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RES: GET /public-spaces/{id}/resources/{rid}/reservations
    RES->>GU: execute(publicSpaceId, resourceId, date, sub, pageable)
    GU->>RR: findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
    RR->>RSR: findByIdAndPublicSpaceIdAndDeletedFalse(rid, id) [single modelcity DB]
    alt not found
        RR-->>C: 404 Not Found
    else found
        GU->>CC: getUserRole(sub)
        GU->>SR: findByResourceAndDate(resourceId, date, pageable)
        SR->>SSR: findByResourceIdAndReservationDate(rid, date, pageable)
        SR-->>GU: Page<SpaceReservationView>
        Note over GU: map public or privileged view
        GU-->>C: 200 Page<ReservationDto>
    end
```
