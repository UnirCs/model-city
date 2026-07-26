---
title: LocationSection
sidebar_label: LocationSection
sidebar_position: 8
---

# LocationSection

`packages/leisure/components/molecules/LocationSection.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Server Component

## Purpose

A reusable "location" panel shared by place/space detail views. It merges the map,
the postal address and a centred "open in external maps" CTA into one bordered
section. Renders nothing when there are neither coordinates nor an address.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `latitude` | `number \| null` | no | — | Marker latitude. |
| `longitude` | `number \| null` | no | — | Marker longitude. |
| `name` | `string` | yes | — | Place name (marker/label). |
| `address` | `string` | no | — | Postal address line. |
| `mapTitle` | `string` | yes | — | Section heading + map accessible name. |
| `openInMapsLabel` | `string` | yes | — | CTA label for the external-maps link. |
| `headingId` | `string` | no | `'location-map-heading'` | Heading id. |

## Behaviour

When coordinates are present it renders `PlaceMapClient` and builds a Google Maps
search link; the address (if any) is shown with a pin icon.

## Composition

- **Uses:** `atoms/core/Icon`, `molecules/leisure/PlaceMapClient`.
- **Used by:** `organisms/leisure/PlaceDetailView`, `organisms/leisure/SpaceDetailView`.

## Internationalisation

`mapTitle` + `openInMapsLabel` supplied (localized) by the caller.

## Accessibility

`<section aria-labelledby>`; the external link uses `rel="noopener noreferrer"` +
`open_in_new` icon.

## Styling & tokens

`bg-surface-container-lowest`, `border-outline-variant`, `rounded-md`,
`bg-secondary` CTA.

## Related

`molecules/leisure/PlaceMap`, `organisms/leisure/PlaceDetailView`,
`organisms/leisure/SpaceDetailView`.
