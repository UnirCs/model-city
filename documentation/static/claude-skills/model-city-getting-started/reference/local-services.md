# Reference — Local services (PostgreSQL & Valkey)

Two infrastructure containers: **PostgreSQL** and **Valkey** (Redis-protocol
compatible cache).

| Variable | Default in code | Value used here |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/modelcity[-core/...]` | same |
| `DB_USERNAME` | `root` | `root` |
| `DB_PASSWORD` | `model-city` | `modelcity` (note the code default and this guide's container password differ — use the container's) |
| `CACHE_ENABLED` | `false` | `true` to test the cache |
| `VALKEY_HOST` | `localhost` | `localhost` |
| `VALKEY_PORT` | `6379` | `6379` |
| `VALKEY_USERNAME` / `VALKEY_PASSWORD` | empty | empty |

## PostgreSQL

```bash
docker run -d \
  --name modelcity-postgres \
  -e POSTGRES_USER=root \
  -e POSTGRES_PASSWORD=model-city \
  -e POSTGRES_DB=modelcity \
  -p 5432:5432 \
  -v modelcity-pgdata:/var/lib/postgresql/data \
  postgres:16-alpine

docker exec -it modelcity-postgres psql -U root -d modelcity -c "\l"   # verify
```

**Create the databases the chosen topology expects.** The container already creates
`modelcity` (monolith). Microservices need one database per vertical:

```bash
docker exec -it modelcity-postgres psql -U root -d postgres -c "
  CREATE DATABASE \"modelcity-core\";
  CREATE DATABASE \"modelcity-engagement\";
  CREATE DATABASE \"modelcity-leisure\";
  CREATE DATABASE \"modelcity-mobility\";
"
```

Resulting `DB_URL`: monolith → `jdbc:postgresql://localhost:5432/modelcity`;
microservices → `jdbc:postgresql://localhost:5432/modelcity-<vertical>` per service.

Lifecycle: `docker stop|start modelcity-postgres`, `docker rm -f modelcity-postgres`
(keeps the volume), `docker volume rm modelcity-pgdata` (deletes data — confirm with
the user before running this one).

## Valkey

```bash
docker run -d --name modelcity-valkey -p 6379:6379 valkey/valkey:8-alpine
docker exec -it modelcity-valkey valkey-cli ping   # should reply PONG
```

No user/password. The cache is **disabled by default** (`CACHE_ENABLED=false`); if
disabled, Valkey isn't even needed (Spring won't try to connect and the Redis health
indicator is off). To exercise it:

```bash
export CACHE_ENABLED=true
export VALKEY_HOST=localhost
export VALKEY_PORT=6379
```

## Minimal variables to start a service against both

```bash
export DB_USERNAME=root
export DB_PASSWORD=model-city
export CACHE_ENABLED=true
export VALKEY_HOST=localhost
export VALKEY_PORT=6379
```

Write these into the back-end project's `.env` (create it if it doesn't exist; make
sure it's git-ignored) so they survive across sessions.

Note: with `JPA_DDL_AUTO` at its default (`validate`) the schema must already exist;
for local development export `JPA_DDL_AUTO=update` so Hibernate creates/updates it.
The rest of the secrets (Stripe/Auth0/mail) are still needed to actually start a
service — see stage 4.
