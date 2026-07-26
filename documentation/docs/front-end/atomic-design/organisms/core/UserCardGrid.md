---
title: UserCardGrid
sidebar_label: UserCardGrid
sidebar_position: 20
---

# UserCardGrid

`packages/core/components/organisms/UserCardGrid.js` · **Tier:** organism · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

A responsive grid of [`UserMiniCard`](../../molecules/core/UserMiniCard.md) for the
Administration → Citizens / Workers subsections. It owns the **optimistic** account
status of each card, invokes the `onToggleStatus` server action, and surfaces any
failure as an inline banner (403 → forbidden message).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `users` | `Array<{ id, name, email, role, status, neighbourhoodName? }>` | yes | — | The users to display. |
| `lang` | `string` | yes | — | For building detail hrefs. |
| `mode` | `'citizens' \| 'workers'` | yes | — | Chooses the secondary line (neighbourhood vs role) and icon. |
| `detailBasePath` | `string` | yes | — | Lang-relative detail base path. |
| `onToggleStatus` | `(userId, status) => Promise<{ ok: true } \| { error, status? }>` | yes | — | Enable/disable server action. |
| `labels` | `object` | yes | — | Localized copy (card actions/statuses, `toggleError`, `forbiddenAdmin`, `empty`, `roleLabels`). |

## Behaviour

Keeps per-user status overrides in state and applies the new status optimistically
on success; on failure it shows an error banner and leaves the card unchanged.

## Composition

- **Uses:** `atoms/core/Icon`, `molecules/core/UserMiniCard`, `useState`.
- **Used by:** the Administration citizens/workers list pages (with
  `AdminUserFilters`).

## Internationalisation

All copy via `labels`.

## Accessibility

The error state uses `role="alert"`; each card exposes labelled view/toggle actions.

## Styling & tokens

`grid ... lg:grid-cols-4`, `bg-surface-container-low`, `border-outline-variant`,
`rounded-md`.

## Related

`molecules/core/UserMiniCard`, `molecules/core/AdminUserFilters`,
`organisms/core/UserDetailView`.
