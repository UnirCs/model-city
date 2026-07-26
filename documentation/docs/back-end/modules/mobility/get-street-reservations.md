---
title: List reservations (management)
sidebar_label: List reservations (management)
sidebar_position: 7
---

# List reservations (management)

`GET /api/mobility/street-reservations` → `GetStreetReservationsUseCase`

Management endpoint to inspect reservations across the whole city. **Platform admin or mobility
agent** (verified by the aspect through `CoreClient`). Optional filters: `licensePlate`, a
`from`/`to` window (over `created_at`) and `active`; per the store implementation, `active`
takes precedence over the `from`/`to` window. Paginated 20 per page, ordered by `createdAt`
descending. Not cached.

This management endpoint does resolve the role: in microservices the aspect uses `CoreClient`
over **HTTP** to `core`; in the monolith, `InProcessCoreClient` resolves it **in-process** on
the local `users` table.

## Inputs

**`GET /api/mobility/street-reservations?licensePlate=1234ABC&active=true&page=0`** — no body.

| Query param | Type | Notes |
| --- | --- | --- |
| `licensePlate` | string | exact plate |
| `from` / `to` | ISO-8601 | window over `created_at` |
| `active` | boolean | takes precedence over `from`/`to` |
| `page` | int | 0-based; page size 20 |

## Outputs

- **`200 OK`** — `Page<StreetReservationDto>` (same element shape as the citizen listing).
- **`403 Forbidden`** — the requester is neither admin nor mobility agent.

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
  "size": 20,
  "first": true,
  "last": true
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / mobility agent)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant RC as StreetReservationController
        participant UC as GetStreetReservationsUseCase
        participant RS as StreetReservationStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    C->>GW: GET /api/mobility/street-reservations?licensePlate&from&to&active&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: GET /street-reservations (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub) (HTTP to core)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        RC->>UC: execute(licensePlate, from, to, active, page)
        UC->>RS: search(licensePlate, from, to, active, PageRequest(page, 20, createdAt desc))
        RS->>RR: findAll(spec, pageable)
        RR-->>RS: Page<StreetReservation>
        RS-->>UC: Page<StreetReservationView>
        UC-->>C: 200 Page<StreetReservationDto>
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / mobility agent)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant RC as StreetReservationController
        participant UC as GetStreetReservationsUseCase
        participant RS as StreetReservationStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as StreetReservationRepository
    end

    C->>SEC: GET /api/mobility/street-reservations?licensePlate&from&to&active&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: GET /street-reservations (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub) (in-process)
    Note right of CC: full chain in "Conventions → Role verification"
    alt role not allowed
        ASP-->>C: 403 Forbidden
    else allowed
        RC->>UC: execute(licensePlate, from, to, active, page)
        UC->>RS: search(licensePlate, from, to, active, PageRequest(page, 20, createdAt desc))
        RS->>RR: findAll(spec, pageable) [single modelcity DB]
        RR-->>RS: Page<StreetReservation>
        RS-->>UC: Page<StreetReservationView>
        UC-->>C: 200 Page<StreetReservationDto>
    end
```
