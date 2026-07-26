---
title: DeleteEntityButton
sidebar_label: DeleteEntityButton
sidebar_position: 4
---

# DeleteEntityButton

`packages/leisure/components/molecules/DeleteEntityButton.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

A destructive action button that opens a confirmation modal and invokes a provided
server action; on success it navigates to a fallback URL. Generic enough to reuse for
city routes, city places or any resource whose deletion needs explicit confirmation.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `id` | `string \| number` | yes | — | The entity id passed to `action`. |
| `action` | `(id) => Promise<{ ok?, error? }>` | yes | — | The delete server action. |
| `onSuccessHref` | `string` | yes | — | Redirect target after a successful delete. |
| `labels` | `object` | yes | — | Localized copy (`button`, `confirmTitle`, `confirmBody`, `confirm`, `cancel`, `deleting`, `error`). |
| `className` | `string` | no | `''` | Extra classes on the trigger. |

## Behaviour

`useTransition` guards the confirm; on error it shows an inline banner and announces
it; on success it `router.replace(onSuccessHref)` + `refresh()`.

## Composition

- **Uses:** `atoms/core/Button`, `atoms/core/Icon`, `molecules/core/Modal`,
  `useAnnounce`, `useRouter`, `useTransition`.
- **Used by:** the leisure detail views via `molecules/core/AdminSection`
  (events, places, routes, spaces).

## Internationalisation

All copy via `labels`.

## Accessibility

The confirmation is the accessible `core/Modal`; error uses the error-container
colours; the button can't be dismissed while the delete is pending.

## Related

`molecules/core/Modal`, `molecules/core/AdminSection`.
