# Reference — Auth0

Auth0 plays three roles: OIDC identity provider for the Next.js front-end, JWT issuer
validated by the gateway/monolith as a resource server, and the user/role directory
the *core* vertical administers via the Management API.

Variables produced (walk the human to each value one at a time; don't ask for all of
them up front):

| Variable | Consumed by | Source in Auth0 |
| --- | --- | --- |
| `AUTH0_DOMAIN` | core + frontend | Tenant domain |
| `AUTH0_ISSUER_URI` | monolith (JWT validation) | `https://<domain>/` — **with trailing slash**, must match token `iss` exactly |
| `AUTH0_AUDIENCE` | gateway/monolith + frontend | API *Identifier* |
| `AUTH0_CLIENT_ID` / `AUTH0_CLIENT_SECRET` | frontend (RWA) | Application settings |
| `AUTH0_MGMT_CLIENT_ID` / `AUTH0_MGMT_CLIENT_SECRET` | core (Management API) | M2M app settings |
| `AUTH0_DB_CONNECTION` | core | Database connection name (default `Username-Password-Authentication`) |
| `AUTH0_BACKOFFICE_ROLE_ID` / `AUTH0_OPERATOR_ROLE_ID` / `AUTH0_MOBILITY_AGENT_ROLE_ID` | core | Role IDs (`rol_…`) |
| `AUTH0_SECRET` / `NEXTAUTH_SECRET` | frontend (session encryption) | Generated locally, not from Auth0 |
| `AUTH0_BASE_URL` / `APP_BASE_URL` / `NEXTAUTH_URL` | frontend | Public app URL |

Secrets (`*_CLIENT_SECRET`, `AUTH0_SECRET`) must never be committed. Domain, issuer,
audience and `rol_…` IDs are configuration, not secrets.

## Walk-through (one step at a time with the human)

1. **Account + tenant**: sign up at `https://auth0.com/signup`. Region `EU` for
   Spain, environment *Development*. Land on `https://manage.auth0.com`.
2. **Identify the tenant**: domain prefix shown top-left, e.g. `my-tenant.eu.auth0.com`.
   → `AUTH0_DOMAIN` = that domain (no protocol). `AUTH0_ISSUER_URI` =
   `https://<domain>/` (https + trailing slash).
3. **Create the API**: *Applications → APIs → + Create API*. Name e.g.
   `Model City API`; *Identifier* is a logical URI, e.g.
   `https://model-city.<town>.es` (need not resolve) → `AUTH0_AUDIENCE`. Signing
   algorithm **RS256** (required).
4. **Regular Web Application** for Next.js: *Applications → + Create Application →
   Regular Web Applications*. From its Settings: `AUTH0_CLIENT_ID`,
   `AUTH0_CLIENT_SECRET`. Configure, comma-separated for local + deployed:
   - Allowed Callback URLs: `https://<app-domain>/api/auth/callback, http://localhost:3000/api/auth/callback`
   - Allowed Logout URLs: `https://<app-domain>, http://localhost:3000`
   - Allowed Web Origins: `https://<app-domain>, http://localhost:3000`
   - Advanced Settings → Grant Types: enable `Authorization Code` and `Refresh Token`.
   Generate the session secret yourself: `openssl rand -base64 32` → `AUTH0_SECRET` =
   `NEXTAUTH_SECRET`. `AUTH0_BASE_URL` = `APP_BASE_URL` = `NEXTAUTH_URL` = the app's
   public URL (`http://localhost:3000` locally).
5. **Machine to Machine app** for the back-end: *Applications → + Create Application
   → Machine to Machine*, authorize against the **Auth0 Management API** with scopes
   `read:users create:users update:users delete:users read:roles
   create:role_members read:role_members delete:role_members` (add
   `read:connections` if using invitations). From Settings: `AUTH0_MGMT_CLIENT_ID`,
   `AUTH0_MGMT_CLIENT_SECRET`. `AUTH0_DB_CONNECTION` = the DB connection name,
   default `Username-Password-Authentication`.
6. **Roles**: *User Management → Roles → + Create Role* for Backoffice, Operator,
   Mobility agent. Each role's `rol_…` ID (from its URL/header) →
   `AUTH0_BACKOFFICE_ROLE_ID` / `AUTH0_OPERATOR_ROLE_ID` /
   `AUTH0_MOBILITY_AGENT_ROLE_ID`.
7. **Post-Login Action** (required — Auth0 has no other way to inject token claims):
   *Actions → Triggers → Login / Post Login → + Add Action → Build from scratch*.
   Paste:

   ```javascript
   exports.onExecutePostLogin = async (event, api) => {
   const namespace = "https://model-city.<town>.es/"; // match AUTH0_AUDIENCE's host
   if (event.authorization && event.authorization.roles) {
    api.accessToken.setCustomClaim(`${namespace}roles`, event.authorization.roles);
    api.idToken.setCustomClaim(`${namespace}roles`, event.authorization.roles);
   }

   const socialStrategies = [
   "google-oauth2","facebook","apple","github","linkedin","twitter","windowslive"
   ];

   const isSocialLogin = socialStrategies.includes(event.connection.strategy);

   if (!isSocialLogin) {return;}
   api.accessToken.setCustomClaim(`${namespace}roles`, ["MODEL-CITY-CITIZEN"]);
   api.idToken.setCustomClaim(`${namespace}roles`, ["MODEL-CITY-CITIZEN"]);
   };
   ```

   **Deploy**, then drag it into the Login flow diagram and **Apply**.

## Endpoints (informational, mounted by the SDK in the front-end itself)

`/api/auth/login`, `/api/auth/callback` (must be in Allowed Callback URLs),
`/api/auth/logout`, `/api/auth/me`. The back-end never exposes login routes — it only
validates the `access_token` sent as `Authorization: Bearer`.

## Summary — read this back to the user at the end

`AUTH0_DOMAIN`, `AUTH0_ISSUER_URI`, `AUTH0_AUDIENCE`, `AUTH0_CLIENT_ID`,
`AUTH0_CLIENT_SECRET`, `AUTH0_MGMT_CLIENT_ID`, `AUTH0_MGMT_CLIENT_SECRET`,
`AUTH0_DB_CONNECTION`, `AUTH0_BACKOFFICE_ROLE_ID`, `AUTH0_OPERATOR_ROLE_ID`,
`AUTH0_MOBILITY_AGENT_ROLE_ID`, `AUTH0_SECRET`/`NEXTAUTH_SECRET`,
`AUTH0_BASE_URL`/`APP_BASE_URL`/`NEXTAUTH_URL`, and the deployed Post-Login Action.
