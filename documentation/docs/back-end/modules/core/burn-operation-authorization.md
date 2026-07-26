---
title: Burn operation authorization
sidebar_label: Burn operation authorization
sidebar_position: 13
---

# Burn operation authorization

`PATCH /api/core/operation-authorizations/{id}/burn` → `BurnOperationAuthorizationUseCase`

Marks a `VERIFIED` authorization as `BURNT`, **once the protected operation has actually run**,
so it cannot be reused (a verified OTP is good for a single operation). Only a `VERIFIED`
authorization can be burnt; any other state returns `409`. If the id does not exist, `404`.
Records the `OPERATION_AUTHORIZATION_BURNT` audit event.

It is invoked by the service that performed the operation, via `CoreClient`, right after
persisting it. In the civic-vote flow, engagement validates the authorization (`GET`), records
the vote, and then calls `burn` to invalidate it.

Like [get-operation-authorization](./get-operation-authorization.md): in **microservices**
`CoreClient.burnOperationAuthorization(id)` is an HTTP call through the controller; in the
**monolith** `InProcessCoreClient` calls `BurnOperationAuthorizationUseCase.execute(id)`
directly, with no controller and no network.

## Inputs

**`PATCH /api/core/operation-authorizations/{id}/burn`** — no body.

## Outputs

- **`200 OK`** — authorization burnt (`OperationAuthorizationResponseDto` with `status: BURNT`).
- **`404 Not Found`** — the authorization does not exist.
- **`409 Conflict`** — the authorization was not `VERIFIED`.

## Sequence — microservices

```mermaid
sequenceDiagram
    autonumber
    participant CV as Consumer vertical (e.g. SubmitAnswerUseCase)
    box rgb(224,242,254) Component
        participant CC as CoreClient
        participant OC as OperationAuthorizationController
        participant BU as BurnOperationAuthorizationUseCase
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant OR as OperationAuthorizationRepository
    end

    CV->>CC: burnOperationAuthorization(id)
    CC->>OC: PATCH /operation-authorizations/{id}/burn (HTTP to http://core)
    OC->>BU: execute(id)
    BU->>OR: findById(id)
    alt not found
        OR-->>CV: 404 Not Found
    else status != VERIFIED
        BU-->>CV: 409 Conflict
    else VERIFIED
        BU->>OR: save(status=BURNT)
        BU->>TG: operationAuthorizationBurnt (audit → core_trails)
        BU-->>CV: 200 OperationAuthorizationResponseDto
    end
```

## Sequence — monolith

```mermaid
sequenceDiagram
    autonumber
    participant CV as Consumer vertical (e.g. SubmitAnswerUseCase)
    box rgb(224,242,254) Component
        participant CC as CoreClient (InProcessCoreClient)
        participant BU as BurnOperationAuthorizationUseCase
        participant TG as SystemTrailGenerator
    end
    box rgb(224,247,224) DB · third parties
        participant OR as OperationAuthorizationRepository
    end

    CV->>CC: burnOperationAuthorization(id)
    CC->>BU: execute(id) (in-process, no HTTP)
    BU->>OR: findById(id) [single modelcity DB]
    alt not found
        OR-->>CV: 404 Not Found
    else status != VERIFIED
        BU-->>CV: 409 Conflict
    else VERIFIED
        BU->>OR: save(status=BURNT)
        BU->>TG: operationAuthorizationBurnt (audit → core_trails)
        BU-->>CV: 200 OperationAuthorizationResponseDto
    end
```
