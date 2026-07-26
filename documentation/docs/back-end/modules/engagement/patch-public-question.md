---
title: Patch civic question
sidebar_label: Patch civic question
sidebar_position: 5
---

# Patch civic question (JSON Merge Patch)

`PATCH /api/engagement/public-questions/{id}` → `PatchPublicQuestionUseCase`

Partially updates a civic question with a JSON Merge Patch body
(`Content-Type: application/json`). **Admin or backoffice**. The current entity is serialized
into a `CivicQuestionRequestDto` and the patch is applied; an invalid patch returns `400`. If
the question does not exist, `404`.

Contrary to `PUT`, the patch does **not** require the question to be future-dated. Returns `200`
with the updated detail. Audits `CIVIC_QUESTION_UPDATED` with `updateType=PATCH` and evicts
`publicQuestion` + `publicQuestions`.

## Inputs

**`PATCH /api/engagement/public-questions/{id}`**

```json
{
  "title": {
    "es": "¿Peatonalizar la calle Mayor? (texto corregido)",
    "en": "Should Calle Mayor be pedestrianized? (corrected)"
  },
  "closeDate": "2026-07-05"
}
```

## Outputs

- **`200 OK`** — `CivicQuestionDetailDto` with merged fields.
- **`400 Bad Request`** — invalid merge patch or validation error.
- **`403 Forbidden`** — unauthorized role.
- **`404 Not Found`** — question does not exist.

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
        participant PU as PatchPublicQuestionUseCase
        participant QS as CivicQuestionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>GW: PATCH /api/engagement/public-questions/{id} (merge patch) + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: PATCH /public-questions/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>PU: execute(id, sub, patchBody, locale)
        PU->>QS: findById(id)
        alt not found
            PU-->>C: 404 Not Found
        else found
            Note over PU: apply JsonMergePatch to CivicQuestionRequestDto from entity → 400 if invalid
            PU->>QS: update(id, merged)
            QS->>QR: save(CivicQuestion)
            QS-->>PU: CivicQuestionView
            PU->>TG: civicQuestionUpdated (PATCH) (audit → engagement_trails)
            Note over PU: evicts caches publicQuestion + publicQuestions
            PU-->>C: 200 CivicQuestionDetailDto
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
        participant PU as PatchPublicQuestionUseCase
        participant QS as CivicQuestionStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>SEC: PATCH /api/engagement/public-questions/{id} (merge patch) + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: PATCH /public-questions/{id} (X-Auth-Sub=sub)
    Note over ASP: requires role PLATFORM_ADMIN or BACKOFFICE
    ASP->>CC: getUserRole(sub)
    Note right of CC: full chain in "Conventions → Role verification"
    alt not authorized
        ASP-->>C: 403 Forbidden
    else authorized
        PC->>PU: execute(id, sub, patchBody, locale)
        PU->>QS: findById(id)
        alt not found
            PU-->>C: 404 Not Found
        else found
            Note over PU: apply JsonMergePatch to CivicQuestionRequestDto from entity → 400 if invalid
            PU->>QS: update(id, merged)
            QS->>QR: save(CivicQuestion) [single modelcity DB]
            QS-->>PU: CivicQuestionView
            PU->>TG: civicQuestionUpdated (PATCH) (audit → engagement_trails)
            Note over PU: evicts caches publicQuestion + publicQuestions
            PU-->>C: 200 CivicQuestionDetailDto
        end
    end
```
