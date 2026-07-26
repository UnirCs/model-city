---
title: mobility
sidebar_label: Overview
sidebar_position: 1
---

# mobility — REST API

`model-city-mobility` covers **urban mobility**: a citizen's **cars**
(`/users/{userId}/cars`), **SER-zone reservations** (regulated parking,
`/street-reservations` and `/users/{userId}/street-reservations`, paid via **Stripe
Checkout**) and parking **sanctions** (`/sanctions` and `/users/{userId}/sanctions`). The
service registers in Eureka as `mobility`, so the gateway routes `/api/mobility/**`.

Two authorization patterns coexist:

- **Citizen endpoints** (`/users/{userId}/...`) — no role check; the use case itself requires
  the token's `sub` (`X-Auth-Sub`) to **match** the path `{userId}`, returning `403` otherwise.
  On cars/sanctions it additionally checks resource ownership.
- **Management endpoints** (`/street-reservations`, `/sanctions`) — require **platform admin**
  or **mobility agent**, verified by `ModelCityAccessAspect` through `CoreClient`.

Reservation payment: the client creates the **Checkout Session** in Stripe and the reservation
is persisted with its `checkoutSessionId` in the initial `PENDING` state; later, the **Stripe
webhook** (`POST /stripe/webhook`, a **public** route) marks it `PAID` or `CANCELLED`.

External systems: **Stripe** (Checkout + signed webhook), **PostgreSQL** (`modelcity-mobility`
in microservices, `modelcity` in the monolith) and **Valkey** (cache). This domain does **not**
call `core` except to resolve the role on management endpoints. See the
[mobility data model](../../architecture/data-model.md#mobility-module-modelcity-mobility).

## Cars (`/users/{userId}/cars`)

| Operation | Endpoint | Page |
| --- | --- | --- |
| Register car | `POST /api/mobility/users/{userId}/cars` | [create-car](./create-car.md) |
| List citizen's cars | `GET /api/mobility/users/{userId}/cars` | [get-user-cars](./get-user-cars.md) |

## SER-zone reservations

| Operation | Endpoint | Page |
| --- | --- | --- |
| Create reservation | `POST /api/mobility/users/{userId}/street-reservations` | [create-street-reservation](./create-street-reservation.md) |
| List citizen's reservations | `GET /api/mobility/users/{userId}/street-reservations` | [get-user-street-reservations](./get-user-street-reservations.md) |
| Renew reservation | `POST /api/mobility/users/{userId}/street-reservations/{id}/renewals` | [renew-street-reservation](./renew-street-reservation.md) |
| List reservations (management) | `GET /api/mobility/street-reservations` | [get-street-reservations](./get-street-reservations.md) |
| Stripe payment webhook | `POST /api/mobility/stripe/webhook` | [stripe-webhook](./stripe-webhook.md) |

## Sanctions

| Operation | Endpoint | Page |
| --- | --- | --- |
| Issue sanction | `POST /api/mobility/sanctions` | [create-sanction](./create-sanction.md) |
| List sanctions (management) | `GET /api/mobility/sanctions` | [get-sanctions](./get-sanctions.md) |
| Sanction detail (management) | `GET /api/mobility/sanctions/{id}` | [get-sanction](./get-sanction.md) |
| List my sanctions | `GET /api/mobility/users/{userId}/sanctions` | [get-user-sanctions](./get-user-sanctions.md) |
| My sanction detail | `GET /api/mobility/users/{userId}/sanctions/{id}` | [get-user-sanction](./get-user-sanction.md) |

## Audit (system trails)

| Operation | Endpoint | Page |
| --- | --- | --- |
| Query the audit log | `GET /api/mobility/system-trails` | [get-system-trails](./get-system-trails.md) |
