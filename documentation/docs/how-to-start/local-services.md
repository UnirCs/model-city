---
title: Local services (PostgreSQL & Valkey)
sidebar_label: Local services
sidebar_position: 3
---

# Local services with Docker: PostgreSQL and Valkey

For local development you need two infrastructure services: a **PostgreSQL**
database and a **Valkey** cache (compatible with the Redis protocol). This guide
gives the minimal Docker commands to run them, with the credentials the project
expects.

## Related environment variables

| Variable | Default in code | Value for this guide |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/modelcity[-core/...]` | same |
| `DB_USERNAME` | `root` | `root` |
| `DB_PASSWORD` | `model-city` | **`modelcity`** (as used here) |
| `CACHE_ENABLED` | `false` | `true` to test the cache |
| `VALKEY_HOST` | `localhost` | `localhost` |
| `VALKEY_PORT` | `6379` | `6379` |
| `VALKEY_USERNAME` / `VALKEY_PASSWORD` | empty | empty |

---

## 1. PostgreSQL (user `root`, password `modelcity`)

1. Start the container:

   ```bash
   docker run -d \
     --name modelcity-postgres \
     -e POSTGRES_USER=root \
     -e POSTGRES_PASSWORD=model-city \
     -e POSTGRES_DB=modelcity \
     -p 5432:5432 \
     -v modelcity-pgdata:/var/lib/postgresql/data \
     postgres:16-alpine
   ```

   - `POSTGRES_USER=root` → `DB_USERNAME`.
   - `POSTGRES_PASSWORD=modelcity` → `DB_PASSWORD`.
   - The `modelcity-pgdata` volume persists data across restarts.
   - Uses `postgres:16-alpine`, the same image the deployment's init scripts use.

2. Verify it is up:

   ```bash
   docker exec -it modelcity-postgres psql -U root -d modelcity -c "\l"
   ```

3. **Create the databases each topology expects.** The container already creates
   `modelcity` (used by the **monolith**). The **microservices** topology needs one
   database per vertical:

   ```bash
   docker exec -it modelcity-postgres psql -U root -d postgres -c "
     CREATE DATABASE \"modelcity-core\";
     CREATE DATABASE \"modelcity-engagement\";
     CREATE DATABASE \"modelcity-leisure\";
     CREATE DATABASE \"modelcity-mobility\";
   "
   ```

   > In short: the **monolith** uses a single database (`modelcity`); the
   > **microservices** use `modelcity-core`, `modelcity-engagement`,
   > `modelcity-leisure` and `modelcity-mobility`.

4. Resulting connection string (`DB_URL`):
   - Monolith: `jdbc:postgresql://localhost:5432/modelcity`
   - Core (micro): `jdbc:postgresql://localhost:5432/modelcity-core` (and likewise
     for the rest).

5. Lifecycle management:

   ```bash
   docker stop modelcity-postgres      # stop
   docker start modelcity-postgres     # resume
   docker rm -f modelcity-postgres     # remove (keeps the volume)
   docker volume rm modelcity-pgdata   # delete data permanently
   ```

## 2. Valkey (distributed cache)

1. Start the container:

   ```bash
   docker run -d \
     --name modelcity-valkey \
     -p 6379:6379 \
     valkey/valkey:8-alpine
   ```

   No user or password, consistent with empty `VALKEY_USERNAME`/`VALKEY_PASSWORD`.

2. Check the connection:

   ```bash
   docker exec -it modelcity-valkey valkey-cli ping   # should reply PONG
   ```

3. The cache is **disabled by default** (`CACHE_ENABLED=false`). To test it
   locally, start the services with:

   ```bash
   export CACHE_ENABLED=true
   export VALKEY_HOST=localhost
   export VALKEY_PORT=6379
   ```

   If `CACHE_ENABLED=false`, you don't even need Valkey (Spring does not try to
   connect and the Redis health indicator is disabled).

4. Lifecycle:

   ```bash
   docker stop modelcity-valkey
   docker start modelcity-valkey
   docker rm -f modelcity-valkey
   ```

## 3. Quick combined startup

To bring up both services at once:

```bash
# PostgreSQL
docker run -d --name modelcity-postgres \
  -e POSTGRES_USER=root -e POSTGRES_PASSWORD=model-city -e POSTGRES_DB=modelcity \
  -p 5432:5432 -v modelcity-pgdata:/var/lib/postgresql/data postgres:16-alpine

# Valkey
docker run -d --name modelcity-valkey -p 6379:6379 valkey/valkey:8-alpine
```

And the minimal variables to start a service against them:

```bash
export DB_USERNAME=root
export DB_PASSWORD=model-city
export CACHE_ENABLED=true
export VALKEY_HOST=localhost
export VALKEY_PORT=6379
```

:::note[Remaining secrets and schema]

Starting a service also needs the rest of the secrets (Stripe, Auth0, mail)
described in the [Stripe](./stripe.md), [Auth0](./auth0.md) and [Gmail](./gmail.md)
guides. With `JPA_DDL_AUTO` at its default (`validate`) the schema must already
exist; for development you can `export JPA_DDL_AUTO=update` so Hibernate
creates/updates it.

:::
