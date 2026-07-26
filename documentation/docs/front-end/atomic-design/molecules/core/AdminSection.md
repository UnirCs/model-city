---
title: AdminSection
sidebar_label: AdminSection
sidebar_position: 1
---

# AdminSection

`packages/core/components/molecules/AdminSection.js` · **Tier:** molecule · **Module:** core · **Rendering:** Server Component

## Purpose

A right-column panel that groups the staff management controls (edit / delete /
etc.) of a detail view. The controls are passed as `children` and stacked
vertically. Render it only when the user actually has management rights.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `title` | `string` | yes | — | Section heading text. |
| `children` | `ReactNode` | yes | — | The management controls, stacked with `gap-sm`. |
| `headingId` | `string` | no | `'admin-section-heading'` | Id linking the `<h2>` to the section via `aria-labelledby`. |

## Composition

- **Uses:** `atoms/core/Icon` (`admin_panel_settings`).
- **Used by:** the detail-view organisms across modules (event/place/route/space
  detail views, user detail) as the staff-only action panel.

## Internationalisation

None directly — `title` and the child controls are supplied (localized) by the
caller.

## Accessibility

Semantic `<section aria-labelledby>` with an `<h2>` heading, exposing the panel as
a labelled region to assistive tech.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`, `shadow-sm`,
`text-h3`, `text-primary`.

## Usage

```jsx
<AdminSection title={t.admin.manage}>
  <Button variant="outline">{t.common.edit}</Button>
  <DeleteEntityButton /* … */ />
</AdminSection>
```

## Related

`molecules/leisure/DeleteEntityButton`, the module detail-view organisms.
