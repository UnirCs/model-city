---
title: AI-assisted setup
sidebar_label: AI-assisted setup
sidebar_position: 9
---

# AI-assisted setup with Claude Code

Every guide in this section can be followed by hand, but the platform also ships a
[Claude Code](https://claude.com/claude-code) **skill** that walks an AI assistant
through the same 7 stages interactively — scaffolding, local services, Auth0,
Stripe, Gmail, local mTLS and AWS deployment — asking you for the values it can't
fetch itself (dashboard clicks) and resolving version numbers live instead of
hardcoding them.

This is optional. If you'd rather do it by hand, start at
[Scaffold the code base](./scaffolding.md) and follow the table on the
[overview page](./index.md).

## What it does and doesn't do

- It **generates the front-end and back-end projects** for you, using the latest
  published archetype/npm versions it looks up at run time.
- It **brings up PostgreSQL and Valkey** in Docker and wires the resulting variables
  into a `.env` file.
- For Auth0, Stripe and Gmail it **cannot click through the dashboards for you** —
  those accounts are yours. It gives you one instruction at a time, waits for the
  value you get back, and writes it to the right `.env` file so you don't have to
  keep track of a dozen variables by hand.
- For local mTLS it can generate the certificates, the Nginx config and the
  `docker-compose.yml`; you still have to trust the local CA in your OS/browser
  yourself.
- For AWS it will **ask for explicit confirmation before every command that creates,
  modifies or destroys billed cloud resources** — it never runs `terraform apply` or
  `terraform destroy` unattended.

You can stop after any stage and resume later; each stage only needs what the
previous one produced (a scaffolded project, a running database, a set of
variables).

## Install the skill

The skill lives in this repository at
[`documentation/static/claude-skills/model-city-getting-started`](https://github.com/UnirCs/model-city/tree/master/documentation/static/claude-skills/model-city-getting-started).
Copy it into your generated project's `.claude/skills/` directory:

```bash
npx degit UnirCs/model-city/documentation/static/claude-skills/model-city-getting-started \
  .claude/skills/model-city-getting-started
```

If you don't want to use `degit`, a sparse checkout works too:

```bash
git clone --depth 1 --filter=blob:none --sparse https://github.com/UnirCs/model-city.git
cd model-city
git sparse-checkout set documentation/static/claude-skills/model-city-getting-started
cp -r documentation/static/claude-skills/model-city-getting-started \
  /path/to/your/project/.claude/skills/
```

## Use it

Open Claude Code in the directory where you want your city's projects to live, and
either describe what you want ("set up a new Model City for Aranjuez") — Claude Code
will pick up the skill from its description — or invoke it by name if your version
of Claude Code supports that. It will ask you for the topology (microservices vs
monolith), the modules you've contracted, and how far you want to go in this session
before doing anything.

:::caution[Secrets stay local]

The skill writes every value it collects (Auth0/Stripe/Gmail secrets, AWS keys) to
`.env` files on your machine and checks they're git-ignored. It never sends them
anywhere else. Still, review what gets written before pushing your generated project
anywhere.

:::
