---
title: Avatar
sidebar_label: Avatar
sidebar_position: 2
---

# Avatar

`packages/core/components/atoms/Avatar.js` · **Tier:** atom · **Module:** core · **Rendering:** Server Component

## Purpose

Displays a profile picture, or the user's initials as a fallback, in four sizes.
When `src` is provided it renders an `<img>`; otherwise it derives up to two
initials from `name` and renders them in a coloured circle.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `name` | `string` | no | `''` | Full name; the first letters of the first two words become the initials, and it is used as the `aria-label` of the initials fallback. |
| `src` | `string \| null` | no | `null` | Image URL. When present, renders an `<img>` instead of initials. |
| `size` | `'sm' \| 'md' \| 'lg' \| 'xl'` | no | `'md'` | Diameter + type scale. Unknown → `md`. |
| `className` | `string` | no | `''` | Extra classes appended last. |

## Variants & states

`sizeMap`: `sm` (`w-8 h-8`), `md` (`w-10 h-10`), `lg` (`w-14 h-14`), `xl`
(`w-20 h-20`), each paired with a type-scale token for the initials. Two visual
states: **image** (`<img>`, rounded, object-cover) and **initials** (primary-
container circle).

## Composition

- **Uses:** native `<img>` / `<span>`.
- **Used by:** profile and user-supervision surfaces (e.g. `UserMenu`,
  `ProfileCard`, `UserMiniCard`).

## Internationalisation

None (the `name` string is provided by the caller).

## Accessibility

The image variant uses a generic `alt="photo"`. The initials variant carries
`aria-label={name}` so the person is announced. `select-none` prevents accidental
text selection of the initials.

## Styling & tokens

`bg-primary-container` / `text-on-primary-container`, `border-outline-variant`,
`rounded-full`, size + type-scale tokens.

## Usage

```jsx
<Avatar name="Ada Lovelace" size="lg" />
<Avatar name="Ada Lovelace" src={user.pictureUrl} />
```

## Related

`molecules/core/UserMiniCard`, `organisms/core/ProfileCard`.
