---
title: core
sidebar_label: Overview
sidebar_position: 1
---

# core — REST API

`model-city-core` owns **users** (citizens + staff, JIT-provisioned from Auth0, mTLS
certificate verification) and the **OTP / operation authorizations** (the second factor used
to authorize sensitive operations across the platform). Every other vertical reaches these
capabilities through the commons `CoreClient`.

External systems: **Auth0** (JWT validation at the edge + Management API for provisioning),
**SMTP** (OTP and invitation emails), **PostgreSQL** (`modelcity-core` in microservices, the
single `modelcity` in the monolith) and **Valkey** (cache). See the
[core data model](../../architecture/data-model.md#core-module).

The controllers are abstract extension points (`UsersController`, `AgentController`,
`OperationAuthorizationController`, `SystemTrailController`); the platform registers the
`Default*` subclass as the active bean. See the
[Extensibility Guide](../../extensibility-guide/index.md).

## Users

| Operation | Endpoint | Page |
| --- | --- | --- |
| Sign-in / JIT provisioning | `POST /api/core/users` | [register-user](./register-user.md) |
| Get user profile | `GET /api/core/users/{userId}` | [get-user](./get-user.md) |
| List users | `GET /api/core/users` | [list-users](./list-users.md) |
| Check citizen exists | `HEAD /api/core/users/{sub}` | [find-user](./find-user.md) |
| Enable / disable user | `PATCH /api/core/users/{userId}` | [set-user-status](./set-user-status.md) |
| Delete user | `DELETE /api/core/users/{userId}` | [delete-user](./delete-user.md) |
| Verify mTLS certificate | `POST /api/core/certificate-verifications` | [verify-certificate](./verify-certificate.md) |
| Invite agent (staff) | `POST /api/core/agents` | [invite-agent](./invite-agent.md) |

## OTP / operation authorizations

| Operation | Endpoint | Page |
| --- | --- | --- |
| Create challenge (send OTP) | `POST /api/core/operation-authorizations` | [create-challenge](./create-challenge.md) |
| Validate challenge (verify OTP) | `PATCH /api/core/operation-authorizations/{id}` | [validate-challenge](./validate-challenge.md) |
| Get operation authorization | `GET /api/core/operation-authorizations/{id}` | [get-operation-authorization](./get-operation-authorization.md) |
| Burn operation authorization | `PATCH /api/core/operation-authorizations/{id}/burn` | [burn-operation-authorization](./burn-operation-authorization.md) |

## Audit (system trails)

| Operation | Endpoint | Page |
| --- | --- | --- |
| Query the audit log | `GET /api/core/system-trails` | [get-system-trails](./get-system-trails.md) |
