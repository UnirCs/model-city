---
title: AddWorkerButton
sidebar_label: AddWorkerButton
sidebar_position: 1
---

# AddWorkerButton

`packages/core/components/organisms/AddWorkerButton.js` · **Tier:** organism · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The "add municipal operator" action for the Administration → Workers subsection. It
opens the shared [`CreateAgentModal`](../../molecules/core/CreateAgentModal.md) and
refreshes the worker list (`router.refresh()`) after a successful invitation so the
new agent appears.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `onCreateAgent` | `(payload) => Promise<{ ok: true } \| { error, status? }>` | yes | — | The invitation server action. |
| `buttonLabel` | `string` | yes | — | Trigger button text. |
| `modalLabels` | `object` | yes | — | Localized labels for `CreateAgentModal` (`admin.createAgent`). |

## Composition

- **Uses:** `atoms/core/Button`, `atoms/core/Icon`,
  `molecules/core/CreateAgentModal`, `useRouter`, `useState`.
- **Used by:** the Administration → Workers page.

## Internationalisation

`buttonLabel` + `modalLabels` supplied by the caller (the page resolves them from
the `admin` dictionary).

## Accessibility

A standard button that opens the accessible `CreateAgentModal`; the list refresh is
deferred until the modal closes.

## Usage

```jsx
<AddWorkerButton onCreateAgent={inviteAgent} buttonLabel={t.admin.addWorker} modalLabels={t.admin.createAgent} />
```

## Related

`molecules/core/CreateAgentModal`, `organisms/core/UserCardGrid`.
