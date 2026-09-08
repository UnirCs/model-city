# Reference — Stripe

Variables produced:

| Variable | Consumed by | Format | Source |
| --- | --- | --- | --- |
| `STRIPE_SECRET_KEY` | leisure, mobility, monolith, frontend (server-side) | `sk_test_…`/`sk_live_…` | Developers → API keys |
| `STRIPE_PUBLISHABLE_KEY` | Next.js frontend (baked at `docker build` as `NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY`) | `pk_test_…`/`pk_live_…` | Developers → API keys |
| `STRIPE_WEBHOOK_SECRET` | leisure webhook (both topologies) | `whsec_…` | Webhook endpoint |
| `STRIPE_RESERVATION_WEBHOOK_SECRET` | mobility webhook, **monolith only** (in micro, mobility reuses `STRIPE_WEBHOOK_SECRET` on its own service) | `whsec_…` | Webhook endpoint |
| `STRIPE_PARKING_PRODUCT_ID` | Next.js frontend (parking Checkout Session) | `prod_…` | Product catalog |

## Walk-through

1. **Account**: `https://dashboard.stripe.com/register`. Country determines
   currency/payment methods (project default `eur`). **No activation/KYC needed** for
   development — test mode has full keys/webhooks. Dashboard opens in **Test mode**
   by default; stay there while developing.
2. **Keys**: *Developers → API keys*. Publishable key (`pk_test_…`) →
   `STRIPE_PUBLISHABLE_KEY`. Reveal + copy the secret key (`sk_test_…`) →
   `STRIPE_SECRET_KEY` (back-end/server only, never in the browser).
3. **Webhooks**: *Developers → Webhooks → Add endpoint*, one per vertical, endpoint
   URL from the table below, subscribe to `checkout.session.completed`,
   `checkout.session.expired`, `checkout.session.async_payment_failed` (or the whole
   `checkout.session.*` family). After creating, reveal the **Signing secret**
   (`whsec_…`):

   | Topology | Vertical | Public webhook URL | Goes into |
   | --- | --- | --- | --- |
   | Microservices | leisure | `https://<domain>/api/leisure/stripe/webhook` | leisure's `STRIPE_WEBHOOK_SECRET` |
   | Microservices | mobility | `https://<domain>/api/mobility/stripe/webhook` | mobility's `STRIPE_WEBHOOK_SECRET` |
   | Monolith | leisure | `https://<domain>/api/leisure/stripe/webhook` | `STRIPE_WEBHOOK_SECRET` |
   | Monolith | mobility | `https://<domain>/api/mobility/stripe/webhook` | `STRIPE_RESERVATION_WEBHOOK_SECRET` |

   The endpoint URL must be reachable from the Internet — for **local** testing use
   the Stripe CLI instead of a real endpoint:

   ```bash
   brew install stripe/stripe-cli/stripe && stripe login
   stripe listen --forward-to http://localhost:8093/stripe/webhook   # leisure (micro)
   stripe listen --forward-to http://localhost:8094/stripe/webhook   # mobility (micro)
   ```

   The CLI prints an ephemeral `whsec_…` — use it as `STRIPE_WEBHOOK_SECRET` for the
   duration of that `stripe listen` session.

4. **Parking product**: *Product catalog → Products → Add product*. Name e.g.
   `Parking Model City`, currency `EUR`. Either a recurring per-unit-time price, or a
   base price the front-end scales into `unit_amount` at Checkout Session creation —
   ask the user which they want. Open the product, copy its `prod_…` id →
   `STRIPE_PARKING_PRODUCT_ID`.
5. **`stripe-java` version sanity check** (only if the user reports webhook/API
   mismatches, or is about to bump their account's API version): the project pins
   `stripe-java` in the root `pom.xml` BOM and in the archetypes' `<stripe.version>`.
   Each `stripe-java` release pins one Stripe API version
   (`com.stripe.Stripe.API_VERSION`), sent as the `Stripe-Version` header regardless
   of the account's dashboard default. Check the account's default under *Developers
   → Overview*; pick a `stripe-java` version whose pinned API is equal to or
   compatible with it (check release notes at
   `https://github.com/stripe/stripe-java/releases`). After bumping, rebuild
   (`mvn -q -DskipTests package`) and re-test the webhooks — payload shapes can change
   across API versions.

## Summary — read this back to the user at the end

`STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY`, `STRIPE_WEBHOOK_SECRET`,
`STRIPE_RESERVATION_WEBHOOK_SECRET` (monolith only), `STRIPE_PARKING_PRODUCT_ID` — all
for the chosen mode (test/live).
