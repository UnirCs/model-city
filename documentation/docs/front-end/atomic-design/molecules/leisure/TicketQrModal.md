---
title: TicketQrModal
sidebar_label: TicketQrModal
sidebar_position: 14
---

# TicketQrModal

`packages/leisure/components/molecules/TicketQrModal.js` · **Tier:** molecule · **Module:** leisure · **Rendering:** Client Component (`'use client'`)

## Purpose

Displays a ticket's access QR code in a centred modal, with a download button that
serialises the SVG and triggers a browser download. Returns `null` when there is no
ticket.

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `open` | `boolean` | yes | — | Visibility. |
| `ticket` | `{ id: number, eventName? } \| null` | yes | — | The ticket; `null` renders nothing. |
| `labels` | `{ qrModalTitle, qrHint, qrDownload, close }` | yes | — | Localized copy. |
| `onClose` | `() => void` | yes | — | Close handler. |

## Behaviour

Renders the QR from the ticket id (`QRCodeSVG value={String(ticket.id)}`). The
download button serialises the inline `<svg>` to a Blob and clicks a temporary
`<a download>` (`ticket-{id}-qr.svg`).

## Composition

- **Uses:** `qrcode.react` (`QRCodeSVG`), `molecules/core/Modal`,
  `atoms/core/Button`, `atoms/core/Icon`, `useRef`/`useCallback`.
- **Used by:** `organisms/leisure/MyTicketsView`.

:::note[Module-only dependency]

`qrcode.react` is a **leisure-only** dependency — `create-model-city-app` drops it
from a city's `package.json` when the leisure module is not contracted.

:::

## Internationalisation

All copy via `labels`.

## Accessibility

Inherits `Modal`'s accessible dialog semantics; the QR sits on a white plate for
scanner contrast.

## Related

`molecules/core/Modal`, `organisms/leisure/MyTicketsView`,
`molecules/leisure/BuyTicketButton`.
