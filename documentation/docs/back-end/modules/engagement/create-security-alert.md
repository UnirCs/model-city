---
title: Create security alert
sidebar_label: Create security alert
sidebar_position: 8
---

# Create security alert

`POST /api/engagement/security-alerts` → `CreateSecurityAlertUseCase`

Creates a geolocated security alert. Authorized roles: `PLATFORM_ADMIN`, `BACKOFFICE`,
`OPERATOR` or `MOBILITY_AGENT`. The body is validated with `jakarta.validation` and `expiresAt`
must be in the future (`400` otherwise). Returns `201` with `SecurityAlertDto`.

Localizable `title` and `description` are `Map<String, String>` with mandatory `es`; the
response is resolved to `Accept-Language`.

Audits `SECURITY_ALERT_CREATED` and evicts the whole `securityAlerts` cache.

## Inputs

**`POST /api/engagement/security-alerts`**

```json
{
  "title": {
    "es": "Corte de tráfico por obras",
    "en": "Road closure for works"
  },
  "severity": "IMPORTANT",
  "description": {
    "es": "Calle Mayor cortada entre las 8:00 y las 14:00.",
    "en": "Calle Mayor closed between 8:00 and 14:00."
  },
  "latitude": 40.4168,
  "longitude": -3.7038,
  "zoneId": 3,
  "neighbourhoodId": 7,
  "expiresAt": "2026-06-17T14:00:00Z"
}
```

## Outputs

- **`201 Created`** — `SecurityAlertDto`.
- **`400 Bad Request`** — validation error or `expiresAt` not in the future.
- **`403 Forbidden`** — unauthorized role.

```json
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
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (staff)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant AC as SecurityAlertController
        participant CU as CreateSecurityAlertUseCase
        participant AS as SecurityAlertStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant AR as SecurityAlertRepository
    end

    C->>GW: POST /api/engagement/security-alerts {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>AC: POST /security-alerts (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN, BACKOFFICE, OPERATOR or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        AC->>CU: execute(sub, request, locale)
        alt expiresAt not after now
            CU-->>C: 400 Bad Request
        else valid
            CU->>AS: create(request)
            AS->>AR: save(SecurityAlert)
            AR-->>AS: SecurityAlert
            AS-->>CU: SecurityAlertView
            CU->>TG: securityAlertCreated (audit → engagement_trails)
            Note over CU: evicts cache securityAlerts
            CU-->>C: 201 SecurityAlertDto
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (staff)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant AC as SecurityAlertController
        participant CU as CreateSecurityAlertUseCase
        participant AS as SecurityAlertStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant AR as SecurityAlertRepository
    end

    C->>SEC: POST /api/engagement/security-alerts {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>AC: POST /security-alerts (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN, BACKOFFICE, OPERATOR or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        AC->>CU: execute(sub, request, locale)
        alt expiresAt not after now
            CU-->>C: 400 Bad Request
        else valid
            CU->>AS: create(request)
            AS->>AR: save(SecurityAlert) [single modelcity DB]
            AR-->>AS: SecurityAlert
            AS-->>CU: SecurityAlertView
            CU->>TG: securityAlertCreated (audit → engagement_trails)
            Note over CU: evicts cache securityAlerts
            CU-->>C: 201 SecurityAlertDto
        end
    end
```
