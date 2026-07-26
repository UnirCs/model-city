---
title: Back-end
sidebar_label: Overview
sidebar_position: 1
---

# Back-end documentation

The Model City back-end is a **Spring Boot (Java)** platform built from shared
domain code and shipped in two topologies from the same source:

- **Microservices** — a Eureka service registry, an API gateway and one Spring
  Boot app per vertical (`core`, `engagement`, `leisure`, `mobility`).
- **Monolith** — the same verticals assembled into a single deployable.

A concrete city's back-end is scaffolded with one of the
[Maven archetypes](../how-to-start/scaffolding.md#2-back-end-maven-archetypes).

This documentation has three subsections.

## [Architecture](./architecture/)

How the back-end is put together: the dual microservices/monolith topology, the shared
domain-library split, the data model, the Valkey cache, internationalization,
observability, the extensibility seams and the audit trails. Every page was written
against the current source and is verified against it. Start with the
[Architecture overview](./architecture/).

## [Modules & REST API](./modules/)

The per-module REST reference — for each operation: description, inputs, outputs and the
sequence diagrams for both topologies. This subsection is being built next.

## [Extensibility Guide](./extensibility-guide/)

How a city customises the platform **without editing platform code**: the one override
mechanism (the `@ModelCityDisabledIfInherited` seam), then a library of worked examples —
adding fields to the data model, overriding and adding use cases, controllers, endpoints
and persistence adapters, standing up a whole new entity, and tuning the YAML configuration
(including disabling the cache for a single use case). Start with the
[Extensibility overview](./extensibility-guide/index.md).
