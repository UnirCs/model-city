---
title: SpacesList
sidebar_label: SpacesList
sidebar_position: 18
---

# SpacesList

`packages/leisure/components/organisms/SpacesList.js` · **Tier:** organism · **Module:** leisure · **Rendering:** Server Component

## Purpose

A card grid of public (sports) spaces with pagination, plus fetch-error and empty
states. Administrators see a "create space" card in the grid and in the empty state.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `t` | `object` | yes | — | `leisure.sportsSpaces` dictionary. |
| `canManage` | `boolean` | yes | — | Shows the create card. |
| `fetchError` | `boolean` | yes | — | Renders the error banner. |
| `spaces` | `object[]` | yes | — | The space page content. |
| `page` | `number` | yes | — | Page index. |
| `totalPages` | `number` | yes | — | Total pages. |
| `pageHref` | `(pageNumber) => string` | yes | — | Pager href builder. |

## Composition

- **Uses:** `molecules/core/CatalogueCard`, `molecules/core/FetchErrorBanner`,
  `molecules/core/SectionPager`, `molecules/leisure/CreateCard`, `atoms/core/Icon`.
- **Used by:** the sports-spaces list page.

## Internationalisation

Copy from `leisure.sportsSpaces`.

## Accessibility

`<section aria-labelledby>` with an `<h2>`.

## Related

`organisms/leisure/SpaceDetailView`, `organisms/leisure/SpaceResourcesPanel`,
`molecules/core/CatalogueCard`.
