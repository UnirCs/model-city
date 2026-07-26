---
title: Auth0 configuration
sidebar_label: Auth0
sidebar_position: 4
---

# Auth0 configuration

This guide describes how to operate Auth0 to obtain **all the authentication
environment variables** consumed by the back-end (gateway/monolith and the *core*
vertical) and the Next.js front-end of Model City.

In Model City, Auth0 plays three roles at once:

- **Identity provider (OIDC)** for the front-end (login/callback/logout via
  `@auth0/nextjs-auth0`).
- **JWT issuer (Authorization Server)** whose token the gateway/monolith validates
  as a *resource server* (RS256 signature, `issuer` and `audience`).
- **User and role directory** that the *core* vertical administers through the
  **Management API**.

Through this guide, the following environment variables will be obtained:

| Variable | Consumed by | Example / format | Source in Auth0 |
| --- | --- | --- | --- |
| `AUTH0_DOMAIN` | core (backend) and frontend | `my-tenant.eu.auth0.com` | Tenant domain |
| `AUTH0_ISSUER_URI` | monolith (JWT validation) | `https://my-tenant.eu.auth0.com/` (trailing `/`) | Tenant issuer |
| `AUTH0_AUDIENCE` | gateway/monolith and frontend | `https://model-city.aranjuez.es` | API Identifier |
| `AUTH0_CLIENT_ID` | frontend (RWA) | `xxxxxxxx…` | Application Client ID |
| `AUTH0_CLIENT_SECRET` | frontend (RWA) | `xxxxxxxx…` (secret) | Application Client Secret |
| `AUTH0_MGMT_CLIENT_ID` | core (Management API) | `xxxxxxxx…` | M2M app Client ID |
| `AUTH0_MGMT_CLIENT_SECRET` | core (Management API) | `xxxxxxxx…` (secret) | M2M app Client Secret |
| `AUTH0_DB_CONNECTION` | core | `Username-Password-Authentication` | Database connection name |
| `AUTH0_BACKOFFICE_ROLE_ID` | core | `rol_…` | Role ID |
| `AUTH0_OPERATOR_ROLE_ID` | core | `rol_…` | Role ID |
| `AUTH0_MOBILITY_AGENT_ROLE_ID` | core | `rol_…` | Role ID |
| `AUTH0_SECRET` / `NEXTAUTH_SECRET` | frontend (session encryption) | 32 bytes base64 | Generated locally |
| `AUTH0_BASE_URL` / `APP_BASE_URL` / `NEXTAUTH_URL` | frontend | `https://<app-domain>` | Public app URL |

:::caution[Secrets]

The secrets (`*_CLIENT_SECRET`, `AUTH0_SECRET`) must never be committed to a
public repository. Domain, issuer, audience and the `rol_…` values are
configuration, not secrets.

:::

---

## 1. Create the account and tenant

1. Go to `https://auth0.com/signup` and register (email or Google/GitHub account).
2. During sign-up, Auth0 creates your **first tenant**. Choose:
   - **Region**: for Spain, `EU` (Europe). The region is embedded in the domain
     (`.eu.auth0.com`).
   - **Tenant name**: a unique identifier, e.g. `my-tenant`. It yields the domain
     `my-tenant.eu.auth0.com`.
   - **Environment**: *Development* for your working environment.
3. After sign-up you land on the **Dashboard** (`https://manage.auth0.com`).

## 2. Identify your tenant

1. The **tenant** is the domain prefix. In `my-tenant.eu.auth0.com`, the tenant is
   `my-tenant` and the region is `eu`.
2. It always appears in the **top-left selector** of the Dashboard (you can have
   several tenants and switch between them there).
3. Under **Settings → General → Settings** you see the *Region*, and the full
   domain at the top.
4. This yields two variables:
   - `AUTH0_DOMAIN` = `my-tenant.eu.auth0.com` (no protocol).
   - `AUTH0_ISSUER_URI` = `https://my-tenant.eu.auth0.com/` — **with `https://` and
     a trailing slash**. It is what the monolith uses in
     `spring.security.oauth2.resourceserver.jwt.issuer-uri`; the microservices
     gateway has an equivalent fixed value. It must match the `iss` of the tokens
     **exactly**, or validation fails.

## 3. Create the API and the audience

The gateway/monolith validates that the JWT carries the expected `audience`. That
`audience` is the *Identifier* of an API defined in Auth0.

