---
title: leisure
sidebar_label: Overview
sidebar_position: 1
---

# leisure — REST API

`model-city-leisure` is the platform's largest vertical: **city places** (`/city-places`,
points of interest), **city routes** (`/city-routes`, ordered tourist routes over places),
**public spaces** (`/public-spaces`, civic facilities with reservable resources and citizen
reservations) and ticketed **events** (`/events`, paid via **Stripe**). The service registers
in Eureka as `leisure`, so the gateway routes `/api/leisure/**`.

Three authorization patterns coexist:

- **Public catalogue reads** (places, routes, public spaces, events) — open to any
  authenticated user.
- **Management writes** (create / update / delete of catalogue entities) — require **platform
  admin** or **backoffice**, verified by `ModelCityAccessAspect` through `CoreClient`.
- **Citizen-owned resources** (reservations, tickets) — the use case requires the token's `sub`
  (`X-Auth-Sub`) to own the resource.

Localizable fields (`name`, `description`, …) are `Map<String, String>` on requests
(`locale → text`, with a mandatory `es`); responses are resolved from `Accept-Language`,
falling back to `es`. Events ticketing runs on **Stripe**: the web flow uses a **Checkout
Session** and the mobile flow a **Payment Intent**; a **public** Stripe webhook confirms
payments, and deleting an event auto-refunds its outstanding tickets.

External systems: **Stripe** (Checkout / Payment Intent / refunds + signed webhook),
**PostgreSQL** (`modelcity-leisure` in microservices, the single `modelcity` in the monolith)
and **Valkey** (cache: `cityPlace`, `cityPlaces`, `cityRoute`, `cityRoutes`, `cityRoutePlaces`,
`publicSpace`, `publicSpaces`, `reservableResources`, `event`, `events`). It calls `core` only
to resolve the caller's role on management endpoints. See the
[leisure data model](../../architecture/data-model.md#leisure-module-modelcity-leisure).

The controllers are abstract extension points (`CityPlaceController`, `CityRouteController`,
`PublicSpaceController`, `ReservableResourceController`, `ReservationController`,
`EventController`, `EventTicketController`, `CitizenTicketController`, `StripeWebhookController`,
`SystemTrailController`); the platform registers the `Default*` subclass as the active bean. See
the [Extensibility Guide](../../extensibility-guide/index.md).

## City places (`/city-places`)

| Operation | Endpoint | Page |
| --- | --- | --- |
| List city places | `GET /api/leisure/city-places` | [get-city-places](./cityplaces/get-city-places.md) |
| City place detail | `GET /api/leisure/city-places/{id}` | [get-city-place](./cityplaces/get-city-place.md) |
| Create city place | `POST /api/leisure/city-places` | [create-city-place](./cityplaces/create-city-place.md) |
| Replace city place | `PUT /api/leisure/city-places/{id}` | [update-city-place](./cityplaces/update-city-place.md) |
| Delete city place | `DELETE /api/leisure/city-places/{id}` | [delete-city-place](./cityplaces/delete-city-place.md) |

## City routes (`/city-routes`)

| Operation | Endpoint | Page |
| --- | --- | --- |
| List city routes | `GET /api/leisure/city-routes` | [get-city-routes](./cityroutes/get-city-routes.md) |
| City route detail | `GET /api/leisure/city-routes/{id}` | [get-city-route](./cityroutes/get-city-route.md) |
| List route places | `GET /api/leisure/city-routes/{id}/city-places` | [get-city-route-places](./cityroutes/get-city-route-places.md) |
| Route place detail | `GET /api/leisure/city-routes/{id}/city-places/{placeId}` | [get-city-route-place](./cityroutes/get-city-route-place.md) |
| Create city route | `POST /api/leisure/city-routes` | [create-city-route](./cityroutes/create-city-route.md) |
| Replace city route | `PUT /api/leisure/city-routes/{id}` | [update-city-route](./cityroutes/update-city-route.md) |
| Delete city route | `DELETE /api/leisure/city-routes/{id}` | [delete-city-route](./cityroutes/delete-city-route.md) |

## Public spaces (`/public-spaces`)

| Operation | Endpoint | Page |
| --- | --- | --- |
| List public spaces | `GET /api/leisure/public-spaces` | [get-public-spaces](./publicspaces/get-public-spaces.md) |
| Public space detail | `GET /api/leisure/public-spaces/{id}` | [get-public-space](./publicspaces/get-public-space.md) |
| Create public space | `POST /api/leisure/public-spaces` | [create-public-space](./publicspaces/create-public-space.md) |
| Update public space | `PUT /api/leisure/public-spaces/{id}` | [update-public-space](./publicspaces/update-public-space.md) |
| Delete public space | `DELETE /api/leisure/public-spaces/{id}` | [delete-public-space](./publicspaces/delete-public-space.md) |
| List reservable resources | `GET /api/leisure/public-spaces/{publicSpaceId}/resources` | [get-reservable-resources](./publicspaces/get-reservable-resources.md) |
| Create reservable resource | `POST /api/leisure/public-spaces/{publicSpaceId}/resources` | [create-reservable-resource](./publicspaces/create-reservable-resource.md) |
| Update reservable resource | `PUT /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}` | [update-reservable-resource](./publicspaces/update-reservable-resource.md) |
| Delete reservable resource | `DELETE /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}` | [delete-reservable-resource](./publicspaces/delete-reservable-resource.md) |
| List resource reservations | `GET /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations` | [get-reservations](./publicspaces/get-reservations.md) |
| Create reservation | `POST /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations` | [create-reservation](./publicspaces/create-reservation.md) |
| Delete reservation | `DELETE /api/leisure/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations/{reservationId}` | [delete-reservation](./publicspaces/delete-reservation.md) |

## Events (`/events`)

| Operation | Endpoint | Page |
| --- | --- | --- |
| List events | `GET /api/leisure/events` | [get-events](./events/get-events.md) |
| Event detail | `GET /api/leisure/events/{id}` | [get-event](./events/get-event.md) |
| Create event | `POST /api/leisure/events` | [create-event](./events/create-event.md) |
| Update event | `PUT /api/leisure/events/{id}` | [update-event](./events/update-event.md) |
| Delete event | `DELETE /api/leisure/events/{id}` | [delete-event](./events/delete-event.md) |
| Purchase ticket (web) | `POST /api/leisure/events/{eventId}/tickets` | [purchase-ticket](./events/purchase-ticket.md) |
| List event tickets | `GET /api/leisure/events/{eventId}/tickets` | [get-event-tickets](./events/get-event-tickets.md) |
| Refund ticket | `POST /api/leisure/events/{eventId}/tickets/{ticketId}/refunds` | [refund-ticket](./events/refund-ticket.md) |
| Citizen ticket history | `GET /api/leisure/users/{userId}/tickets` | [get-citizen-tickets](./events/get-citizen-tickets.md) |
| Stripe payment webhook | `POST /api/leisure/stripe/webhook` | [stripe-webhook](./events/stripe-webhook.md) |

## Audit (system trails)

| Operation | Endpoint | Page |
| --- | --- | --- |
| Query the audit log | `GET /api/leisure/system-trails` | [get-system-trails](./get-system-trails.md) |
