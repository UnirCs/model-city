---
title: StayCard
sidebar_label: StayCard
sidebar_position: 8
---

# StayCard

`packages/mobility/components/molecules/StayCard.js` · **Tier:** molecule · **Module:** mobility · **Rendering:** Client Component (`'use client'`)

## Purpose

Renders a single street reservation in two visual variants: **active** (secondary
accent, live remaining-time counter, renew CTA) and **past** (muted, no counter, no
renew CTA). The countdown is computed client-side from `expiresAt` and refreshed every
30 s; the actual renewal is delegated to the parent via `onRenew`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `reservation` | `object` | yes | — | `{ id, licensePlate, carNickname?, latitude, longitude, createdAt, expiresAt, active, renewedFromId? }`. |
| `labels` | `object` | yes | — | Card copy. |
| `lang` | `string` | yes | — | Locale for date/time formatting. |
| `onRenew` | `(reservation) => void` | — | — | Renew handler (active variant). |

## Behaviour

An interval recomputes the remaining time to `expiresAt` every 30 s; when active, the
renew CTA calls `onRenew`.

## Composition

- **Uses:** `atoms/core/Icon`, `useState`/`useEffect`.
- **Used by:** `organisms/mobility/StaysPanel`.

## Internationalisation

Copy via `labels`; timestamps localized with `lang`.

## Accessibility

The countdown updates a labelled region; the renew CTA is a labelled button.

## Styling & tokens

Active: secondary accent; past: muted; `bg-surface-container-lowest`,
`border-outline-variant`, `rounded-md`.

## Related

`molecules/mobility/RenewStayModal`, `organisms/mobility/StaysPanel`.
