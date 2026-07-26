---
title: Stripe configuration
sidebar_label: Stripe
sidebar_position: 5
---

# Stripe configuration

This guide describes, step by step, how to operate a Stripe account to obtain
**all the environment variables** consumed by the microservices (and the
monolith) of Model City. The goal is not just to create the account, but to know
how to navigate the Stripe dashboard to extract each concrete value.

Through this guide, the following environment variables will be obtained:

| Variable | Consumed by | Format | Source in Stripe |
| --- | --- | --- | --- |
| `STRIPE_SECRET_KEY` | `leisure`, `mobility`, monolith and frontend (server-side) | `sk_test_…` / `sk_live_…` | Environment secret key |
| `STRIPE_PUBLISHABLE_KEY` | Next.js frontend (baked into the bundle at `docker build`, mapped to `NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY`) | `pk_test_…` / `pk_live_…` | Environment publishable key |
| `STRIPE_WEBHOOK_SECRET` | `leisure` (micro) · leisure webhook in the monolith | `whsec_…` | Event webhook secret |
| `STRIPE_RESERVATION_WEBHOOK_SECRET` | mobility webhook in the **monolith** | `whsec_…` | Reservation webhook secret |
| `STRIPE_PARKING_PRODUCT_ID` | Next.js frontend (creates the parking Checkout Session) | `prod_…` | Parking product ID |

:::note[Micro vs monolith webhook secrets]

In **microservices**, `leisure` and `mobility` share `STRIPE_SECRET_KEY` and each
uses its own `STRIPE_WEBHOOK_SECRET`. In the **monolith** there is a single
`STRIPE_SECRET_KEY` and two webhook secrets: `STRIPE_WEBHOOK_SECRET` (leisure) and
`STRIPE_RESERVATION_WEBHOOK_SECRET` (mobility) so the same logic can be used in
both ways.

:::

---

## 1. Create the account

1. Go to `https://dashboard.stripe.com/register`.
2. Enter email, name and password, and verify the confirmation email.
3. Set the account country (it determines the currencies and payment methods
   available. Project's default currency is `eur`).
4. **You don't need to activate the account** (the activation/KYC process with tax
   and bank data) to develop: in **test mode** you have complete keys and webhooks
   without activation. Activation is only required to charge real money in *live*
   mode.
5. On first entry the dashboard opens in **test mode** by default (*Test mode*
   switch, top right). Always work in this mode while developing.

## 2. Working environments (Test vs Live) and *sandbox* accounts

In Stripe an "environment" is not created as in other systems: each account ships
with **two environments** with independent sets of keys and webhooks.

1. **Test mode**: keys `sk_test_…` / `pk_test_…`. Payments use test cards (e.g.
   `4242 4242 4242 4242`, any future date and CVC). No real money.
2. **Live mode**: keys `sk_live_…` / `pk_live_…`. Requires an activated account.
   Moves real money.
3. The **Test mode** switch (top-right corner) toggles between the two. Each mode
   has its **own** products, webhooks and keys: a webhook created in test does not
   exist in live.
4. If you need several isolated environments (e.g. *dev*, *staging*, *demo*)
   without mixing data, create **Sandboxes**: account selector top menu →
   *Sandboxes* → *Create sandbox*. Each sandbox is an independent test account with
   its own set of keys and webhooks. It is the recommended way to have a clean
   environment per deployment.
5. For real production, one account (or a Connect *account*) per city/tenant is
   recommended; Model City is prepared for one account per deployment.

## 3. Getting the secret and publishable keys

1. Make sure you are in the correct mode (Test for development).
2. Go to **Developers → API keys** (`https://dashboard.stripe.com/test/apikeys`).
3. Copy the **Publishable key** (`pk_test_…`): it is public, baked into the
   front-end bundle. It goes into `STRIPE_PUBLISHABLE_KEY`.
4. Under **Secret key** click *Reveal test key* and copy the value (`sk_test_…`).
   It goes into `STRIPE_SECRET_KEY`. **It is secret**: it must only live in the
   back-end (and the front-end server-side), never in the browser.
5. If the secret key ever leaks, use *Roll key* on that same screen to rotate it;
   the previous value is invalidated.
6. In Live mode the steps are identical but the keys start with `pk_live_…` /
   `sk_live_…` and only appear with an activated account.

## 4. Create the webhook and get its secret

The back-ends validate each event's signature with
`Webhook.constructEvent(payload, sigHeader, webhookSecret)`. Therefore each
endpoint needs its `whsec_…` secret. The endpoints are public (they are in the
gateway/monolith `permitAll` list) precisely because they authenticate by
signature, not by JWT.

### 4.1. Exposed endpoints

| Topology | Vertical | Public webhook URL |
| --- | --- | --- |
| Microservices | leisure | `https://<domain>/api/leisure/stripe/webhook` |
| Microservices | mobility | `https://<domain>/api/mobility/stripe/webhook` |
| Monolith | leisure | `https://<domain>/api/leisure/stripe/webhook` |
| Monolith | mobility | `https://<domain>/api/mobility/stripe/webhook` |

### 4.2. Events to subscribe

Both verticals handle exactly these event types (see `StripeWebhookController`):

- `checkout.session.completed` → marks the ticket/reservation as **PAID**.
- `checkout.session.expired` → marks it **CANCELLED**.
- `checkout.session.async_payment_failed` → marks it **CANCELLED**.

