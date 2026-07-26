---
title: Modal
sidebar_label: Modal
sidebar_position: 17
---

# Modal

`packages/core/components/molecules/Modal.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The accessible modal dialog base, backed by **`@radix-ui/react-dialog`**. It
provides, out of the box, a focus trap, ESC-to-close, return-focus on close, scroll
lock, backdrop click and full ARIA wiring (`role="dialog"`, `aria-modal="true"`,
`aria-labelledby`). Callers control visibility via `{ open, onClose }`.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Visibility (Radix `Dialog.Root open`). |
| `onClose` | `() => void` | yes | — | Called when the dialog requests to close. |
| `title` | `string` | yes | — | Dialog title (`Dialog.Title`). |
| `description` | `string` | no | — | Optional description; when omitted, the title is used as a visually-hidden description. |
| `closeLabel` | `string` | no | `dict.a11y.closeDialog` | Accessible label for the close button. |
| `panelClassName` | `string` | no | `w-[min(32rem,…)]` | Overrides the panel width/size. |
| `bodyClassName` | `string` | no | `px-lg py-md flex-1` | Overrides the body wrapper classes. |
| `children` | `ReactNode` | yes | — | Dialog body. |

## Composition

- **Uses:** `@radix-ui/react-dialog`, `atoms/core/Icon` (`close`),
  `atoms/core/VisuallyHidden`, `useTranslations()`.
- **Used by:** every modal molecule/organism (`SessionExpiredModal`,
  `SettingsModal`, `CreateAgentModal`, `AddCarModal`, `RenewStayModal`,
  `SanctionDetailModal`, `EditQuestionModal`, …).

## Internationalisation

Reads `dict.a11y.closeDialog` (fallback `'Close dialog'`) for the close button.

## Accessibility

Implements WCAG 2.2 SC 2.1.1 (Keyboard), 2.1.2 (No Keyboard Trap), 2.4.3 (Focus
Order), 2.4.7 (Focus Visible) and 4.1.2 (Name/Role/Value) via Radix. When no
`description` is given, a `VisuallyHidden` description is emitted to satisfy the
dialog contract.

## Styling & tokens

`bg-surface`, `border-outline-variant`, `rounded-md`, `shadow-xl`, overlay
`bg-black/50 backdrop-blur-sm`, `max-h-[90vh]`.

## Usage

```jsx
<Modal open={open} onClose={close} title={t.dialog.title} description={t.dialog.desc}>
  {/* body */}
</Modal>
```

## Related

`molecules/core/SessionExpiredModal`, `molecules/core/SettingsModal`,
`molecules/core/CreateAgentModal`.
