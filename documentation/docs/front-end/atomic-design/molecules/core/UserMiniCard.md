---
title: UserMiniCard
sidebar_label: UserMiniCard
sidebar_position: 34
---

# UserMiniCard

`packages/core/components/molecules/UserMiniCard.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A compact supervision card for the Administration → Citizens / Workers grids.
Smaller than [`CatalogueCard`](./CatalogueCard.md): it shows the name, email, a
secondary line (neighbourhood for citizens, role for workers) and a status badge,
plus two fused actions — view detail (link) and enable/disable (callback). Purely
presentational: the parent grid owns the status state and the toggle handler.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `name` | `string` | yes | — | User's name. |
| `email` | `string` | yes | — | User's email. |
| `status` | `'ACTIVE' \| 'DISABLED'` | yes | — | Drives the status badge and the toggle action's polarity. |
| `secondary` | `string` | no | — | Secondary line (neighbourhood/role). |
| `secondaryIcon` | `string` | no | `'badge'` | Icon for the secondary line. |
| `detailHref` | `string` | yes | — | View-detail link (already lang-prefixed). |
| `busy` | `boolean` | no | `false` | Disables the toggle + shows a spinner during the mutation. |
| `onToggleStatus` | `() => void` | yes | — | Enable/disable callback. |
| `labels` | `object` | yes | — | Localized copy (`actionView`, `actionDisable`, `actionEnable`, `statusActive`, `statusDisabled`). |

## Composition

- **Uses:** `atoms/core/Icon`, `next/link`.
- **Used by:** `organisms/core/UserCardGrid`.

## Internationalisation

All copy via `labels`.

## Accessibility

Truncated fields carry `title` attributes; the busy state uses a spinner
(`progress_activity animate-spin`) and disables the toggle button.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`; status badge
uses secondary-/error-container pairs; fused bottom action bar.

## Usage

```jsx
<UserMiniCard name={u.name} email={u.email} status={u.status}
  detailHref={`/${lang}/administration/citizens/${u.id}`}
  onToggleStatus={() => toggle(u.id)} labels={t.admin.userCard} />
```

## Related

`organisms/core/UserCardGrid`, `molecules/core/CatalogueCard`, `atoms/core/Badge`.
