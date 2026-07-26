---
title: SecurityAlertCard
sidebar_label: SecurityAlertCard
sidebar_position: 7
---

# SecurityAlertCard

`packages/engagement/components/molecules/SecurityAlertCard.js` · **Tier:** molecule · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

A compact, severity-coloured security-alert card showing the title, area
(zone · neighbourhood) and timestamp. Clicking it opens a detail modal with the full
description and metadata.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `alert` | `object` | yes | — | The alert (`title`, `description`, `severity`, `createdAt`, …). |
| `zoneName` | `string` | no | — | Zone display name. |
| `neighbourhoodName` | `string` | no | — | Neighbourhood display name. |
| `t` | `object` | yes | — | Card/modal dictionary (labels, `closeModal`, `unknownZone`, `wholeZone`). |
| `tSeverity` | `object` | yes | — | Severity labels. |
| `lang` | `string` | yes | — | Locale for date formatting. |

## Behaviour

The severity drives colour via `severityVisual(alert.severity)` (border/bubble/chip
classes + icon). The detail modal renders the description and a `<dl>` of zone /
neighbourhood / created-at.

## Composition

- **Uses:** `molecules/core/Modal`, `atoms/core/Icon`, `severityVisual`
  (`engagement/lib/security/severity`).
- **Used by:** `organisms/engagement/SecurityAlertList`,
  `molecules/engagement/AlertsMap` popups.

## Internationalisation

Copy via `t` / `tSeverity`; timestamps via `Intl.DateTimeFormat(lang)`.

## Accessibility

The card is a labelled button; the detail is the accessible `core/Modal`; long text
wraps with `overflow-wrap`.

## Styling & tokens

Severity-driven `border`/`bubble`/`chip` classes, `bg-surface-container-lowest`,
`rounded-xl`.

## Related

`organisms/engagement/SecurityAlertList`, `molecules/engagement/AlertsMap`,
`molecules/core/Modal`.