### 4.3. Steps to create the webhook

1. Go to **Developers → Webhooks** (`https://dashboard.stripe.com/test/webhooks`).
2. Click **Add endpoint**.
3. In *Endpoint URL* enter the vertical's public URL (table 4.1). It must be
   reachable from the Internet.
4. In *Select events* choose the three events from section 4.2 (or the whole
   `checkout.session.*` family).
5. Click **Add endpoint** to create it.
6. On the newly created endpoint's screen, **Signing secret** section, click
   *Reveal* and copy the `whsec_…` value:
   - In **microservices**: the leisure endpoint's value goes into
     `STRIPE_WEBHOOK_SECRET` of the `leisure` service; the mobility endpoint's
     value goes into `STRIPE_WEBHOOK_SECRET` of the `mobility` service (same
     variable, different value per service).
   - In the **monolith**: leisure goes into `STRIPE_WEBHOOK_SECRET` and mobility
     into `STRIPE_RESERVATION_WEBHOOK_SECRET`.
7. Repeat for the second vertical: **two endpoints** are created (leisure and
   mobility), each with its own secret.

### 4.4. Testing webhooks locally (Stripe CLI)

1. Install the CLI: `brew install stripe/stripe-cli/stripe` (macOS) and run
   `stripe login`.
2. Forward events to your local back-end:
   ```bash
   stripe listen --forward-to http://localhost:8093/stripe/webhook   # leisure (micro)
   stripe listen --forward-to http://localhost:8094/stripe/webhook   # mobility (micro)
   ```
3. The CLI prints an ephemeral `whsec_…` secret: use it as `STRIPE_WEBHOOK_SECRET`
   for the duration of the `stripe listen` session.

## 5. Choosing the `stripe-java` version to match your account's API version

The project pins the dependency in the root BOM (`pom.xml`) and in the archetypes:

```xml
<dependency>
  <groupId>com.stripe</groupId>
  <artifactId>stripe-java</artifactId>
  <version>24.14.0</version>
</dependency>
```

The relationship between the library and the Stripe API, and how to check it:

1. **Each `stripe-java` release pins a specific Stripe API version.** That value is
   in the constant `com.stripe.Stripe.API_VERSION` and is sent in the
   `Stripe-Version` header of every call, regardless of your account's default
   version.
2. **Check your account's API version** under **Developers → Overview** (or
   *Settings → Business → API version*): you'll see the *default API version* (e.g.
   `2024-06-20`) and the version history.
3. **Check the version your library pins**: open the release notes at
   `https://github.com/stripe/stripe-java/releases` for `24.14.0`, or just log the
   value of `com.stripe.Stripe.API_VERSION` at startup. That is the real
   `Stripe-Version` the back-end will use.
4. **Practical rule of choice**: pick a `stripe-java` version whose pinned API is
   **equal to or compatible with** your account's *default API version*. Since the
   app's calls always use the library version, what matters is that:
   - the objects the back-end creates (Product, Price, Checkout Session, Refund)
     exist as-is in that API version, and
   - the **shape of the webhook payloads** you receive matches what the code
     expects (the project uses `checkout.session.*`, stable for many versions).
5. **When you bump the API version in the dashboard** (*Upgrade* button in the
   version history), also bump `stripe-java` to the release whose pinned API
   matches the new one, recompile and **re-test the webhooks**, because payloads
   may change shape.
6. To update in the project, just change the version in the `<dependency>` of the
   root `pom.xml` (BOM) and in the `<stripe.version>` property of the archetypes,
   then run `mvn -q -DskipTests package`.

## 6. Create the parking product

For the mobility services and parking, the **front-end** creates the
  Checkout Session using a **fixed parking product** and returns the
  `checkoutSessionId` to the back-end. That fixed product is what
  `STRIPE_PARKING_PRODUCT_ID` references.

Steps to create the parking product:

1. In the correct mode (Test/Live), go to **Product catalog → Products**
   (`https://dashboard.stripe.com/test/products`).
2. Click **Add product** (or **+ Create product**).
3. Fill in:
   - *Name*: e.g. `Parking Model City` (name shown at checkout).
   - *Description* (optional).
4. Under **Pricing** define the parking price. Since the real amount depends on
   duration, one of these two strategies is recommended:
   - A **recurring price per unit of time**, or
   - A product with a base price where the front-end computes the final amount
     (`unit_amount`) when creating the Session. Currency `EUR`, consistent with the
     back-end's `default-currency: eur`.
5. Click **Save product**.
6. Open the newly created product. Its identifier **`prod_…`** appears at the top.
   Copy it: that is the value of `STRIPE_PARKING_PRODUCT_ID`.


## 7. Summary

At the end you should have, for the chosen **mode** (test or live):

1. `STRIPE_SECRET_KEY` = `sk_…` → **Developers → API keys** (step 3).
2. `STRIPE_PUBLISHABLE_KEY` = `pk_…` → **Developers → API keys** (step 3).
3. `STRIPE_WEBHOOK_SECRET` = `whsec_…` of the **leisure** endpoint (step 4).
4. `STRIPE_RESERVATION_WEBHOOK_SECRET` = `whsec_…` of the **mobility** endpoint
   (monolith only; in micro the mobility service uses `STRIPE_WEBHOOK_SECRET`)
   (step 4).
5. `STRIPE_PARKING_PRODUCT_ID` = `prod_…` of the parking product (step 6).

