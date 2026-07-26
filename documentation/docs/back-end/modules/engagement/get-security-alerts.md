---
title: List active security alerts
sidebar_label: List active security alerts
sidebar_position: 7
---

# List active security alerts

`GET /api/engagement/security-alerts` → `GetSecurityAlertsUseCase`

Returns paginated active (non-expired) security alerts. **Any authenticated user**. Page size
is **4**, sorted by `severity` ascending then `createdAt` descending. Cached in `securityAlerts`
keyed by `locale-page`.

Content is resolved to `Accept-Language`.

## Inputs

**`GET /api/engagement/security-alerts?page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<SecurityAlertDto>`.

```json
{
  "content": [
    {
      "id": 9001,
      "title": "Road closure for works",
      "severity": "IMPORTANT",
      "description": "Calle Mayor closed between 8:00 and 14:00.",
      "latitude": 40.4168,
      "longitude": -3.7038,
      "zoneId": 3,
      "neighbourhoodId": 7,
      "createdAt": "2026-06-17T07:00:00Z",
      "expiresAt": "2026-06-17T14:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 4,
  "first": true,
  "last": true
}
```

`severity` can be `IMPORTANT`, `MEDIUM` or `MILD`.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant AC as SecurityAlertController
        participant GU as GetSecurityAlertsUseCase
        participant AS as SecurityAlertStore
    end
    box rgb(224,247,224) DB · third parties
        participant AR as SecurityAlertRepository
    end

    C->>GW: GET /api/engagement/security-alerts?page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>AC: GET /security-alerts
    AC->>GU: execute(page, locale)
    Note over GU: if cached in securityAlerts[locale-page], returned without DB
    GU->>AS: findActive(now, PageRequest(page, 4, severity asc, createdAt desc))
    AS->>AR: findByExpiresAtAfter(now, pageable)
    AR-->>AS: Page<SecurityAlert>
    AS-->>GU: Page<SecurityAlertView>
    GU-->>C: 200 Page<SecurityAlertDto>
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
        participant AC as SecurityAlertController
        participant GU as GetSecurityAlertsUseCase
        participant AS as SecurityAlertStore
    end
    box rgb(224,247,224) DB · third parties
        participant AR as SecurityAlertRepository
    end

    C->>SEC: GET /api/engagement/security-alerts?page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>AC: GET /security-alerts
    AC->>GU: execute(page, locale)
    Note over GU: if cached in securityAlerts[locale-page], returned without DB
    GU->>AS: findActive(now, PageRequest(page, 4, severity asc, createdAt desc))
    AS->>AR: findByExpiresAtAfter(now, pageable) [single modelcity DB]
    AR-->>AS: Page<SecurityAlert>
    AS-->>GU: Page<SecurityAlertView>
    GU-->>C: 200 Page<SecurityAlertDto>
```
