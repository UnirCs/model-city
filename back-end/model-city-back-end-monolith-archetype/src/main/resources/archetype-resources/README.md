# ${cityName} — Model City monolith deployable

Generated from the **Model City** platform archetype (`model-city-back-end-monolith-archetype`,
platform version `${modelcityVersion}`). This project is a city-specific monolith deployable that
depends on the published platform libraries (`io.github.unircs:*-domain`) and customizes them with override
beans — without forking platform code, so you can bump non-MAJOR platform versions and keep your changes.

## What was generated

A single Spring Boot application (`${artifactId}`) bundling every vertical:
- **core** — Users and Auth0 JIT provisioning
- **engagement** — Civic participation (alerts, objectives, questions)
- **leisure** — Events, places, routes, public spaces
- **mobility** — Cars, street reservations, sanctions

Runs on port `8762` by default #[[(`${PORT:8762}`)]]#.

## Runtime configuration (environment variables)

Set these variables in your deployment environment:

| Variable | Description | Value for ${cityName} |
|---|---|---|
| `AUTH0_AUDIENCE` | Auth0 API audience in the JWT `aud` claim | `${auth0Audience}` |
| `MAIL_CITY_NAME` | City name shown in outgoing emails | `${mailCityName}` |
| `MAIL_ADDRESS` | City postal address shown in outgoing emails | `${mailAddress}` |
| `AUTH0_DOMAIN` | Auth0 domain | *(your Auth0 tenant)* |
| `AUTH0_MGMT_CLIENT_ID` | Auth0 management API client ID | *(your client ID)* |
| `AUTH0_MGMT_CLIENT_SECRET` | Auth0 management API client secret | *(your secret)* |
| `DB_URL` | JDBC URL for the monolith's database | *(your PostgreSQL URL)* |
| `MAIL_HOST` / `MAIL_PORT` | SMTP server | *(your mail server)* |
| `STRIPE_SECRET_KEY` | Stripe secret key | *(your key)* |

The generated `application.yml` ships with neutral empty defaults for branding values
#[[(`${AUTH0_AUDIENCE:}`, `${MAIL_CITY_NAME:}`, `${MAIL_ADDRESS:}`)]]# — always set them via env vars.

## How to customize (the extension points)

The platform exposes use cases (single-method interfaces) and controllers (abstract base classes) as
**seams** annotated `@ModelCityExtensionPoint`. To override one, declare your own bean of the seam type
in a scanned package — the platform default backs off automatically (`@ConditionalOnMissingBean`).

See [`com.modelcity.leisure.example.ExampleCityEventController`](src/main/java/com/modelcity/leisure/example/ExampleCityEventController.java)
for a worked example (delete it once you have your own). In short:

```java
@Service
public class MyGetEventsUseCase implements GetEventsUseCase { /* ... */ }   // overrides a use case

@RestController
public class MyEventController extends EventController { /* @Override endpoints */ }  // overrides a controller
```

## Build & run

```bash
mvn clean package          # builds the bootable jar
mvn spring-boot:run        # runs locally
```

## Keeping up with the platform

Bump `modelcity.version` in `pom.xml` to adopt a new platform release. Re-generating from a newer
archetype refreshes the copied boilerplate; your override beans are yours to keep.
