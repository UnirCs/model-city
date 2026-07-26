---
title: Add a city-local component
sidebar_label: Add a city-local component
sidebar_position: 3
---

# Add a city-local component

**Goal:** create a brand-new component that upstream does not ship (e.g. a city hero
or a custom event tile) and use it from your overrides.

- **New file:** `overrides/leisure/components/molecules/AranjuezEventTile.js`
- **Regen needed:** no — components under `components/` never get a shim.

A city-local component is just a new file under the mirrored `components/` path. It
is imported through the normal `@modelcity/<id>/components/…` specifier; the
`jsconfig` fallback resolves it from `overrides/` because it exists there and nowhere
in `packages/`.

## Recipe

```js
// overrides/leisure/components/molecules/AranjuezEventTile.js
import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import { formatEventDateTime } from '@modelcity/leisure/lib/utils/format';

export default function AranjuezEventTile({ event, lang, labels }) {
  return (
    <Link href={`/${lang}/events/${event.id}`} className="block break-inside-avoid">
      <article className="rounded-md border border-outline-variant overflow-hidden bg-surface-container-lowest hover:shadow-md transition">
        {event.photoUrl && (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={event.photoUrl} alt={event.name} className="w-full object-cover" loading="lazy" />
        )}
        <div className="p-md">
          <h3 className="text-body-md text-primary font-semibold">{event.name}</h3>
          <p className="text-caption text-on-surface-variant flex items-center gap-xs mt-xs">
            <Icon name="schedule" size={13} />
            {formatEventDateTime(event.startsAt, lang)}
          </p>
        </div>
      </article>
    </Link>
  );
}
```

Then import it anywhere via `@modelcity/leisure/components/molecules/AranjuezEventTile`
— for example from your overridden [`EventsList`](./replace-an-organism.md).

:::caution[Not under `routes/`]

Never place a city-local component under a `routes/` directory — the codegen would
emit a route shim for it. Components belong under `components/`.

:::

## Verify

`npm run dev`; the component renders wherever you imported it. No `gen:modules` run is
needed.
