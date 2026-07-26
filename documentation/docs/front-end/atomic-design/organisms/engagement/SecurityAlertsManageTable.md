---
title: SecurityAlertsManageTable
sidebar_label: SecurityAlertsManageTable
sidebar_position: 7
---

# SecurityAlertsManageTable

`packages/engagement/components/organisms/SecurityAlertsManageTable.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

A client island that renders the alerts table for backoffice/admin users. Each row
opens a detail modal on click; a separate per-row "delete" button fires the
destructive server action behind a confirmation modal.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `alerts` | `Array<object>` | yes | — | The alerts to list. |
| `areas` | `Record<number, { zoneName, neighbourhoodName }>` | yes | — | Resolved area names. |
| `t` | `object` | yes | — | Table dictionary. |
| `tSeverity` | `Record<string,string>` | yes | — | Severity labels. |
| `tCenter` | `object` | yes | — | Alert-centre dictionary. |
| `lang` | `string` | yes | — | Locale. |

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/Icon`/`Button`, the delete action,
  `useState`/`useTransition`.
- **Used by:** `organisms/engagement/SecurityAlertsManageView`.

## Internationalisation

Copy via `t` / `tSeverity` / `tCenter`.

## Accessibility

Row detail + delete confirmation are accessible `core/Modal`s; delete is a labelled
button.

## Related

`organisms/engagement/SecurityAlertsManageView`, `molecules/engagement/SecurityAlertCard`.
