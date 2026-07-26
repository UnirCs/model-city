---
title: Add an internal-only field
sidebar_label: Add an internal-only field
sidebar_position: 1
---

# Add an internal-only field

**Goal:** add a column to an existing entity that the city uses **internally** (a query, a
report, a scheduled job) but never accepts or returns on the public API. This is the
lightest kind of data-model extension — no DTO, use case or controller changes.

- **Seam:** the `*Base` `@MappedSuperclass` + the `@NoRepositoryBean` generic repository.
- **City files:** a subclass entity, a bound repository, an additive migration.
- **Schema change:** additive `V2+` Flyway migration.
- **Applies to:** both topologies.

## Why it works

Each aggregate splits its columns into a `<Entity>Base` (`@MappedSuperclass`, all platform
columns) plus a thin concrete `@Entity`, and the repository is generic
(`CityPlaceRepository<T extends CityPlaceBase>`, marked `@NoRepositoryBean`). A city adds its
own `@Entity` extending the base with the extra column and binds the generic repository to
it — the platform-owned columns are never forked.

## Recipe

### 1. The migration

Cities add **additive** `V2+` migrations in their deployable's
`src/main/resources/db/migration`, so adding a column never conflicts with a platform
upgrade.

```sql
-- V2__add_internal_priority_to_city_places.sql
ALTER TABLE city_places
    ADD COLUMN internal_priority INT NOT NULL DEFAULT 0;
```

### 2. The city entity + repository

```java
// com.modelcity.leisure.cityplaces.repository.model.CityPlaceWithPriority
@Entity
@Table(name = "city_places")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class CityPlaceWithPriority extends CityPlaceBase {

    @Column(name = "internal_priority", nullable = false)
    private int internalPriority;
}
```

```java
// com.modelcity.leisure.cityplaces.repository.CityPlaceWithPriorityRepository
public interface CityPlaceWithPriorityRepository extends CityPlaceRepository<CityPlaceWithPriority> {

    List<CityPlaceWithPriority> findByInternalPriorityGreaterThanOrderByInternalPriorityDesc(int min);
}
```

### 3. Use it from your own internal code

```java
@Service
@RequiredArgsConstructor
public class CityPriorityReport {

    private final CityPlaceWithPriorityRepository repository;

    public List<CityPlaceWithPriority> topPriorityPlaces() {
        return repository.findByInternalPriorityGreaterThanOrderByInternalPriorityDesc(0);
    }
}
```

That is all: no `CityPlaceDto`, no use case, no controller change. The public
`GET /city-places` keeps returning the platform shape.

## Notes

- **The platform `CityPlace` entity stays on the classpath** and remains mapped to
  `city_places`. Two entities mapping one table is legal and intended here — the platform
  default store keeps using `CityPlace`, your internal code uses `CityPlaceWithPriority`.
  Both see the same rows; only your entity sees the new column.
- **If the platform write path must populate the column**, this becomes a
  [Replace a persistence adapter](./replace-a-store-adapter.md) job (bind the store to your
  entity). If the column must also appear on the API, see
  [Expose a new field on the API](./expose-a-new-api-field.md).
- Keep the column **nullable or defaulted** so the platform's own inserts (which don't know
  about it) still succeed.

## Verify

```bash
mvn -q -pl <your-app> flyway:migrate   # or start the app with FLYWAY_ENABLED=true
```

`jpa.hibernate.ddl-auto=validate` (the archetype default) makes the app **fail fast on
startup** if the entity and schema disagree — a green start proves the mapping and the
migration line up. Then hit your internal report and confirm `GET /city-places` is
unchanged.
