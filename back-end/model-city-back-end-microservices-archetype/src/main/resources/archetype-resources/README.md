# ${cityName} — Model City microservices deployable

Generated from the **Model City** platform archetype (`model-city-back-end-microservices-archetype`,
platform version `${modelcityVersion}`). This project is a city-specific microservices deployable that
depends on the published platform libraries (`io.github.unircs:*-domain`) and customizes them with override
beans — without forking platform code, so you can bump non-MAJOR platform versions and keep your changes.

## What was generated

An aggregator project with the following modules:

| Module | Port | Role |
|---|---|---|
| `model-city-service-registry` | 8761 | Eureka service registry |
| `model-city-gateway` | 8762 | API gateway — OAuth2 resource server + routing |
| `model-city-core` | 8090 | Users, auth (Auth0 JIT provisioning) |
| `model-city-engagement` | 8091 | Civic participation (alerts, objectives, questions) |
| `model-city-leisure` | 8093 | Events, places, routes, public spaces |
| `model-city-mobility` | 8094 | Cars, street reservations, sanctions |

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
| `DB_URL` | JDBC URL for each service's database | *(per service)* |
| `MAIL_HOST` / `MAIL_PORT` | SMTP server | *(your mail server)* |
| `STRIPE_SECRET_KEY` | Stripe secret key (leisure + mobility) | *(your key)* |

The generated `application.yml` files ship with neutral empty defaults for branding values
#[[(`${AUTH0_AUDIENCE:}`, `${MAIL_CITY_NAME:}`, `${MAIL_ADDRESS:}`)]]# — always set them via env vars.

## How to customize (the extension points)

The platform exposes use cases (single-method interfaces) and controllers (abstract base classes) as
**seams** annotated `@ModelCityExtensionPoint`. To override one, declare your own bean of the seam type
in a scanned package — the platform default backs off automatically (`@ConditionalOnMissingBean`).

See [`com.modelcity.leisure.example.ExampleCityEventController`](model-city-leisure/src/main/java/com/modelcity/leisure/example/ExampleCityEventController.java)
for a worked example (delete it once you have your own). In short:

```java
@Service
public class MyGetEventsUseCase implements GetEventsUseCase { /* ... */ }   // overrides a use case

@RestController
public class MyEventController extends EventController { /* @Override endpoints */ }  // overrides a controller
```

## Build & run

```bash
mvn clean package -DskipTests        # builds all modules
mvn -pl model-city-core spring-boot:run   # runs a specific service
```

## Keeping up with the platform

Bump `modelcity.version` in the root `pom.xml` to adopt a new platform release. Re-generating from a
newer archetype refreshes the copied boilerplate; your override beans are yours to keep.
