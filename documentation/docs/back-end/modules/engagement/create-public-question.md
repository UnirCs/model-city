---
title: Create civic question
sidebar_label: Create civic question
sidebar_position: 3
---

# Create civic question

`POST /api/engagement/public-questions` → `CreatePublicQuestionUseCase`

Creates a new civic question with its objectives. **Admin only**
(`@ModelCityAccess.PlatformAdmin`). The body is validated with `jakarta.validation`
(title/description/objectives maps non-empty, dates, `zoneId`, `neighbourhoodId`). Then
`QuestionDateValidator` enforces that `openDate` is after today and `closeDate` is at least 3
days after `openDate`. Returns `201` with `CivicQuestionDetailDto` (votes at 0 and
`submittable=false`).

The localizable `title`, `description` and each `objective` are `Map<String, String>` with a
mandatory `es` entry; the response is resolved to `Accept-Language`.

Audits `CIVIC_QUESTION_CREATED` and evicts the whole `publicQuestions` cache.

## Inputs

**`POST /api/engagement/public-questions`**

```json
{
  "title": {
    "es": "¿Peatonalizar la calle Mayor?",
    "en": "Should Calle Mayor be pedestrianized?"
  },
  "description": {
    "es": "Propuesta para restringir el tráfico rodado en el tramo central.",
    "en": "Proposal to restrict motor traffic in the central stretch."
  },
  "imageUrl": "https://cdn.modelcity.example/questions/42.jpg",
  "openDate": "2026-07-01",
  "closeDate": "2026-07-20",
  "zoneId": 3,
  "neighbourhoodId": 7,
  "objectives": [
    { "objective": { "es": "Reducir el ruido", "en": "Reduce noise" }, "sortOrder": 0 },
    { "objective": { "es": "Mejorar la seguridad peatonal", "en": "Improve pedestrian safety" }, "sortOrder": 1 }
  ]
}
```

## Outputs

- **`201 Created`** — `CivicQuestionDetailDto`.
- **`400 Bad Request`** — validation error or dates invalid.
- **`403 Forbidden`** — not an admin.

```json
{
  "id": 42,
  "title": "Should Calle Mayor be pedestrianized?",
  "description": "Proposal to restrict motor traffic in the central stretch.",
  "imageUrl": "https://cdn.modelcity.example/questions/42.jpg",
  "openDate": "2026-07-01",
  "closeDate": "2026-07-20",
  "zoneId": 3,
  "neighbourhoodId": 7,
  "objectives": [
    { "id": 100, "objective": "Reduce noise", "sortOrder": 0 },
    { "id": 101, "objective": "Improve pedestrian safety", "sortOrder": 1 }
  ],
  "yesVotes": 0,
  "noVotes": 0,
  "submittable": false
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant PC as PublicQuestionController
        participant CU as CreatePublicQuestionUseCase
        participant QS as CivicQuestionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>GW: POST /api/engagement/public-questions {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: POST /public-questions (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>C: 403 Forbidden
    else admin
        PC->>CU: execute(sub, request, locale)
        Note over CU: QuestionDateValidator.validateDates(openDate, closeDate) → 400 if invalid
        CU->>QS: create(request)
        QS->>QR: save(CivicQuestion + objectives)
        QR-->>QS: CivicQuestion
        QS-->>CU: CivicQuestionView
        CU->>TG: civicQuestionCreated (audit → engagement_trails)
        Note over CU: evicts cache publicQuestions
        CU-->>C: 201 CivicQuestionDetailDto
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant PC as PublicQuestionController
        participant CU as CreatePublicQuestionUseCase
        participant QS as CivicQuestionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>SEC: POST /api/engagement/public-questions {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: POST /public-questions (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not an admin
        ASP-->>C: 403 Forbidden
    else admin
        PC->>CU: execute(sub, request, locale)
        Note over CU: QuestionDateValidator.validateDates(openDate, closeDate) → 400 if invalid
        CU->>QS: create(request)
        QS->>QR: save(CivicQuestion + objectives) [single modelcity DB]
        QR-->>QS: CivicQuestion
        QS-->>CU: CivicQuestionView
        CU->>TG: civicQuestionCreated (audit → engagement_trails)
        Note over CU: evicts cache publicQuestions
        CU-->>C: 201 CivicQuestionDetailDto
    end
```
