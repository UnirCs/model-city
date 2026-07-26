---
title: FormField
sidebar_label: FormField
sidebar_position: 7
---

# FormField

`packages/core/components/atoms/FormField.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

A wrapper that groups a label, an input/select slot, an optional hint and an
optional inline validation error. Beyond rendering the surrounding markup, it
**injects accessibility attributes into the first valid child element** via
`React.cloneElement`, so the field control is correctly labelled and described
without the caller wiring `id`/`aria-*` by hand.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `id` | `string` | yes | — | Base id; injected onto the first child (if it has none) and used to derive `-error` / `-hint` ids. |
| `label` | `string` | yes | — | Visible label (rendered in a `<label htmlFor={id}>`). |
| `hint` | `string` | no | — | Helper text (shown only when there is no error). |
| `error` | `string \| null` | no | — | Inline error message; when set, marks the field invalid. |
| `required` | `boolean` | no | `false` | Adds the `*` marker and native `required` + `aria-required` on the child. |
| `children` | `ReactNode` | yes | — | The field control(s); a11y attrs are injected into the **first valid** child only. |

## Behaviour — injected attributes

Into the first valid child (only when the child does not already declare them):

- `id` — set to the prop `id`.
- `required` + `aria-required="true"` — when `required`.
- `aria-invalid="true"` — when `error` is truthy.
- `aria-describedby` — wired to the error paragraph (`{id}-error`, `role="alert"`)
  or, when there is no error, the hint paragraph (`{id}-hint`).

Subsequent children pass through untouched.

## Composition

- **Uses:** React `Children` / `cloneElement` / `isValidElement`; native `<label>`
  and `<p>`.
- **Used by:** every form organism (registration, event/place/route/space forms,
  sanction/reservation forms, modals).

## Internationalisation

None directly — `label`, `hint` and `error` are supplied (localized) by the caller.

## Accessibility

Implements WCAG 2.2 SC 1.3.1 (Info and Relationships), 3.3.1 (Error
Identification), 3.3.2 (Labels or Instructions) and 4.1.2 (Name/Role/Value). The
error paragraph uses `role="alert"`; the `*` marker is `aria-hidden`.

## Styling & tokens

`flex flex-col gap-xs`, `text-label-md`, `text-caption`, `text-error`,
`text-on-surface-variant`.

## Usage

```jsx
<FormField id="email" label={t.fields.email} required error={errors.email}>
  <input type="email" autoComplete="email" />
</FormField>
```

## Related

`atoms/core/Button`, and the form organisms across all modules.
