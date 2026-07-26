---
title: Internationalization (i18n)
sidebar_label: i18n
sidebar_position: 5
---

# Internationalization (i18n)

Model City serves **business content** in three languages. The mechanism is identical in
both topologies and lives in `model-city-commons`.

## Supported locales

| Code | Language | Role |
| --- | --- | --- |
| `es` | Spanish | **Default** and **fallback** |
| `en` | English | — |
| `fr` | French | — |

The single source of truth is the `SupportedLocale` enum (`com.modelcity.common.i18n`).
Matching is by language only, so region subtags collapse (`en-US` → `en`). Adding a language
is **just** a new enum value: the database schema does not change (the `locale` column has no
language `CHECK`).

## Translation tables

Each entity with translatable text has a side table `<entity>_translations`:

- The **base row** keeps its text columns as the default-language (`es`) value.
- The translation table stores the other locales, keyed `(<entity>_id, locale)` with a
  `FOREIGN KEY … ON DELETE CASCADE` to the parent.
- On read, each field resolves to the requested language and, if that translation is
  missing, **falls back to `es`** — **per field** (a name may be translated while its
  description is not).

The ten translation tables:

| Database | Base table | Translatable fields |
| --- | --- | --- |
| core | `zones` | `display_name` |
| core | `neighbourhoods` | `display_name` |
| engagement | `civic_questions` | `title`, `description` |
| engagement | `objectives` | `objective` |
| engagement | `security_alerts` | `title`, `description` |
| leisure | `city_places` | `name`, `description`, `address`, `access_info`, `accessibility_info` |
| leisure | `city_routes` | `name`, `description` |
| leisure | `public_spaces` | `name`, `description`, `address` |
| leisure | `reservable_resources` | `name`, `description` |
| leisure | `events` | `name`, `description` |

**Not translated** (the front-end localizes these codes with its own UI i18n): enums like
`category`, `event_type`, `severity`, `resource_type`, `status`; plus coordinates, dates,
prices, currencies and image URLs. `mobility` and OTP have no translatable text.

The JPA mapping exposes an `@ElementCollection Map<String, …I18n>` (keyed by language code)
over the translation table, with `@BatchSize` to avoid N+1 in listings.

## Language negotiation

The request language is resolved from the standard **`Accept-Language`** header:

```
Accept-Language: fr-FR,fr;q=0.9,en;q=0.8   → best supported match (fr)
```

`ModelCityI18nAutoConfiguration` registers an `AcceptHeaderLocaleResolver` restricted to
`{es, en, fr}` with default `es`. If the header is absent or unsupported, it resolves to
`es`. The resolved language is echoed back on the **`Content-Language`** response header via
an interceptor. There is **no** query parameter for the read language — always
`Accept-Language`.

In microservices the gateway forwards `Accept-Language` unchanged, and inter-service
`WebClient`/`CoreClient` calls propagate it with `LocaleExchangeFilter` (mirroring the
correlation id — see [Observability](./observability.md)).

## Reading — the response is already resolved

Read DTOs do **not** change shape: `name`, `description`, `title`… stay plain strings, but
carry the value resolved to the `Accept-Language` (with per-field fallback to `es`).

```http
GET /api/leisure/city-places/1
Accept-Language: en
```
```json
{ "id": 1, "name": "Royal Palace of Aranjuez", "description": "Antigua residencia real…", "category": "MONUMENT" }
```
> `name` comes in English; `description` falls back to Spanish because it has no `en`
> translation yet.

The cache key includes the language, so the English and Spanish versions never mix — see
[Cache](./cache.md).

## Writing (admin) — multi-language payloads

Create/update endpoints (and question `PATCH`) receive translatable fields as
**`language → text` maps**. The `es` entry is **required** (it is the fallback); omitting it
returns `400`.

```json
{
  "name":        { "es": "Palacio Real de Aranjuez", "en": "Royal Palace of Aranjuez", "fr": "Palais Royal d'Aranjuez" },
  "description": { "es": "Antigua residencia real…", "en": "Former royal residence…" },
  "latitude": 40.038333, "longitude": -3.605833, "category": "MONUMENT"
}
```

Languages not included are simply not stored (they resolve via fallback). A write
**invalidates the cache for all languages** of the entity.

### Editing — recover every language

To pre-fill an admin form with all languages (not just the resolved one), add
`?translations=full` to the detail `GET`. The response adds a `translations` object of
`field → { language → value }`. It is available on the detail of places, routes, public
spaces, events and civic questions; for reservable resources it is on the **listing**
(`GET …/resources?translations=full`). In normal responses the `translations` field is
omitted.
