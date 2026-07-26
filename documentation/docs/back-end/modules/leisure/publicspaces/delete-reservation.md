---
title: Delete reservation
sidebar_label: Delete reservation
sidebar_position: 12
---

# Delete reservation

`DELETE /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations/{reservationId}` → `DeleteReservationUseCase`

Hard-deletes a reservation. **Admin or operator**. If the reservation or resource is not
found, `404`. On success, `204 No Content`.

Audits `SPACE_RESERVATION_DELETED`.

## Inputs

**`DELETE /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations/{reservationId}`** — no body.

## Outputs

- **`204 No Content`** — reservation deleted.
- **`403 Forbidden`** — not admin or operator.
- **`404 Not Found`** — reservation not found.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / operator)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant RES as ReservationController
        participant DU as DeleteReservationUseCase
        participant SR as SpaceReservationStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant SSR as SpaceReservationRepository
    end

    C->>GW: DELETE /api/leisure/public-spaces/{id}/resources/{rid}/reservations/{resid} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RES: DELETE /public-spaces/{id}/resources/{rid}/reservations/{resid}
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RES->>DU: execute(publicSpaceId, resourceId, reservationId, sub)
        DU->>SR: findById(reservationId)
        SSR-->>SR: optional SpaceReservationView
        alt not found or resource mismatch
            DU-->>C: 404 Not Found
        else found
            DU->>SR: delete(reservationId)
            SR->>SSR: deleteById(reservationId)
            DU->>TG: spaceReservationDeleted (audit → leisure_trails)
            DU-->>C: 204 No Content
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / operator)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant RES as ReservationController
        participant DU as DeleteReservationUseCase
        participant SR as SpaceReservationStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant SSR as SpaceReservationRepository
    end

    C->>SEC: DELETE /api/leisure/public-spaces/{id}/resources/{rid}/reservations/{resid} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RES: DELETE /public-spaces/{id}/resources/{rid}/reservations/{resid}
    Note over ASP: requires role PLATFORM_ADMIN or OPERATOR
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        RES->>DU: execute(publicSpaceId, resourceId, reservationId, sub)
        DU->>SR: findById(reservationId)
        SSR-->>SR: optional SpaceReservationView
        alt not found or resource mismatch
            DU-->>C: 404 Not Found
        else found
            DU->>SR: delete(reservationId)
            SR->>SSR: deleteById(reservationId) [single modelcity DB]
            DU->>TG: spaceReservationDeleted (audit → leisure_trails)
            DU-->>C: 204 No Content
        end
    end
```
