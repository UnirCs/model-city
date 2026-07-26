---
title: Replace a screen (route)
sidebar_label: Replace a screen
sidebar_position: 6
---

# Replace a screen (route)

**Goal:** completely redesign a page — here the events screen — with a different
layout and hero, keeping the rest of the module intact.

- **Override file:** `overrides/leisure/routes/events/page.js`
- **Regen needed:** run `npm run gen:modules` after adding/renaming a route file
  (it also runs on `predev`/`prebuild`). A pure *replace* also works through the
  existing shim, but running it is harmless and keeps the log honest.

## Recipe

Keep the route export contract: a **default export** (the page), plus optional
`generateMetadata` and `export const dynamic`.

```js
// overrides/leisure/routes/events/page.js
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';
import EventsList from '@modelcity/leisure/components/organisms/EventsList';
// City-local component under the mirrored components/ path, NOT inside routes/:
import AranjuezEventsHero from '@modelcity/leisure/components/molecules/AranjuezEventsHero';

export const generateMetadata = makeMetadata('nav.eventsAgenda');
export const dynamic = 'force-dynamic';

export default async function EventsPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'leisure');
  return (
    <>
      <AranjuezEventsHero title={dict.leisure.events.bannerTitle} />
      <EventsList lang={lang} /* …city-specific layout/props… */ />
    </>
  );
}
```

`gen:modules` logs `override route (replace): leisure/routes/events/page`; the page
renders at `/{lang}/events`.

## Notes

- The generated shim reads the **winning** file's content, so if your override adds a
  new route export (e.g. `generateStaticParams`) the shim reflects it — re-run the
  codegen after such a change.
- Need a bespoke loading state for this screen? See
  [Add a loading skeleton](./add-a-loading-skeleton.md).

## Verify

`npm run gen:modules` prints the `replace` log line; `npm run dev` renders the new
events page at `/{lang}/events`.
