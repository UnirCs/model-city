---
title: Update event
sidebar_label: Update event
sidebar_position: 4
---

# Update event

`PUT /api/leisure/events/{id}` → `UpdateEventUseCase`

Fully replaces an existing event. **Backoffice or admin**. The same validation rules as
creation apply (via `EventWriteValidator`). If the event is not active, `404`. Returns `200`
with `EventDto`.

Audits `EVENT_UPDATED` and evicts `event` and `events`.

## Inputs

**`PUT /api/leisure/events/{id}`** — same body as creation.

## Outputs

- **`200 OK`** — `EventDto`.
- **`400 Bad Request`** — validation error.
- **`403 Forbidden`** — not backoffice or admin.
- **`404 Not Found`** — event not found.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (backoffice / admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant EC as EventController
        participant UU as UpdateEventUseCase
        participant VAL as EventWriteValidator
        participant ES as EventStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant CPS as CityPlaceStore
    end

    C->>GW: PUT /api/leisure/events/{id} {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>EC: PUT /events/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        EC->>UU: execute(id, sub, request, locale)
        UU->>ES: findActiveById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>VAL: validate(request)
            VAL->>CPS: existsById(placeId)
            alt validation fails
                UU-->>C: 400 Bad Request
            else ok
                UU->>ES: update(id, request)
                ES->>ER: save(Event)
                ER-->>ES: Event
                ES-->>UU: EventView
                UU->>TG: eventUpdated (audit → leisure_trails)
                Note over UU: evicts event + events
                UU-->>C: 200 EventDto
            end
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (backoffice / admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant EC as EventController
        participant UU as UpdateEventUseCase
        participant VAL as EventWriteValidator
        participant ES as EventStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant ER as EventRepository
        participant CPS as CityPlaceStore
    end

    C->>SEC: PUT /api/leisure/events/{id} {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>EC: PUT /events/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    alt unauthorized
        ASP-->>C: 403 Forbidden
    else authorized
        EC->>UU: execute(id, sub, request, locale)
        UU->>ES: findActiveById(id)
        alt not found
            UU-->>C: 404 Not Found
        else found
            UU->>VAL: validate(request)
            VAL->>CPS: existsById(placeId)
            alt validation fails
                UU-->>C: 400 Bad Request
            else ok
                UU->>ES: update(id, request)
                ES->>ER: save(Event) [single modelcity DB]
                ER-->>ES: Event
                ES-->>UU: EventView
                UU->>TG: eventUpdated (audit → leisure_trails)
                Note over UU: evicts event + events
                UU-->>C: 200 EventDto
            end
        end
    end
```
