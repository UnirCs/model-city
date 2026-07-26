---
title: RegistrationForm
sidebar_label: RegistrationForm
sidebar_position: 11
---

# RegistrationForm

`packages/core/components/organisms/RegistrationForm.js` · **Tier:** organism · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

The new-user registration form shown after Auth0 login when no backend profile
exists yet (see the registration gate in
[Auth & roles](../../../architecture/auth-and-roles.md)). Fields: email (read-only,
from Auth0), display name (editable), and neighbourhood (select). On submit it calls
the `registerUser` server action and redirects to `/{lang}/home` on success.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `email` | `string` | yes | — | The Auth0 email (read-only field). |
| `defaultName` | `string` | yes | — | Pre-filled account name. |
| `dict` | `object` | yes | — | The `registration` dictionary (labels, hints, errors). |
| `lang` | `string` | yes | — | For the success redirect. |

## Behaviour

Client-side validation requires a name and a neighbourhood; the neighbourhood
options come from `core/lib/config/neighbourhoods` (grouped by zone). Submit errors
surface an inline error banner; a "back" link logs out.

## Composition

- **Uses:** `atoms/core/Button`, `atoms/core/Icon`, `registerUser`
  (`core/lib/actions/registration`), the neighbourhoods config, an internal `Field`
  sub-component.
- **Used by:** the public `register` route (`core/routes-public/register`).

## Internationalisation

All copy from the `registration` dictionary (`dict`).

## Accessibility

Each field has a `<label>` and inline error (`role`-less but icon + text); the
select uses a custom chevron with `appearance-none`.

## Styling & tokens

`bg-surface-container-lowest` inputs, `border-error` on invalid, `rounded-md`,
`bg-error-container` submit-error banner.

## Related

`atoms/core/FormField`, `organisms/core/ProfileCard`,
[Auth & roles](../../../architecture/auth-and-roles.md).
