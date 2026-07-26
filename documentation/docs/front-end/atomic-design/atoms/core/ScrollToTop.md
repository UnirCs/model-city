---
title: ScrollToTop
sidebar_label: ScrollToTop
sidebar_position: 10
---

# ScrollToTop

`packages/core/components/atoms/ScrollToTop.js` · **Tier:** atom · **Module:** core · **Rendering:** Client Component (`'use client'`)

## Purpose

An invisible client component that listens for pathname changes and immediately
scrolls the document to the top. This is needed because the Next.js App Router does
not reliably restore scroll to `0,0` on every client-side navigation, which can
leave the page title/description partially hidden — especially on mobile/tablet
where there is no side-navigation forcing a full re-render. It renders nothing.

## Props

None.

## Behaviour

On every `usePathname()` change it calls
`window.scrollTo({ top: 0, left: 0, behavior: 'instant' })`. `'instant'` avoids any
animation, so it does not conflict with `prefers-reduced-motion`.

## Composition

- **Uses:** `usePathname` (`next/navigation`), `useEffect`.
- **Used by:** mounted once inside `templates/core/AppShell` so it is always active.

## Internationalisation

None.

## Accessibility

Uses an instant (non-animated) scroll, respecting reduced-motion preferences.

## Usage

```jsx
// Placed once in the app scaffold
<ScrollToTop />
```

## Related

`templates/core/AppShell` (its mount point).
