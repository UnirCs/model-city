---
title: Add a database migration (schema & seed data)
sidebar_label: Add a database migration
sidebar_position: 4
---

# Add a database migration (schema & seed data)

**Goal:** understand the building block every data-model change relies on — the city's own
**Flyway** migrations. A city adds columns, indexes, tables **and** seed data through
additive `V2+` scripts it owns, so a platform upgrade never conflicts.

- **City files:** SQL under `src/main/resources/db/migration`.
- **Applies to:** both topologies (the location differs; see below).

## Ownership: the deployable owns the schema

A generated city **owns no platform persistence source** — but it **does** own its Flyway
migrations. The archetype ships:

- `V1__baseline.sql` — the full platform schema at scaffolding time.
- `V2__<City>_ExampleData.sql` — example seed data (delete or adapt it).

Every city change is a **new, higher-numbered, additive** script. Flyway applies them in
order and records them in `flyway_schema_history`; never edit an already-applied migration.

## Where the migrations live

| Topology | Location |
| --- | --- |
| **Monolith** | `src/main/resources/db/migration` (one history, all verticals) |
| **Microservices** | `model-city-<vertical>/src/main/resources/db/migration` (one history **per** service database) |

In microservices, put a migration in the service that owns the table (an events column goes
in `model-city-leisure`). In the monolith, everything shares the single `modelcity` database.

## Recipe

### A schema migration (add a column)

```sql
-- V3__add_capacity_alert_to_events.sql
ALTER TABLE events
    ADD COLUMN capacity_alert_threshold INT;

CREATE INDEX ix_events_place ON events (place_id);
```

Pair every schema-additive migration with an entity subclass (see
[Add an internal-only field](./add-an-internal-field.md)) — `ddl-auto=validate` (the archetype
default) fails startup if the mapping and schema diverge.

### A data-seed migration (city content)

```sql
-- V4__seed_city_places.sql
INSERT INTO city_places (name, latitude, longitude, description, category)
VALUES ('Palacio Real', 40.0000, -3.6000, 'Royal Palace and gardens.', 'MONUMENT');

-- localizable fields keep the es value on the base row; other locales go in the side table
INSERT INTO city_place_translations (place_id, locale, name, description)
VALUES (currval('city_places_id_seq'), 'en', 'Royal Palace', 'Royal Palace and gardens.');
```

## Notes

- **Additive only.** Adding columns/tables/indexes and inserting rows is safe across upgrades;
  dropping or renaming a platform column is a fork and will break on the next platform bump.
- **Keep new columns nullable or defaulted** so the platform's own inserts (which don't set
  them) still succeed.
- **Localised content** follows the side-translation-table pattern (`<entity>_translations`,
  `es` on the base row) — see [Data model](../../architecture/data-model.md) and
  [Internationalization](../../architecture/internationalization.md).
- Toggle with `FLYWAY_ENABLED` and control the JPA check with `JPA_DDL_AUTO`
  (see [Override a config property](./override-a-config-property.md)).

## Verify

```bash
mvn -q -pl model-city-leisure flyway:info   # lists pending/applied migrations
```

Start the app: Flyway applies the pending scripts and Hibernate `validate` confirms the
entities match the resulting schema.
