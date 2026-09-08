---
name: model-city-getting-started
description: Use this skill when the user wants to stand up a new Model City deployment from scratch — scaffolding the front-end and back-end from the platform's published artifacts, bringing up local PostgreSQL/Valkey, wiring Auth0/Stripe/Gmail, optionally testing local mTLS, and optionally deploying to AWS. Trigger on requests like "set up a new Model City for <town>", "scaffold my city", "run the Model City getting-started guide", or "help me configure Auth0/Stripe/Gmail for Model City".
---

# Model City — AI-assisted getting started

You are guiding a developer through standing up their own **Model City** deployment.
Model City is generated, never forked: the back-end comes from a Maven archetype and
the front-end from an npm generator, both published by the platform maintainers
(group `io.github.unircs`, npm scope `@modelcity/*`).

This skill is the router for a 7-stage flow. Each stage has a `reference/<stage>.md`
file in this same skill directory with the exact commands, tables and dashboard
click-paths — **read the relevant reference file with the Read tool right before you
start that stage**, don't try to hold all of them in context at once.

```
reference/scaffolding.md      reference/auth0.md      reference/mtls-local.md
reference/local-services.md   reference/stripe.md      reference/aws-deployment.md
                                reference/gmail.md
```

## Ground rules

- **Never guess a version.** Archetype and package versions change over time; always
  resolve them live (stage 0) instead of trusting a number you remember or one written
  in a doc you've seen before.
- **Never commit secrets.** Everything collected in stages 3–5 (Auth0/Stripe/Gmail
  values) is written to `.env` / `.env.local` files that must stay out of git. Check
  `.gitignore` covers them; if not, add them before writing any secret to disk.
- **You cannot click through Auth0/Stripe/Gmail dashboards yourself.** For those
  stages your job is to give the human one numbered instruction at a time, wait for
  them to report back the resulting value (or paste a screenshot), and only then move
  to the next instruction. Don't dump the whole reference file on them at once.
- **Gate anything that costs money or is hard to reverse.** Stage 6 (AWS) creates
  billed cloud resources and IAM identities. Confirm explicitly with the user before
  running any `aws`, `terraform apply`, or `terraform destroy` command — one
  confirmation per destructive/billing action, not one blanket confirmation up front.
- **Use `AskUserQuestion`** (if available in your tool set) for the multiple-choice
  decisions in stage 0; otherwise just ask in chat and wait for the answer.
- Prefer running commands yourself (Bash) over telling the user to run them, except
  where a step is inherently manual (browser dashboards, installing a root CA in the
  OS trust store).

## Stage 0 — Scope the work

Ask the user (don't assume defaults):

1. **Topology**: microservices or monolith back-end?
2. **City identity**: `cityName`, a short `cityKey`, Maven `groupId`/`artifactId`, and
   the front-end app/directory name.
3. **Feature modules**: which of `leisure`, `engagement`, `mobility` are contracted
   (`core` is always included)?
4. **Scope of this session**: just scaffolding + local services, or also the Auth0 /
   Stripe / Gmail integrations, local mTLS, and/or an AWS deployment? Later stages are
   independent — the user can stop after any stage and resume later by re-invoking
   this skill.

Record the answers; you'll reuse them across every stage below.

## Stage 1 — Resolve current versions live

Before generating anything, resolve the actual latest published versions — do not
reuse a version number from memory or from a previously-read doc, since both the
archetypes and the npm packages ship independently of this skill.

```bash
# Back-end archetypes (same version for both topologies)
curl -s https://repo1.maven.org/maven2/io/github/unircs/model-city-back-end-microservices-archetype/maven-metadata.xml | grep -m1 '<release>'
curl -s https://repo1.maven.org/maven2/io/github/unircs/model-city-back-end-monolith-archetype/maven-metadata.xml | grep -m1 '<release>'

# Front-end generator and platform packages
npm view create-model-city-app version
npm view @modelcity/core version
```

Use the `<release>` value as `-DarchetypeVersion` and as `-DmodelcityVersion` (unless
the user wants a `-SNAPSHOT` for platform development), and the npm version to sanity
check what `npm create model-city-app@latest` will pull.

## Stage 2 — Scaffold the code base

Read `reference/scaffolding.md`, then generate both projects non-interactively using
the stage-0 answers and stage-1 versions. Confirm both projects build before moving on
(`npm install` for the front-end; `mvn -q -DskipTests package` for the back-end).

## Stage 3 — Local services (PostgreSQL & Valkey)

Read `reference/local-services.md`. Bring up the two Docker containers, create the
per-vertical databases if the topology is microservices, and export/record the
resulting `DB_*` / `VALKEY_*` / `CACHE_ENABLED` variables into the back-end's `.env`.

## Stage 4 — External integrations (Auth0, Stripe, Gmail)

Only run the sub-stages the user asked for in stage 0. For each one:

1. Read the matching reference file (`reference/auth0.md`, `reference/stripe.md`,
   `reference/gmail.md`).
2. Walk the human through it **one dashboard step at a time**, in your own words,
   pausing for their input at each value you need back.
3. As values come in, append them to the back-end `.env` (and the front-end
   `.env.local` for the front-end-facing ones — see each reference file's variable
   table for which side consumes what) instead of holding them only in chat, so the
   user doesn't have to re-type them if the session is interrupted.
4. At the end of each integration, read back the full list of variables you collected
   for that integration so the user can double check nothing is missing, referencing
   the "Summary" section of the corresponding reference file.

## Stage 5 — Local mTLS (optional)

Only if the user wants to test the FNMT client-certificate flow locally. Read
`reference/mtls-local.md`. You can script the certificate generation, the Nginx
config, and the `docker-compose.yml` yourself; the only manual step is trusting the
generated local CA in the OS/browser keychain, which you must ask the user to do and
wait for confirmation before testing the flow end to end.

## Stage 6 — AWS deployment (optional, billed)

Only if the user explicitly wants to deploy now. Read `reference/aws-deployment.md`.

This stage creates **real, billed AWS resources** (VPC, ALB, ECS, RDS, ElastiCache,
IAM users/roles, ECR repositories). Before running anything:

- Confirm the user has an AWS account they intend to spend on, and which topology
  (`deploy.sh` vs `deploy-monolith.sh`) they're deploying — the two stacks share
  resource names and are mutually exclusive.
- Run the read-only checks (`aws sts get-caller-identity`, prerequisite tool versions)
  freely.
- Before any `aws iam create-*`, `aws iam attach-*`, `terraform apply`, or
  `terraform destroy`, state exactly what it will create/destroy and get an explicit
  go-ahead.
- Never write the generated AWS access keys anywhere other than the credentials file
  the user names; never print the secret key back after the one time AWS shows it.

## Wrap-up

At the end of whatever stages were run, summarize: what was generated, where the
`.env` files live, which variables are still missing (if the user skipped an
integration), and the next command to actually run the stack locally.
