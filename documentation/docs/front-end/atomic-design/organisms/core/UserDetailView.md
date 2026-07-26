---
title: UserDetailView
sidebar_label: UserDetailView
sidebar_position: 21
---

# UserDetailView

`packages/core/components/organisms/UserDetailView.js` · **Tier:** organism · **Module:** core · **Rendering:** Server Component

## Purpose

The admin detail layout for a citizen or worker: a back link, a read-only profile
card (avatar, name, email, status badge, and a field list) and a "System events"
section. The system-trail panel itself (filters, list, pager) is composed by the
page and passed as `children`, keeping this organism agnostic of how events are
fetched.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `profile` | `object` | yes | — | `{ id, name, email, createdAt?, role, status, neighbourhood? }`. |
| `lang` | `string` | yes | — | Locale for `formatDateTime`. |
| `backHref` | `string` | yes | — | Back link (already lang-prefixed). |
| `detailLabels` | `object` | yes | — | `admin.userDetail` labels (back, profileTitle, field labels). |
| `commonLabels` | `object` | yes | — | `admin.common` (`roleLabels`, `statusActive`/`Disabled`). |
| `children` | `ReactNode` | yes | — | The system-trail panel (e.g. `UserActivityPanel`). |

## Composition

- **Uses:** `atoms/core/Avatar`, `atoms/core/Icon`, `formatDateTime`; an internal
  `Field` sub-component.
- **Used by:** the Administration citizen/worker detail pages (which pass a
  `UserActivityPanel` as `children`).

## Internationalisation

Labels via `detailLabels` / `commonLabels`; the join date is localized via
`formatDateTime`.

## Accessibility

Single page `<h1>`; the profile fields use a `<dl>`; a disabled account renders the
avatar grayscaled and an error-tone status badge.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`; status badge
uses secondary-/error-container pairs.

## Related

`organisms/core/UserActivityPanel`, `organisms/core/UserCardGrid`,
`molecules/core/UserMiniCard`.
