---
title: SecurityAlertForm
sidebar_label: SecurityAlertForm
sidebar_position: 5
---

# SecurityAlertForm

`packages/engagement/components/organisms/SecurityAlertForm.js` · **Tier:** organism · **Module:** engagement · **Rendering:** Client Component (`'use client'`)

## Purpose

The two-column form to create a new security alert. The left column hosts the
descriptive/metadata fields (severity, description, zone, neighbourhood, expiration);
the right column holds the interactive map picker that drives the latitude/longitude
values.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | Form dictionary. |
| `tSeverity` | `Record<string,string>` | yes | — | Severity labels. |
| `barrios` | `Array<{ id, zone, neighbourhoods }>` | yes | — | Zones + neighbourhoods for the selects. |
| `lang` | `string` | yes | — | Locale. |

## Composition

- **Uses:** `molecules/core/LocationPickerMapClient`, `atoms/core/FormField`,
  `atoms/core/Button`, `atoms/core/Icon`, the security-alert action,
  `useLocalizedBack`.
- **Used by:** the `/security/alerts/new` page.

## Internationalisation

Copy via `t`; severity options via `tSeverity`.

## Accessibility

Fields via `FormField`; the map picker is keyboard-operable with a manual-coordinate
alternative.

## Related

`molecules/core/LocationPickerMap`, `organisms/engagement/SecurityAlertList`,
`molecules/engagement/SecurityAlertCard`.
