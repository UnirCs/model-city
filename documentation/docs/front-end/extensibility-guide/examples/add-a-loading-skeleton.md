---
title: Add a loading skeleton to a screen
sidebar_label: Add a loading skeleton
sidebar_position: 8
---

# Add a loading skeleton to a screen

**Goal:** give a screen a bespoke loading state. The App Router treats `loading.js`
as a sibling route file, so this is an **add**.

- **New file:** `overrides/leisure/routes/events/loading.js`
- **Regen needed:** **yes** — a new route sibling means a new shim.

## Recipe

Compose the shared skeleton molecules (or your own):

```js
// overrides/leisure/routes/events/loading.js
import PageHeaderSkeleton from '@modelcity/core/components/molecules/skeletons/PageHeaderSkeleton';
import ListSkeleton from '@modelcity/core/components/molecules/skeletons/ListSkeleton';

export default function Loading() {
  return (
    <>
      <PageHeaderSkeleton />
      <ListSkeleton count={6} />
    </>
  );
}
```

`gen:modules` logs `override route (add): leisure/routes/events/loading` and emits the
shim alongside the events page. The App Router shows it during navigation and data
fetching (it wraps the segment in a Suspense boundary — see
[Rendering](../../architecture/rendering.md)).

## Notes

- The shared skeletons live under `@modelcity/core/components/molecules/skeletons/*`
  (`CardSkeleton`, `ListSkeleton`, `FormSkeleton`, `TableSkeleton`, `MapSkeleton`,
  `PageHeaderSkeleton`, `DetailHeaderSkeleton`, `StatusBarSkeleton`).
- A `loading.js` only needs a **default export** — no `dynamic`/metadata.

## Verify

`npm run gen:modules` prints the `add` line; throttle the network in dev and confirm
the skeleton shows while `/{lang}/events` loads.
