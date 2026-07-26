---
title: SecurityAlertList
sidebar_label: SecurityAlertList
sidebar_position: 6
---

# SecurityAlertList

`packages/engagement/components/organisms/SecurityAlertList.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

Renders the grid of security-alert cards (each managing its own detail-modal state
independently). A "create alert" card is shown first when the user has permission.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `alerts` | `Array<object>` | yes | — | The alerts to render. |
| `areas` | `Record<number, { zoneName, neighbourhoodName }>` | yes | — | Resolved area names per alert. |
| `t` | `object` | yes | — | Card dictionary. |
| `tSeverity` | `Record<string,string>` | yes | — | Severity labels. |
| `lang` | `string` | yes | — | Locale. |
| `canCreate` | `boolean` | yes | — | Shows the create card. |

## Composition

- **Uses:** `molecules/engagement/SecurityAlertCard`, `atoms/core/Icon`, `next/link`.
- **Used by:** `organisms/engagement/AlertCenterView`.

## Internationalisation

Copy via `t` / `tSeverity`.

## Accessibility

Each card is a labelled button opening an accessible modal.

## Related

`molecules/engagement/SecurityAlertCard`, `organisms/engagement/AlertCenterView`,
`organisms/engagement/SecurityAlertsManageTable`.
