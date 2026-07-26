---
title: Replace civic question
sidebar_label: Replace civic question
sidebar_position: 4
---

# Replace civic question (PUT)

`PUT /api/engagement/public-questions/{id}` → `UpdatePublicQuestionUseCase`

Fully replaces an existing civic question. **Admin or backoffice**. Only questions whose
`openDate` is still in the future can be updated (`409` otherwise). If the question does not
exist, `404`. The body is a complete replacement, same shape as creation, and the dates are
validated by `QuestionDateValidator`.

Returns `200` with the updated detail. Audits `CIVIC_QUESTION_UPDATED` with `updateType=PUT`
and evicts `publicQuestion` + `publicQuestions` caches.

## Inputs

**`PUT /api/engagement/public-questions/{id}`**

(Same body as `POST /api/engagement/public-questions`.)

```json
{
  "title": {
    "es": "¿Peatonalizar la calle Mayor (tramo norte)?",
    "en": "Should Calle Mayor be pedestrianized (north stretch)?"
  },
  "description": {
    "es": "Versión revisada de la propuesta.",
    "en": "Revised proposal."
  },
  "imageUrl": "https://cdn.modelcity.example/questions/42.jpg",
  "openDate": "2026-07-01",
  "closeDate": "2026-07-20",
  "zoneId": 3,
  "neighbourhoodId": 7,
  "objectives": [
    { "objective": { "es": "Reducir el ruido", "en": "Reduce noise" }, "sortOrder": 0 }
  ]
}
```

## Outputs

- **`200 OK`** — `CivicQuestionDetailDto`.
- **`400 Bad Request`** — validation or date rules violated.
- **`403 Forbidden`** — unauthorized role.
- **`404 Not Found`** — question does not exist.
- **`409 Conflict`** — question is not future-dated.

```json
{
  "id": 42,
  "title": "Should Calle Mayor be pedestrianized (north stretch)?",
  "description": "Revised proposal.",
  "openDate": "2026-07-01",
  "closeDate": "2026-07-20",
  "zoneId": 3,
  "neighbourhoodId": 7,
  "objectives": [
    { "id": 102, "objective": "Reduce noise", "sortOrder": 0 }
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
    actor C as Client (admin / backoffice)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient
        participant PC as PublicQuestionController
        participant UU as UpdatePublicQuestionUseCase
        participant QS as CivicQuestionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>GW: PUT /api/engagement/public-questions/{id} {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: PUT /public-questions/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>UU: execute(id, sub, request, locale)
        UU->>QS: findById(id)
        alt not found
            UU-->>C: 404 Not Found
        else openDate not after today
            UU-->>C: 409 Conflict
        else editable
            Note over UU: QuestionDateValidator.validateDates(...) → 400 if invalid
            UU->>QS: update(id, request)
            QS->>QR: save(CivicQuestion)
            QS-->>UU: CivicQuestionView
            UU->>TG: civicQuestionUpdated (PUT) (audit → engagement_trails)
            Note over UU: evicts caches publicQuestion + publicQuestions
            UU-->>C: 200 CivicQuestionDetailDto
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (admin / backoffice)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant ASP as ModelCityAccessAspect
        participant CC as CoreClient (in-process)
        participant PC as PublicQuestionController
        participant UU as UpdatePublicQuestionUseCase
        participant QS as CivicQuestionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>SEC: PUT /api/engagement/public-questions/{id} {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: PUT /public-questions/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>UU: execute(id, sub, request, locale)
        UU->>QS: findById(id)
        alt not found
            UU-->>C: 404 Not Found
        else openDate not after today
            UU-->>C: 409 Conflict
        else editable
            Note over UU: QuestionDateValidator.validateDates(...) → 400 if invalid
            UU->>QS: update(id, request)
            QS->>QR: save(CivicQuestion) [single modelcity DB]
            QS-->>UU: CivicQuestionView
            UU->>TG: civicQuestionUpdated (PUT) (audit → engagement_trails)
            Note over UU: evicts caches publicQuestion + publicQuestions
            UU-->>C: 200 CivicQuestionDetailDto
        end
    end
```
