---
title: SecurityAlertsManageView
sidebar_label: SecurityAlertsManageView
sidebar_position: 8
---

# SecurityAlertsManageView

`packages/engagement/components/organisms/SecurityAlertsManageView.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Server Component

## Purpose

The management body for security alerts: an empty state when there are none, or the
summary table (with click-through detail modal and per-row delete) plus pagination.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `lang` | `string` | yes | — | Locale. |
| `t` | `object` | yes | — | `security.manage` dictionary. |
| `tSeverity` | `object` | yes | — | `security.severity` dictionary. |
| `tCenter` | `object` | yes | — | `security.alertCenter` dictionary (pagination labels). |
| `alerts` | `object[]` | yes | — | The alerts. |
| `areas` | `Record<string, { zoneName, neighbourhoodName }>` | yes | — | Resolved area names. |
| `page` | `number` | yes | — | Page index. |
| `totalPages` | `number` | yes | — | Total pages. |

## Composition

- **Uses:** `organisms/engagement/SecurityAlertsManageTable`,
  `molecules/core/LocalizedPager` (pagination), `LocalizedLink`.
- **Used by:** the `/security/alerts/manage` page (staff).

## Internationalisation

Copy from `security.manage` / `severity` / `alertCenter`.

## Accessibility

Empty state and table are clearly labelled; the table delegates its modals.

## Related

`organisms/engagement/SecurityAlertsManageTable`, `organisms/engagement/AlertCenterView`.
