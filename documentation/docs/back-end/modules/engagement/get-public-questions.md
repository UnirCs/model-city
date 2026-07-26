---
title: List civic questions
sidebar_label: List civic questions
sidebar_position: 1
---

# List civic questions

`GET /api/engagement/public-questions` → `GetPublicQuestionsUseCase`

Returns a paginated list of civic questions. **Any authenticated user**. Optional filters:
`status` (`active`, `past`, `future` or any other string), `zoneId` and `neighbourhoodId`.
Pagination is **3 per page**, base 1? Actually `page` is 0-based (Spring `PageRequest.of(page, 3)`).
Returns `Page<CivicQuestionSummaryDto>` (light projection: title only, no description,
objectives or vote counts).

Content is resolved to the `Accept-Language` locale (default `es`) using the i18n fallback
mechanism.

Cached in `publicQuestions` keyed by `locale-status-zoneId-neighbourhoodId-page`. Writes
(create/update/patch/answer) evict the whole cache.

## Inputs

**`GET /api/engagement/public-questions?status=active&zoneId=3&neighbourhoodId=7&page=0`** — no
body.

## Outputs

- **`200 OK`** — `Page<CivicQuestionSummaryDto>` (page size 3, number starts at 0).

```json
{
  "content": [
    {
      "id": 42,
      "title": "¿Peatonalizar la calle Mayor?",
      "imageUrl": "https://cdn.modelcity.example/questions/42.jpg",
      "openDate": "2026-06-10",
      "closeDate": "2026-06-30",
      "zoneId": 3,
      "neighbourhoodId": 7
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 3,
  "first": true,
  "last": true
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
        participant GU as GetPublicQuestionsUseCase
        participant QS as CivicQuestionStore
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>GW: GET /api/engagement/public-questions?status&zoneId&neighbourhoodId&page + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: GET /public-questions
    PC->>GU: execute(status, zoneId, neighbourhoodId, page, locale)
    Note over GU: if cached in publicQuestions[locale-status-zoneId-neighbourhoodId-page], returned without DB
    GU->>QS: search(status, zoneId, neighbourhoodId, PageRequest(page, 3))
    QS->>QR: findAll(spec, pageable)
    QR-->>QS: Page<CivicQuestion>
    QS-->>GU: Page<CivicQuestionView>
    GU-->>C: 200 Page<CivicQuestionSummaryDto>
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
        participant GU as GetPublicQuestionsUseCase
        participant QS as CivicQuestionStore
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
    end

    C->>SEC: GET /api/engagement/public-questions?status&zoneId&neighbourhoodId&page + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: GET /public-questions
    PC->>GU: execute(status, zoneId, neighbourhoodId, page, locale)
    Note over GU: if cached in publicQuestions[locale-status-zoneId-neighbourhoodId-page], returned without DB
    GU->>QS: search(status, zoneId, neighbourhoodId, PageRequest(page, 3))
    QS->>QR: findAll(spec, pageable) [single modelcity DB]
    QR-->>QS: Page<CivicQuestion>
    QS-->>GU: Page<CivicQuestionView>
    GU-->>C: 200 Page<CivicQuestionSummaryDto>
```
