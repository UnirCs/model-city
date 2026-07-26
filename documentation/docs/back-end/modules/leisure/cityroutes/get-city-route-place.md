---
title: Route place detail
sidebar_label: Route place detail
sidebar_position: 4
---

# Place detail inside a city route

`GET /api/leisure/city-routes/{id}/city-places/{placeId}` → `GetCityRoutePlaceUseCase`

Returns the full `CityPlaceDto` for a place that belongs to a route. **Any authenticated user**.
If the route or the place is not found, `404`.

Cached in `cityRoutePlaces` keyed by `locale-route-{routeId}-place-{placeId}`.

## Inputs

**`GET /api/leisure/city-routes/{id}/city-places/{placeId}`** — no body.

## Outputs

- **`200 OK`** — `CityPlaceDto`.
- **`404 Not Found`** — route or place not found.

```json
{
  "id": 10,
  "name": "Reina Sofía Museum",
  "latitude": 40.4087,
  "longitude": -3.6945,
  "description": "National museum of 20th-century art.",
  "address": "Calle de Santa Isabel, 52",
  "photoUrls": [
    "https://cdn.modelcity.example/places/reina-sofia-1.jpg",
    "https://cdn.modelcity.example/places/reina-sofia-2.jpg"
  ],
  "accessInfo": "Metro Atocha",
  "accessibilityInfo": "Step-free access",
  "category": "MUSEUM",
  "visitDurationMinutes": 120
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
        participant RC as CityRouteController
        participant GU as GetCityRoutePlaceUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>GW: GET /api/leisure/city-routes/{id}/city-places/{placeId} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>RC: GET /city-routes/{id}/city-places/{placeId}
    RC->>GU: execute(routeId, placeId, locale)
    Note over GU: cache cityRoutePlaces[locale-route-id-place-placeId] if present
    GU->>RS: findById(routeId)
    RS->>RR: findById(routeId)
    alt not found
        RS-->>C: 404 Not Found
    else found
        RS-->>GU: CityRouteView with routePlaces
        Note over GU: filter place by placeId
        alt place not in route
            GU-->>C: 404 Not Found
        else found
            GU-->>C: 200 CityPlaceDto
        end
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
        participant RC as CityRouteController
        participant GU as GetCityRoutePlaceUseCase
        participant RS as CityRouteStore
    end
    box rgb(224,247,224) DB · third parties
        participant RR as CityRouteRepository
    end

    C->>SEC: GET /api/leisure/city-routes/{id}/city-places/{placeId} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>RC: GET /city-routes/{id}/city-places/{placeId}
    RC->>GU: execute(routeId, placeId, locale)
    Note over GU: cache cityRoutePlaces[locale-route-id-place-placeId] if present
    GU->>RS: findById(routeId)
    RS->>RR: findById(routeId) [single modelcity DB]
    alt not found
        RS-->>C: 404 Not Found
    else found
        RS-->>GU: CityRouteView with routePlaces
        Note over GU: filter place by placeId
        alt place not in route
            GU-->>C: 404 Not Found
        else found
            GU-->>C: 200 CityPlaceDto
        end
    end
```
