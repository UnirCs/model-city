---
title: engagement
sidebar_label: Overview
sidebar_position: 1
---

# engagement — REST API

`model-city-engagement` covers **citizen participation**: **civic questions**
(`/public-questions`, where verified residents vote YES/NO on questions with objectives) and
geolocated **security alerts** (`/security-alerts`, published by city staff). The service
registers in Eureka as `engagement`, so the gateway routes `/api/engagement/**`.

Authorization patterns:

- **Reads** (questions, alerts) — open to any authenticated user.
- **Question management** — creating requires **platform admin**; replacing and patching
  require **platform admin** or **backoffice**.
- **Alert management** (create / delete) — any staff role: **platform admin**, **backoffice**,
  **operator** or **mobility agent**.
- **Submitting an answer** — the citizen must present a valid **OTP operation authorization**:
  `SubmitAnswerUseCase` validates and burns the authorization created by `core`, enforcing one
  vote per verified DNI per question.

Localizable fields (`title`, `description`, `objective`) are `Map<String, String>` on requests
(`locale → text`, with a mandatory `es`); responses are resolved from `Accept-Language`,
falling back to `es`. Role checks are performed by `ModelCityAccessAspect` through `CoreClient`
(HTTP in microservices, in-process in the monolith).

External systems: **PostgreSQL** (`modelcity-engagement` in microservices, the single
`modelcity` in the monolith) and **Valkey** (cache: `publicQuestion`, `publicQuestions`,
`securityAlerts`). It calls `core` for role resolution and for the OTP operation authorization
consumed by *submit answer*. See the
[engagement data model](../../architecture/data-model.md#engagement-module).

The controllers are abstract extension points (`PublicQuestionController`,
`SecurityAlertController`, `SystemTrailController`); the platform registers the `Default*`
subclass as the active bean. See the [Extensibility Guide](../../extensibility-guide/index.md).

## Civic questions (`/public-questions`)

| Operation | Endpoint | Page |
| --- | --- | --- |
| List civic questions | `GET /api/engagement/public-questions` | [get-public-questions](./get-public-questions.md) |
| Civic question detail | `GET /api/engagement/public-questions/{id}` | [get-public-question](./get-public-question.md) |
| Create civic question | `POST /api/engagement/public-questions` | [create-public-question](./create-public-question.md) |
| Replace civic question | `PUT /api/engagement/public-questions/{id}` | [update-public-question](./update-public-question.md) |
| Patch civic question | `PATCH /api/engagement/public-questions/{id}` | [patch-public-question](./patch-public-question.md) |
| Submit answer | `POST /api/engagement/public-questions/{id}/answers` | [submit-answer](./submit-answer.md) |

## Security alerts (`/security-alerts`)

| Operation | Endpoint | Page |
| --- | --- | --- |
| List active alerts | `GET /api/engagement/security-alerts` | [get-security-alerts](./get-security-alerts.md) |
| Create alert | `POST /api/engagement/security-alerts` | [create-security-alert](./create-security-alert.md) |
| Delete alert | `DELETE /api/engagement/security-alerts/{id}` | [delete-security-alert](./delete-security-alert.md) |

## Audit (system trails)

| Operation | Endpoint | Page |
| --- | --- | --- |
| Query the audit log | `GET /api/engagement/system-trails` | [get-system-trails](./get-system-trails.md) |
