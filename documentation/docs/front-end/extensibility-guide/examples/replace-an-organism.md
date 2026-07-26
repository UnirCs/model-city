---
title: Replace a feature organism
sidebar_label: Replace an organism
sidebar_position: 2
---

# Replace a feature organism

**Goal:** redesign a self-contained section — here the leisure
[`EventsList`](../../atomic-design/organisms/leisure/EventsList.md) card grid —
while reusing the upstream route, data fetching and pagination.

- **Override file:** `overrides/leisure/components/organisms/EventsList.js`
- **Regen needed:** no.

No route is involved, so resolution alone does the swap: every importer of
`@modelcity/leisure/components/organisms/EventsList` (the upstream events page and
any other screen) now renders the city version.

## Recipe

Keep the **same default export and props** the upstream page passes
(`lang`, `page`, `eventType`, `paid`, `accessToken`, `canManage`, `t`, `pageHref`),
then compose it however you like — you can still reuse upstream building blocks.

```js
// overrides/leisure/components/organisms/EventsList.js
import { getEvents } from '@modelcity/leisure/lib/api/client';
import SectionPager from '@modelcity/core/components/molecules/SectionPager';
import FetchErrorBanner from '@modelcity/core/components/molecules/FetchErrorBanner';
// A city-local card (see "Add a city-local component"):
import AranjuezEventTile from '@modelcity/leisure/components/molecules/AranjuezEventTile';

export default async function EventsList({ lang, page, eventType, paid, accessToken, canManage, t, pageHref }) {
  const data = await getEvents({ page, eventType, paid }, accessToken);
  if (!data) return <FetchErrorBanner message={t.loadError} />;

  const events = data.content ?? [];
  const totalPages = data.page?.totalPages ?? 1;

  return (
    <section aria-labelledby="events-heading">
      <h2 id="events-heading" className="text-h3 text-primary mb-md">{t.sectionTitle}</h2>
      {/* City-specific masonry layout instead of the upstream 3-col grid */}
      <div className="columns-1 md:columns-2 xl:columns-3 gap-md [&>*]:mb-md">
        {events.map((evt) => (
          <AranjuezEventTile key={evt.id} event={evt} lang={lang} labels={t} />
        ))}
      </div>
      <SectionPager currentPage={page} totalPages={totalPages} hrefBuilder={pageHref}
        labels={{ page: t.page, of: t.of, prev: t.prev, next: t.next }} />
    </section>
  );
}
```

## Notes

- Reusing `getEvents`, `SectionPager` and `FetchErrorBanner` keeps the data/error
  contract identical — you only changed the visual layout.
- If you also need a bespoke loading state, see
  [Add a loading skeleton](./add-a-loading-skeleton.md).

## Verify

`npm run dev`, open `/{lang}/events` and confirm the new grid renders and pagination
still works.
