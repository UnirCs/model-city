---
title: Civic question detail
sidebar_label: Civic question detail
sidebar_position: 2
---

# Civic question detail

`GET /api/engagement/public-questions/{id}` → `GetPublicQuestionUseCase`
`GET /api/engagement/public-questions/{id}?translations=full` → `GetPublicQuestionForEditUseCase`

Returns the full detail of one civic question, including objectives, vote counts
(`yesVotes`/`noVotes`) and the `submittable` flag. `submittable` is `true` only when the
caller sent a `sub` (i.e. `X-Auth-Sub` is present) and that user has not answered the question
yet. If not found, `404`.

The `?translations=full` variant returns every locale of each localizable field (admin
editing). It is not cached and has no `submittable` flag (the value is always `false`).

The standard detail is cached in `publicQuestion` keyed by `locale-id` (the shared part is
user-agnostic; `submittable` is applied per request outside the cache).

## Inputs

**`GET /api/engagement/public-questions/{id}`** — no body.

## Outputs

- **`200 OK`** — `CivicQuestionDetailDto`.
- **`404 Not Found`** — question does not exist.

```json
{
  "id": 42,
  "title": "¿Peatonalizar la calle Mayor?",
  "description": "Propuesta para restringir el tráfico rodado en el tramo central.",
  "imageUrl": "https://cdn.modelcity.example/questions/42.jpg",
  "openDate": "2026-06-10",
  "closeDate": "2026-06-30",
  "zoneId": 3,
  "neighbourhoodId": 7,
  "objectives": [
    { "id": 100, "objective": "Reducir el ruido", "sortOrder": 0 },
    { "id": 101, "objective": "Mejorar la seguridad peatonal", "sortOrder": 1 }
  ],
  "yesVotes": 128,
  "noVotes": 37,
  "submittable": true
}
```

When `translations=full`:

```json
{
  "id": 42,
  "title": "¿Peatonalizar la calle Mayor?",
  "description": "Propuesta para restringir el tráfico rodado en el tramo central.",
  "translations": {
    "title": {
      "es": "¿Peatonalizar la calle Mayor?",
      "en": "Should Calle Mayor be pedestrianized?"
    },
    "description": {
      "es": "Propuesta para restringir el tráfico rodado en el tramo central.",
      "en": "Proposal to restrict motor traffic in the central stretch."
    }
  },
  "yesVotes": 128,
  "noVotes": 37,
  "submittable": false
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
        participant PC as PublicQuestionController
        participant GU as GetPublicQuestionUseCase
        participant GF as GetPublicQuestionForEditUseCase
        participant CQR as CachedCivicQuestionReader
        participant QS as CivicQuestionStore
        participant AS as AnswerStore
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>GW: GET /api/engagement/public-questions/{id}?translations=full + JWT
    Note over GW: validates JWT and injects X-Auth-Sub (optional)
    GW->>PC: GET /public-questions/{id} (X-Auth-Sub=sub?)
    alt translations=full
        PC->>GF: execute(id, locale)
        GF->>CQR: getForEdit(id, locale)
        CQR->>QS: findById(id)
        QS->>QR: findById(id)
        alt not found
            QS-->>C: 404 Not Found
        else found
            QS-->>CQR: CivicQuestionView
            CQR-->>C: 200 CivicQuestionDetailDto (all translations)
        end
    else standard
        PC->>GU: execute(id, sub, locale)
        Note over GU: if cached in publicQuestion[locale-id], returned without DB
        GU->>CQR: getById(id, locale)
        CQR->>QS: findById(id)
        QS->>QR: findById(id)
        alt not found
            QS-->>C: 404 Not Found
        else found
            QS-->>CQR: CivicQuestionView (+ yes/no counts)
            CQR-->>GU: CivicQuestionDetailDto
            GU->>AS: hasAnswered(id, sub) (if sub != null)
            AS-->>GU: boolean
            Note over GU: withSubmittable(sub != null && !hasAnswered)
            GU-->>C: 200 CivicQuestionDetailDto
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
        participant PC as PublicQuestionController
        participant GU as GetPublicQuestionUseCase
        participant GF as GetPublicQuestionForEditUseCase
        participant CQR as CachedCivicQuestionReader
        participant QS as CivicQuestionStore
        participant AS as AnswerStore
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>SEC: GET /api/engagement/public-questions/{id}?translations=full + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub (optional)
    SEC->>PC: GET /public-questions/{id} (X-Auth-Sub=sub?)
    alt translations=full
        PC->>GF: execute(id, locale)
        GF->>CQR: getForEdit(id, locale)
        CQR->>QS: findById(id)
        QS->>QR: findById(id) [single modelcity DB]
        alt not found
            QS-->>C: 404 Not Found
        else found
            QS-->>CQR: CivicQuestionView
            CQR-->>C: 200 CivicQuestionDetailDto (all translations)
        end
    else standard
        PC->>GU: execute(id, sub, locale)
        Note over GU: if cached in publicQuestion[locale-id], returned without DB
        GU->>CQR: getById(id, locale)
        CQR->>QS: findById(id)
        QS->>QR: findById(id)
        alt not found
            QS-->>C: 404 Not Found
        else found
            QS-->>CQR: CivicQuestionView
            CQR-->>GU: CivicQuestionDetailDto
            GU->>AS: hasAnswered(id, sub) (if sub != null)
            AS-->>GU: boolean
            Note over GU: withSubmittable(sub != null && !hasAnswered)
            GU-->>C: 200 CivicQuestionDetailDto
        end
    end
```