1. Go to **Applications → APIs → + Create API**.
2. Fill in:
   - *Name*: e.g. `Model City API`.
   - *Identifier*: a URI that need **not** physically exist; it is just a logical
     identifier. The project uses `https://model-city.aranjuez.es`. **This value is
     `AUTH0_AUDIENCE`** (and `auth0_audience` in `terraform.tfvars`).
   - *Signing Algorithm*: **RS256** (required; the backend downloads the JWKS and
     validates RS256 signatures).
3. Click **Create**.
4. The *Identifier* you set is what clients must send in the `audience` parameter
   when requesting the token, and what the backend validates in the `aud` claim.

## 4. Create the *Regular Web Application* for Next.js

1. Go to **Applications → Applications → + Create Application**.
2. Name: e.g. `Model City Web`.
3. Type: **Regular Web Applications** (not SPA or M2M; the Next.js front-end runs
   the code exchange on the server with `@auth0/nextjs-auth0`).
4. Click **Create** and, if offered, pick **Next.js** to see examples (optional).
5. In the application's **Settings** you'll find:
   - **Domain** → matches `AUTH0_DOMAIN`.
   - **Client ID** → variable `AUTH0_CLIENT_ID`.
   - **Client Secret** (click reveal) → variable `AUTH0_CLIENT_SECRET`.
6. Configure the URLs (*Application URIs* section). For **deployment** (public
   domain, e.g. DuckDNS) and for **local**, include both, comma-separated:
   - **Allowed Callback URLs**:
     ```
     https://<app-domain>/api/auth/callback, http://localhost:3000/api/auth/callback
     ```
   - **Allowed Logout URLs**:
     ```
     https://<app-domain>, http://localhost:3000
     ```
   - **Allowed Web Origins**:
     ```
     https://<app-domain>, http://localhost:3000
     ```
   > The SDK's callback route is `/api/auth/callback`. In `terraform.tfvars` the
   > project documents exactly `https://model-city.duckdns.org/api/auth/callback`.
7. **Grant Types** (*Advanced Settings → Grant Types*): `Authorization Code` and
   `Refresh Token` must be enabled.
8. Click **Save Changes**.
9. Front-end variables derived from this application (see
   `infrastructure/terraform/aws*/ecs.tf`, `frontend` container):
   - `AUTH0_CLIENT_ID`, `AUTH0_CLIENT_SECRET` (from this screen).
   - `AUTH0_DOMAIN` (step 2), `AUTH0_ISSUER_BASE_URL` = `https://<AUTH0_DOMAIN>`.
   - `AUTH0_AUDIENCE` (step 3).
   - `AUTH0_BASE_URL` = `APP_BASE_URL` = `NEXTAUTH_URL` = `https://<app-domain>`.
   - `AUTH0_SECRET` = `NEXTAUTH_SECRET`: session cookie encryption secret,
     **generated locally** (not in Auth0):
     ```bash
     openssl rand -base64 32
     ```

## 5. Create the *Machine to Machine* app for the Back-End

The *core* vertical administers users and roles (create citizen, invite, assign
role) by calling the **Auth0 Management API**. It needs credentials of an
authorized **Machine to Machine** application.

1. Go to **Applications → Applications → + Create Application**.
2. Name: e.g. `Model City Core (Management)`.
3. Type: **Machine to Machine Applications**.
4. In the authorization dialog, select the **Auth0 Management API** and grant at
   least these *scopes*:
   - `read:users`, `create:users`, `update:users`, `delete:users`.
   - `read:roles`, `create:role_members`, `read:role_members`,
     `delete:role_members`.
   - (If using connections/invitations) `read:connections`.
5. Click **Authorize / Create**.
6. In this M2M app's **Settings**:
   - **Client ID** → `AUTH0_MGMT_CLIENT_ID`.
   - **Client Secret** → `AUTH0_MGMT_CLIENT_SECRET`.
7. `AUTH0_DB_CONNECTION`: the name of the database connection where users live.
   Auth0 creates **`Username-Password-Authentication`** by default (see
   **Authentication → Database**), which is exactly the project's default value.
   Only change it if you create your own connection.

## 6. Roles and their IDs

The backend authorization aspect (`ModelCityAccessAspect`) resolves the user's
role through *core*, which in turn works with Auth0 roles identified by their
`rol_…`. You must create the roles and copy their IDs.

