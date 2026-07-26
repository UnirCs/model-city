---
title: CertInvalidError
sidebar_label: CertInvalidError
sidebar_position: 4
---

# CertInvalidError

`packages/core/components/molecules/CertInvalidError.js` · **Tier:** molecule · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

An informational error panel shown when the browser cannot present a valid FNMT
digital certificate. It includes a link to the FNMT guide (rendered inline inside
the first message) and a second paragraph for the wrong-certificate scenario.
Designed to drop inside any flow that requires certificate verification (voting,
document signing, etc.).

## Props

| Prop | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `t` | `object` | yes | — | Localized strings: `certInvalid`, `certInvalidLine2`, `certInvalidLink`, `certInvalidLinkLabel`. |

The component splits `t.certInvalid` on `t.certInvalidLinkLabel` to weave the link
into the sentence (`before` + link + `after`).

## Composition

- **Uses:** `atoms/core/Icon` (`gpp_bad`, `open_in_new`).
- **Used by:** certificate-gated flows such as `molecules/engagement/VotingZone` /
  `organisms/core/OtpVerificationPanel` surfaces.

## Internationalisation

Fully caller-supplied via the `t` object (a dictionary slice); no direct dictionary
access.

## Accessibility

The external link carries `target="_blank"` + `rel="noopener noreferrer"` and an
`open_in_new` affordance icon. Uses the error-container colour pair for contrast.

## Styling & tokens

`bg-error-container`, `text-on-error-container`, `border-outline-variant`.

## Usage

```jsx
<CertInvalidError t={dict.certificate} />
```

## Related

`molecules/engagement/VotingZone`, [Local mTLS](../../../../how-to-start/mtls-local.md).
