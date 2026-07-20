/**
 * System-trail catalogue for the Administration → Registros viewer and the
 * per-user activity panels.
 *
 * Each vertical microservice owns its own `GET /system-trails` endpoint and its
 * own event-type table (decentralised topology). The admin therefore queries
 * **one module at a time** — event types from different modules cannot be mixed.
 *
 * This file is the single source of truth for:
 *   - which modules expose a system-trail endpoint and under which gateway
 *     service path (`servicePath`),
 *   - which feature-module toggle controls each one (so disabled modules
 *     disappear from the module selector), and
 *   - the closed list of event types each module emits (used to populate the
 *     optional `eventType` filter).
 *
 * The catalogue mirrors the summary in `docs/SYSTEM-TRAILS-API.md`; wildcard
 * families (e.g. `CITY_PLACE_*`) are expanded to their standard CRUD
 * operations. Event types are exact, technical constants used as the value sent
 * to the API and as the i18n key; their human-readable, localised display text
 * lives in the dictionaries under `admin.events.eventTypeLabels` (the UI falls
 * back to the raw constant when a label is missing).
 *
 * Safe to import from both server and client components: the only runtime
 * dependency is {@link isModuleEnabled}, which reads inlined `NEXT_PUBLIC_*`
 * flags.
 */

import { MODULES, isModuleEnabled } from '@modelcity/core/lib/config/modules';

/**
 * @typedef {{
 *   id: string,            // stable identifier used in the `module` search param
 *   servicePath: string,   // gateway service path (`/{servicePath}/system-trails`)
 *   moduleId: string|null, // owning feature module ({@link MODULES}) or null for always-on core
 *   labelKey: string,      // i18n key under `admin.events.modules`
 *   eventTypes: string[],
 * }} TrailModule
 */

/** @type {TrailModule[]} */
export const TRAIL_MODULES = [
  {
    id: 'core',
    servicePath: 'core',
    moduleId: null,
    labelKey: 'core',
    eventTypes: [
      'USER_REGISTERED',
      'USER_UPDATED',
      'USER_DELETED',
      'USER_STATUS_CHANGED',
      'AGENT_INVITED',
      'OPERATION_AUTHORIZATION_CREATED',
      'OPERATION_AUTHORIZATION_VERIFIED',
      'OPERATION_AUTHORIZATION_BURNT',
    ],
  },
  {
    id: 'citizen-engagement',
    servicePath: 'engagement',
    moduleId: MODULES.ENGAGEMENT,
    labelKey: 'engagement',
    eventTypes: [
      'CIVIC_QUESTION_CREATED',
      'CIVIC_QUESTION_UPDATED',
      'ANSWER_SUBMITTED',
      'SECURITY_ALERT_CREATED',
      'SECURITY_ALERT_DELETED',
    ],
  },
  {
    id: 'leisure',
    servicePath: 'leisure',
    moduleId: MODULES.LEISURE,
    labelKey: 'leisure',
    eventTypes: [
      'CITY_PLACE_CREATED',
      'CITY_PLACE_UPDATED',
      'CITY_PLACE_DELETED',
      'CITY_ROUTE_CREATED',
      'CITY_ROUTE_UPDATED',
      'CITY_ROUTE_DELETED',
      'PUBLIC_SPACE_CREATED',
      'PUBLIC_SPACE_UPDATED',
      'PUBLIC_SPACE_DELETED',
      'RESERVABLE_RESOURCE_CREATED',
      'RESERVABLE_RESOURCE_UPDATED',
      'RESERVABLE_RESOURCE_DELETED',
      'SPACE_RESERVATION_CREATED',
      'SPACE_RESERVATION_DELETED',
      'EVENT_CREATED',
      'EVENT_UPDATED',
      'EVENT_DELETED',
      'EVENT_TICKET_PURCHASED',
      'EVENT_TICKET_CONFIRMED',
      'EVENT_TICKET_CANCELLED',
      'EVENT_TICKET_REFUNDED',
      'EVENT_TICKET_AUTO_REFUNDED',
    ],
  },
  {
    id: 'mobility',
    servicePath: 'mobility',
    moduleId: MODULES.MOBILITY,
    labelKey: 'mobility',
    eventTypes: [
      'CAR_REGISTERED',
      'STREET_RESERVATION_CREATED',
      'STREET_RESERVATION_RENEWED',
      'STREET_RESERVATION_CONFIRMED',
      'STREET_RESERVATION_CANCELLED',
      'SANCTION_ISSUED',
    ],
  },
];

/**
 * Returns the trail modules currently reachable: always-on core plus every
 * feature module whose toggle is enabled.
 *
 * @returns {TrailModule[]}
 */
export function availableTrailModules() {
  return TRAIL_MODULES.filter((m) => m.moduleId === null || isModuleEnabled(m.moduleId));
}

/**
 * Resolves a trail module by its `id`, or `null` when unknown.
 *
 * @param {string|undefined|null} id
 * @returns {TrailModule|null}
 */
export function trailModuleById(id) {
  return TRAIL_MODULES.find((m) => m.id === id) ?? null;
}

/**
 * Whether the given module id exists and is currently enabled.
 *
 * @param {string|undefined|null} id
 * @returns {boolean}
 */
export function isTrailModuleAvailable(id) {
  const m = trailModuleById(id);
  return !!m && (m.moduleId === null || isModuleEnabled(m.moduleId));
}
