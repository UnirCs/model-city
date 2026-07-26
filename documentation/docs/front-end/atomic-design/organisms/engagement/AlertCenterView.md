---
title: AlertCenterView
sidebar_label: AlertCenterView
sidebar_position: 1
---

# AlertCenterView

`packages/engagement/components/organisms/AlertCenterView.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Server Component

## Purpose

The two-column alert-centre body: the live alerts map on the left and the paginated
alerts list (with empty state) on the right.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `t` | `object` | yes | — | `security.alertCenter` dictionary. |
| `tSeverity` | `object` | yes | — | `security.severity` dictionary. |
| `canCreate` | `boolean` | yes | — | Shows the create-alert affordance. |
| `alerts` | `object[]` | yes | — | The alerts for the map + list. |
| `page` | `number` | yes | — | Page index. |
| `totalPages` | `number` | yes | — | Total pages. |

## Composition

- **Uses:** `molecules/engagement/AlertsMapClient`,
  `organisms/engagement/SecurityAlertList`, `LocalizedLink`.
- **Used by:** the security alert-centre page.

## Internationalisation

Copy from `security.alertCenter` / `severity`.

## Accessibility

Two labelled `<section>`s (map + list).

## Related

`molecules/engagement/AlertsMap`, `organisms/engagement/SecurityAlertList`.
