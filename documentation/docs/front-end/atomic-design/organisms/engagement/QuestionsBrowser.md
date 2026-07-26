---
title: QuestionsBrowser
sidebar_label: QuestionsBrowser
sidebar_position: 4
---

# QuestionsBrowser

`packages/engagement/components/organisms/QuestionsBrowser.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Server Component

## Purpose

Renders the civic-consultations browse UI: a page header, an optional fetch-error
banner, the **active** section (card grid with an optional admin "create" card),
collapsible **upcoming** and **finished** sections, and an empty state. Each section
has independent pagination driven by URL search params.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `t` | `object` | yes | — | `participation.questions` dictionary. |
| `fetchError` | `boolean` | yes | — | Renders the error banner. |
| `canCreate` | `boolean` | yes | — | Shows the admin create card. |
| `sections` | `{ active, future, past }` | yes | — | Each `{ questions, currentPage, totalPages }`. |

## Composition

- **Uses:** `molecules/core/CatalogueCard`, `molecules/core/CollapsibleHeader`,
  `molecules/core/SectionPager`, `molecules/core/FetchErrorBanner`,
  `molecules/leisure/CreateCard`-style create affordance, `atoms/core/Icon`,
  `next/link`.
- **Used by:** the `/participation/questions` page.

## Internationalisation

Copy from `participation.questions`.

## Accessibility

Collapsible sections via `CollapsibleHeader` (`aria-expanded`); cards are links.

## Related

`organisms/engagement/QuestionDetailView`, `organisms/engagement/CreateQuestionForm`.