1. Go to **User Management → Roles → + Create Role**.
2. Create the roles the project uses (names are free; what matters is mapping the
   ID to the correct variable). The code contemplates these profiles:
   - **Backoffice** → `AUTH0_BACKOFFICE_ROLE_ID`.
   - **Operator** → `AUTH0_OPERATOR_ROLE_ID`.
   - **Mobility agent** → `AUTH0_MOBILITY_AGENT_ROLE_ID`.
3. Open each role: its identifier **`rol_…`** appears in the URL and header. Copy
   it to the matching variable.

## 7. Custom Action for the authentication flow

The project requires a custom **Action** in the *Login* flow. Auth0 does not allow adding token logic without an Action, so you
must create it and hook it into the flow:

1. Go to **Actions → Triggers** (`https://manage.auth0.com/#/actions/flows`).
2. Select the **Login / Post Login** trigger.
3. In the side panel click **+ (Add Action) → Build from scratch**.
4. Name it (e.g. `model-city-post-login`), leave *Trigger* = **Login / Post
   Login** and *Runtime* = the recommended Node version.
5. Paste this code into the editor.

   ```javascript
    exports.onExecutePostLogin = async (event, api) => {
    const namespace = "https://model-city.aranjuez.es/";
    if (event.authorization && event.authorization.roles) {
     api.accessToken.setCustomClaim(`${namespace}roles`, event.authorization.roles);
     api.idToken.setCustomClaim(`${namespace}roles`, event.authorization.roles);
    }
    
    const socialStrategies = [
    "google-oauth2",
    "facebook",
    "apple",
    "github",
    "linkedin",
    "twitter",
    "windowslive"
    ];
    
    const isSocialLogin = socialStrategies.includes(event.connection.strategy);
    
    if (!isSocialLogin) {return;}
    api.accessToken.setCustomClaim(`${namespace}roles`, ["MODEL-CITY-CITIZEN"]);
    api.idToken.setCustomClaim(`${namespace}roles`, ["MODEL-CITY-CITIZEN"]);
    };
   ```
6. Click **Deploy** to publish the version.
7. Back in **Actions → Triggers → Login / Post Login**, **drag your Action** from
   *Custom* into the flow diagram (between the start and *Complete*).
8. Click **Apply**. From then on the Action runs on every login.

## 8. Endpoint values (login, callback, logout)

With `@auth0/nextjs-auth0`, the SDK mounts the auth routes under `/api/auth/*` in
the Next.js front-end itself. These are not Spring back-end endpoints, but Next.js
server endpoints:

| Endpoint | Path (relative to the app domain) | Use |
| --- | --- | --- |
| Login | `/api/auth/login` | Starts the flow (redirects to Auth0) |
| Callback | `/api/auth/callback` | Receives the `code` and creates the session. **Must** be in *Allowed Callback URLs* |
| Logout | `/api/auth/logout` | Ends the session. The destination must be in *Allowed Logout URLs* |
| Profile | `/api/auth/me` | Returns the session user |

1. The **base** of these routes is set by `AUTH0_BASE_URL` / `APP_BASE_URL` (e.g.
   `https://model-city.duckdns.org`), so the effective callback is
   `https://model-city.duckdns.org/api/auth/callback`.
2. **Locally** that base is `http://localhost:3000`, so the callback is
   `http://localhost:3000/api/auth/callback` (remember to add it in step 4.6).
3. The back-end (gateway/monolith) does **not** expose login routes: it only
   **validates** the `access_token` the front-end obtains from Auth0 with the API's
   `audience` (step 3) and forwards it in the `Authorization: Bearer` header.

## 9. Summary

1. `AUTH0_DOMAIN` and `AUTH0_ISSUER_URI` → step 2.
2. `AUTH0_AUDIENCE` → API *Identifier*, step 3.
3. `AUTH0_CLIENT_ID`, `AUTH0_CLIENT_SECRET` → *Regular Web Application*, step 4.
4. `AUTH0_MGMT_CLIENT_ID`, `AUTH0_MGMT_CLIENT_SECRET`, `AUTH0_DB_CONNECTION` → M2M
   app + connection, step 5.
5. `AUTH0_BACKOFFICE_ROLE_ID`, `AUTH0_OPERATOR_ROLE_ID`,
   `AUTH0_MOBILITY_AGENT_ROLE_ID` → roles, step 6.
6. `AUTH0_SECRET` / `NEXTAUTH_SECRET` → `openssl rand -base64 32`, step 4.
7. `AUTH0_BASE_URL` / `APP_BASE_URL` / `NEXTAUTH_URL` → the app's public domain.
   8. Custom Action deployed and hooked into the *Post Login* flow, step 7.
