---
title: ResourceReservationsPanel
sidebar_label: ResourceReservationsPanel
sidebar_position: 13
---

# ResourceReservationsPanel

`packages/leisure/components/organisms/ResourceReservationsPanel.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

The booking surface for a single reservable resource. Desktop layout: a 2/3 timeline
plus a 1/3 booking form for citizens (stacked on mobile); admins get a full-width
timeline. The timeline renders the 09:00–19:00 window with absolute-positioned blocks
per reservation; operators/admins can hover a block to reveal a delete button;
citizens get the booking form.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `spaceId` | `string \| number` | yes | — | Owning space id. |
| `resourceId` | `string \| number` | yes | — | The resource being booked. |
| `date` | `string` | yes | — | Timeline date (`YYYY-MM-DD`). |
| `reservations` | `Array<object> \| null` | yes | — | Bookings for the date (`null` on fetch error). |
| `canManage` | `boolean` | yes | — | Enables the delete affordance. |
| `canBook` | `boolean` | yes | — | Shows the booking form (citizens). |
| `t` | `object` | yes | — | `leisure.reservations` dictionary. |
| `tForm` | `object` | yes | — | `leisure.reservationForm` dictionary. |
| `spaceName` / `resourceName` / `resourceType` / `resourceDescription?` | `string` | yes/opt | — | Display metadata. |
| `tTypes` | `object` | yes | — | `leisure.resourceTypes` dictionary. |

## Composition

- **Uses:** `atoms/core/FormField`, `atoms/core/Button`, `atoms/core/Icon`, the
  space-reservation server actions.
- **Used by:** the resource reservations page (`/sports-spaces/[id]/resources/[rid]`).

## Internationalisation

Copy from `leisure.reservations` / `reservationForm` / `resourceTypes`.

## Accessibility

Timeline blocks and the booking form use labelled controls; delete is a labelled
button revealed on hover/focus.

## Related

`organisms/leisure/SpaceResourcesPanel`, `organisms/leisure/SpaceDetailView`.
