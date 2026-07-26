---
title: StaysPanel
sidebar_label: StaysPanel
sidebar_position: 6
---

# StaysPanel

`packages/mobility/components/organisms/StaysPanel.js` · **Tier:** organism · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

Splits the user's reservations into **active** and **past** buckets and renders each
as a responsive card grid. It owns the renewal modal so any active card can trigger it
without lifting state to the page.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `reservations` | `Array<object>` | yes | — | The user's reservations. |
| `labels` | `object` | yes | — | Panel + card copy. |
| `renewLabels` | `object` | yes | — | Renewal-modal copy. |
| `lang` | `string` | yes | — | Locale. |

## Behaviour

`useMemo` partitions reservations by `expiresAt`/`active` into active vs past;
selecting an active card sets the renew target for `RenewStayModal`.

## Composition

- **Uses:** `molecules/mobility/StayCard`, `molecules/mobility/RenewStayModal`,
  `useState`/`useMemo`.
- **Used by:** the `/mobility/my-stays` page.

## Internationalisation

Copy via `labels` / `renewLabels`.

## Accessibility

Card grids with labelled renew actions; the renewal modal is accessible.

## Related

`molecules/mobility/StayCard`, `molecules/mobility/RenewStayModal`.
