---
title: Add a new entity end-to-end
sidebar_label: Add a new entity
sidebar_position: 12
---

# Add a new entity end-to-end

**Goal:** add a whole new aggregate the platform doesn't have — here **Beaches** under leisure
— with its table, entity, repository, DTOs, use cases and REST endpoints. Everything is
city-owned, so there are **no seams to override and no markers** to add.

- **Seam:** none (new capability).
- **City files:** a migration + a full slice under `com.modelcity.leisure.beaches`.
- **Schema change:** new table (`V2+` migration).
- **Applies to:** both topologies (placement differs — see Notes).

## 1. The migration

```sql
-- V2__create_beaches.sql
CREATE TABLE beaches (
    id          BIGSERIAL      NOT NULL,
    name        VARCHAR(255)   NOT NULL,
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    blue_flag   BOOLEAN        NOT NULL DEFAULT false,
    CONSTRAINT pk_beaches PRIMARY KEY (id)
);
```

## 2. The entity + repository

A city-only aggregate doesn't cross vertical boundaries, so it needs neither the
`Base`/concrete split nor a store port — a single `@Entity` and a Spring Data repository are
enough. (Keep the `@MappedSuperclass` split only if you expect *your own* deployments to extend
it further.)

```java
// repository/model/Beach.java
@Entity
@Table(name = "beaches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Beach {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private Double latitude;
    @Column(nullable = false) private Double longitude;
    @Column(name = "blue_flag", nullable = false) private boolean blueFlag;
}
```

```java
// repository/BeachRepository.java
public interface BeachRepository extends JpaRepository<Beach, Long> {
    Page<Beach> findByBlueFlagTrue(Pageable pageable);
}
```

## 3. The DTOs

```java
// controller/model/BeachDto.java
@Getter @Setter @AllArgsConstructor
public class BeachDto {
    private Long id; private String name; private Double latitude; private Double longitude; private boolean blueFlag;
    public static BeachDto from(Beach b) {
        return new BeachDto(b.getId(), b.getName(), b.getLatitude(), b.getLongitude(), b.isBlueFlag());
    }
}
```

```java
// controller/model/BeachRequestDto.java
@Getter @Setter
public class BeachRequestDto {
    @NotBlank private String name;
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    private boolean blueFlag;
}
```

## 4. The use cases

Plain `@Service` beans — no `@ModelCityExtensionPoint`, nothing to disable.

```java
// usecase/GetBeachesUseCase.java
@Service
@RequiredArgsConstructor
public class GetBeachesUseCase {
    private final BeachRepository repository;
    @Transactional(readOnly = true)
    public Page<BeachDto> execute(boolean onlyBlueFlag, int page) {
        PageRequest pageable = PageRequest.of(page, 20, Sort.by("name").ascending());
        Page<Beach> beaches = onlyBlueFlag ? repository.findByBlueFlagTrue(pageable) : repository.findAll(pageable);
        return beaches.map(BeachDto::from);
    }
}

// usecase/CreateBeachUseCase.java
@Service
@RequiredArgsConstructor
public class CreateBeachUseCase {
    private final BeachRepository repository;
    @Transactional
    public BeachDto execute(BeachRequestDto request) {
        Beach saved = repository.save(Beach.builder()
                .name(request.getName()).latitude(request.getLatitude())
                .longitude(request.getLongitude()).blueFlag(request.isBlueFlag()).build());
        return BeachDto.from(saved);
    }
}
```

## 5. The controller

A concrete `@RestController` — no abstract base needed for a city-only aggregate.

```java
// controller/BeachController.java
@RestController
@RequestMapping("/beaches")
@RequiredArgsConstructor
public class BeachController {

    private final GetBeachesUseCase getBeachesUseCase;
    private final CreateBeachUseCase createBeachUseCase;

    @GetMapping
    public Page<BeachDto> getBeaches(@RequestParam(defaultValue = "false") boolean blueFlag,
                                     @RequestParam(defaultValue = "0") int page) {
        return getBeachesUseCase.execute(blueFlag, page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public BeachDto createBeach(@RequestBody @Valid BeachRequestDto request) {
        return createBeachUseCase.execute(request);
    }
}
```

## Notes

- **Placement.** In **microservices**, add the slice to the vertical service that owns it (a
  leisure feature → `model-city-leisure`, whose app scans `com.modelcity.leisure` and entity-scans
  the same package by default) and put the migration in that service's `db/migration`. In the
  **monolith**, put it anywhere under `com.modelcity` (scanned by `@EntityScan("com.modelcity")`
  and `@EnableJpaRepositories("com.modelcity")`) and the single `db/migration`.
- **Want caching, localisation, audit trails or the view/port split?** Follow the platform
  conventions: `@Cacheable` with a name you also register (see
  [Tune the cache](./tune-the-cache.md)), a `<entity>_translations` side table
  (see [Add a migration](./add-a-database-migration.md)), or the `SystemTrailGenerator`.
- **Cross-vertical data** (referencing `users`, `zones`…) needs the topology-aware `Base`/concrete
  split and `CoreClient` — model it on `EventTicket`. A self-contained aggregate like beaches
  does not.

## Verify

After the migration runs, `POST /beaches` (as admin) creates a beach and `GET /beaches?blueFlag=true`
lists blue-flag beaches — a fully working new section with no platform files touched.
