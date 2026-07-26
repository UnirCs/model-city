---
title: Override a public (ungated) route
sidebar_label: Override a public route
sidebar_position: 20
---

# Override a public (ungated) route

**Goal:** customise a page that lives **outside** the session gate — here the public
registration screen — by overriding a `routes-public` source.

- **Override file:** `overrides/core/routes-public/register/page.js`
- **Regen needed:** **yes** — it is a route source.

Core owns the public surface (landing, `register`, `help/glossary`, and the `[lang]`
layout/loading) under `routes-public/`, emitted at the bare `[lang]` root **outside**
the `(app)` gate ([Project structure](../../architecture/project-structure.md)).
Overriding one works exactly like any other route.

## Recipe

Keep the route export contract (default export + optional metadata/`dynamic`), and
reuse the upstream form organism so the registration logic is unchanged:

```js
// overrides/core/routes-public/register/page.js
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import RegistrationForm from '@modelcity/core/components/organisms/RegistrationForm';

export const dynamic = 'force-dynamic';

export default async function RegisterPage({ params }) {
  const { lang } = await params;
  const [dict, session] = await Promise.all([getDictionary(lang, 'registration'), auth0.getSession()]);
  return (
    <main id="main" tabIndex={-1} className="max-w-lg mx-auto p-gutter">
      {/* City welcome copy above the upstream form */}
      <h1 className="text-h1 text-primary mb-md">Bienvenido a Aranjuez</h1>
      <RegistrationForm
        email={session?.user?.email ?? ''}
        defaultName={session?.user?.name ?? ''}
        dict={dict.registration}
        lang={lang}
      />
    </main>
  );
}
```

:::caution[Public route names surface in git]

Feature-module route additions land under the already-ignored `(leisure)` /
`(citizen-engagement)` / `(mobility)` groups. A **new public** route name added under
`core/routes-public/` (a name the public ignore-list doesn't cover) will show up in
git — call it out in your city README. **Replacing** an existing public route (like
`register`) is fine; the shim path is already ignored.

:::

## Verify

`npm run gen:modules`; `npm run dev`, visit `/{lang}/register` (unauthenticated) and
confirm the city welcome renders and registration still completes.
