---
title: Query the audit log
sidebar_label: Get system trails
sidebar_position: 10
---

# Query the audit log (engagement)

`GET /api/engagement/system-trails` → `GetSystemTrailsUseCase`

**Admin-only** (`PLATFORM_ADMIN`) read of the engagement vertical's audit log. Returns 20 events
per page ordered by `occurredAt` descending. Filters: `eventType`, `responsibleUserId`, `from`,
`to`, `page`.

Writes are automatic: `CreatePublicQuestionUseCase`, `UpdatePublicQuestionUseCase`,
`PatchPublicQuestionUseCase`, `SubmitAnswerUseCase`, `CreateSecurityAlertUseCase` and
`DeleteSecurityAlertUseCase` call `SystemTrailGenerator` (`com.modelcity.engagement.trails`),
which persists via `EngagementSystemTrailStore` into `engagement_trails`.

Vertical event types: `CIVIC_QUESTION_CREATED`, `CIVIC_QUESTION_UPDATED`, `ANSWER_SUBMITTED`,
`SECURITY_ALERT_CREATED`, `SECURITY_ALERT_DELETED`.

## Inputs

**`GET /api/engagement/system-trails?eventType=ANSWER_SUBMITTED&page=0`** — no body.

## Outputs

- **`200 OK`** — `Page<SystemTrailDto>` (20 per page, `occurredAt` descending).
- **`403 Forbidden`** — requester is not an admin.

```json
{
  "content": [
    {
      "eventId": "d4e5f6a7-2222-3333-4444-555566667777",
      "eventType": "ANSWER_SUBMITTED",
      "operationType": "CREATE",
      "occurredAt": "2026-06-17T10:30:00Z",
      "responsibleUserId": "auth0|abc",
      "responsibleUserRole": "MODEL-CITY-CITIZEN",
      "resourceType": "CIVIC_ANSWER",
      "resourceId": "5012",
      "payload": {
        "answerId": 5012,
        "questionId": 42,
        "citizenSub": "auth0|abc",
        "vote": "YES"
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor FE as Front-end (admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant SC as SystemTrailController
        participant UC as GetSystemTrailsUseCase
        participant ST as EngagementSystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant TR as EngagementSystemTrailRepository
    end

    FE->>GW: GET /api/engagement/system-trails?eventType&responsibleUserId&from&to&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>SC: GET /system-trails (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>FE: 403 Forbidden
    else admin
        SC->>UC: execute(SystemTrailQuery, page)
        UC->>ST: search(query, pageable[size=20, occurredAt desc])
        ST->>TR: findAll(spec, pageable)
        TR-->>ST: Page<EngagementTrail>
        ST-->>UC: Page<SystemTrailView>
        UC-->>SC: Page<SystemTrailDto>
        SC-->>FE: 200 Page<SystemTrailDto>
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor FE as Front-end (admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant SC as SystemTrailController
        participant UC as GetSystemTrailsUseCase
        participant ST as EngagementSystemTrailStore
    end
    box rgb(224,247,224) DB · third parties
        participant TR as EngagementSystemTrailRepository
    end

    FE->>SEC: GET /api/engagement/system-trails?eventType&responsibleUserId&from&to&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>SC: GET /system-trails (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>FE: 403 Forbidden
    else admin
        SC->>UC: execute(SystemTrailQuery, page)
        UC->>ST: search(query, pageable[size=20, occurredAt desc])
        ST->>TR: findAll(spec, pageable) [single modelcity DB]
        TR-->>ST: Page<EngagementTrail>
        ST-->>UC: Page<SystemTrailView>
        UC-->>SC: Page<SystemTrailDto>
        SC-->>FE: 200 Page<SystemTrailDto>
    end
```
