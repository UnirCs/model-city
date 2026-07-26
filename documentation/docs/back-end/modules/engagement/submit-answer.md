---
title: Submit answer
sidebar_label: Submit answer
sidebar_position: 6
---

# Submit answer

`POST /api/engagement/public-questions/{id}/answers` → `SubmitAnswerUseCase`

Registers a citizen's YES/NO vote. This is the only engagement operation that calls `core`:
it validates and burns an `operation-authorization` created by `core`.

The body contains `operationAuthorizationId` (UUID) and `vote` (`YES` or `NO`). The `sub` comes
from `X-Auth-Sub`. The use case:

1. Fetches the authorization with `CoreClient.getOperationAuthorization`.
2. Validates that it is `VERIFIED`, not expired, `resourceType` = `public-question`,
   `resourceId` = `{id}`, `userId` = `sub` and `operationType` = `CONFIRM_ANSWER`.
3. Verifies the question exists (`404` if not).
4. Deduplicates by `dniHash` from the authorization (the `(question_id, dni_hash)` unique
   constraint is the authoritative backstop). `409` if already voted.
5. Persists the answer and increments the vote tally.
6. Burns the authorization with `CoreClient.burnOperationAuthorization`.

Returns `200` with `answerId`. Transactional. Audits `ANSWER_SUBMITTED` and evicts
`publicQuestion` + `publicQuestions`.

## Inputs

**`POST /api/engagement/public-questions/{id}/answers`**

```json
{
  "operationAuthorizationId": "8f3a9c1e-2b7d-4e6a-9f12-0a1b2c3d4e5f",
  "vote": "YES"
}
```

## Outputs

- **`200 OK`** — `AnswerResponseDto`.
- **`400 Bad Request`** — `vote` not `YES`/`NO`.
- **`403 Forbidden`** — authorization belongs to another user.
- **`404 Not Found`** — question not found.
- **`409 Conflict`** — this DNI already voted on this question.
- **`410 Gone`** — authorization expired.
- **`422 Unprocessable Content`** — authorization not `VERIFIED` or resource/operation mismatch,
  or no `dniHash` (certificate not verified).

```json
{
  "answerId": 5012
}
```

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (citizen)
    box rgb(255,243,224) Perimeter
        participant GW as Gateway
    end
    box rgb(224,242,254) Component
        participant PC as PublicQuestionController
        participant SA as SubmitAnswerUseCase
        participant CC as CoreClient
        participant CORE as core (OperationAuthorizationController)
        participant QS as CivicQuestionStore
        participant AS as AnswerStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
        participant AR as AnswerRepository
    end

    C->>GW: POST /api/engagement/public-questions/{id}/answers {…} + JWT
    Note over GW: validates JWT and injects X-Auth-Sub
    GW->>PC: POST /public-questions/{id}/answers (X-Auth-Sub=sub)
    PC->>SA: execute(id, sub, request)
    SA->>CC: getOperationAuthorization(operationAuthorizationId)
    CC->>CORE: GET /operation-authorizations/{id} (HTTP)
    CORE-->>SA: OperationAuthorizationResponseDto
    Note over SA: validates VERIFIED (422), not expired (410),<br/>resourceType/resourceId/operationType (422), userId==sub (403), dniHash present (422)
    SA->>QS: findById(id)
    alt question not found
        QS-->>C: 404 Not Found
    else exists
        SA->>AS: hasVoted(id, dniHash)
        alt already voted
            AS-->>C: 409 Conflict
        else new vote
            SA->>AS: createAnswer(id, sub, dniHash, Vote.YES/NO)
            AS->>AR: save(Answer) + increment tally
            AR-->>AS: answerId
            SA->>CC: burnOperationAuthorization(operationAuthorizationId)
            CC->>CORE: PATCH /operation-authorizations/{id}/burn (HTTP)
            SA->>TG: answerSubmitted (audit → engagement_trails)
            Note over SA: evicts caches publicQuestion + publicQuestions
            SA-->>C: 200 AnswerResponseDto
        end
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (citizen)
    box rgb(255,243,224) Perimeter
        participant SEC as Security filter (OAuth2 Resource Server)
    end
    box rgb(224,242,254) Component
        participant PC as PublicQuestionController
        participant SA as SubmitAnswerUseCase
        participant CC as CoreClient (in-process)
        participant GOA as GetOperationAuthorizationUseCase
        participant BOA as BurnOperationAuthorizationUseCase
        participant QS as CivicQuestionStore
        participant AS as AnswerStore
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant QR as CivicQuestionRepository
        participant AR as AnswerRepository
    end

    C->>SEC: POST /api/engagement/public-questions/{id}/answers {…} + JWT
    Note over SEC: validates JWT and injects X-Auth-Sub
    SEC->>PC: POST /public-questions/{id}/answers (X-Auth-Sub=sub)
    PC->>SA: execute(id, sub, request)
    SA->>CC: getOperationAuthorization(operationAuthorizationId)
    CC->>GOA: execute(id) (in-process, no HTTP)
    GOA-->>SA: OperationAuthorizationResponseDto
    Note over SA: validates VERIFIED (422), not expired (410),<br/>resourceType/resourceId/operationType (422), userId==sub (403), dniHash present (422)
    SA->>QS: findById(id)
    alt question not found
        QS-->>C: 404 Not Found
    else exists
        SA->>AS: hasVoted(id, dniHash)
        alt already voted
            AS-->>C: 409 Conflict
        else new vote
            SA->>AS: createAnswer(id, sub, dniHash, Vote.YES/NO)
            AS->>AR: save(Answer) + increment tally [single modelcity DB]
            AR-->>AS: answerId
            SA->>CC: burnOperationAuthorization(operationAuthorizationId)
            CC->>BOA: execute(id) (in-process, no HTTP)
            SA->>TG: answerSubmitted (audit → engagement_trails)
            Note over SA: evicts caches publicQuestion + publicQuestions
            SA-->>C: 200 AnswerResponseDto
        end
    end
```
