---
title: Delete security alert
sidebar_label: Delete security alert
sidebar_position: 9
---

# Delete security alert

`DELETE /api/engagement/security-alerts/{id}` → `DeleteSecurityAlertUseCase`

Deletes a security alert. Authorized roles: `PLATFORM_ADMIN`, `BACKOFFICE`, `OPERATOR` or
`MOBILITY_AGENT`. If the alert does not exist, `404` (`ResourceNotFoundException`). On success,
returns `204 No Content`. Audits `SECURITY_ALERT_DELETED` and evicts the whole `securityAlerts`
cache.

## Inputs

**`DELETE /api/engagement/security-alerts/{id}`** — no body.

## Outputs

- **`204 No Content`** — alert deleted.
- **`403 Forbidden`** — unauthorized role.
- **`404 Not Found`** — alert does not exist.

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
        participant DU as DeleteSecurityAlertUseCase
        participant AS as SecurityAlertStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant AR as SecurityAlertRepository
    end

    C->>GW: DELETE /api/engagement/security-alerts/{id} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>AC: DELETE /security-alerts/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN, BACKOFFICE, OPERATOR or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        AC->>DU: execute(id, sub)
        DU->>AS: findById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>AS: deleteById(id)
            AS->>AR: deleteById(id)
            DU->>TG: securityAlertDeleted (audit → engagement_trails)
            Note over DU: evicts cache securityAlerts
            DU-->>C: 204 No Content
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
        participant DU as DeleteSecurityAlertUseCase
        participant AS as SecurityAlertStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant AR as SecurityAlertRepository
    end

    C->>SEC: DELETE /api/engagement/security-alerts/{id} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>AC: DELETE /security-alerts/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN, BACKOFFICE, OPERATOR or MOBILITY_AGENT
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        AC->>DU: execute(id, sub)
        DU->>AS: findById(id)
        alt not found
            DU-->>C: 404 Not Found
        else found
            DU->>AS: deleteById(id)
            AS->>AR: deleteById(id) [single modelcity DB]
            DU->>TG: securityAlertDeleted (audit → engagement_trails)
            Note over DU: evicts cache securityAlerts
            DU-->>C: 204 No Content
        end
    end
```
