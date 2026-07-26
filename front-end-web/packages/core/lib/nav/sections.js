/**
 * Centralised navigation model.
 *
 * Builds the role-gated list of sections shown in every navigation surface
 * (desktop SideNavBar accordion, tablet SideNavRail, mobile BottomNavBar and
 * Drawer). Keeping the model in one place guarantees that the three
 * breakpoint layouts always expose the same set of entries with the same
 * gating rules.
 *
 * The engine itself is module-agnostic: it builds only the always-on core
 * section (administration) and concatenates whatever sections each feature
 * module contributes through {@link registerNavContributor}. The orchestrator
 * registers those contributors at startup
 * (`src/lib/composition/registerModules.js`), so `core` no longer imports the
 * feature modules. See `docs/architecture/modular-frontend-packages.md`.
 *
 * The section shape is intentionally a superset of what each surface needs:
 *   - `icon`, `label` and `sectionPath`/`sectionPaths` are used for both
 *     the accordion header and the rail / bottom-bar tab.
 *   - `items` is consumed by the accordion and the drawer.
 *   - `rootHref` is the link target for surfaces that don't expand
 *     sub-items (rail and bottom-bar). It points to the first navigable
 *     item of the section so the user lands somewhere meaningful.
 */

import { canAccessAdministration } from '@modelcity/core/lib/auth/roles';
import { isPathEnabled } from '@modelcity/core/lib/config/modules';

/**
 * @typedef {{
 *   sectionPath: string,
 *   sectionPaths?: string[],
 *   icon: string,
 *   label: string,
 *   rootHref: string,
 *   items: Array<{ href: string | null, label: string, icon?: string }>,
 * }} NavSection
 */

/**
 * @typedef {(args: { session: object | null, n: Record<string, string> }) => NavSection[]} NavContributor
 */

/** Feature-module section contributors, in display order of registration. */
const contributors = [];

/**
 * Registers a feature module's navigation contributor. Called once per active
 * module at startup; the registration order is the display order of that
 * module's sections. Idempotent: registering the same contributor function
 * more than once (e.g. the composition root re-running within the same
 * process) is a no-op after the first call.
 *
 * @param {NavContributor} contributor
 */
export function registerNavContributor(contributor) {
  if (contributors.includes(contributor)) return;
  contributors.push(contributor);
}

/**
 * Always-on core sections (no owning feature module). Currently just the
 * staff-only administration section, kept last in the nav.
 *
 * @param {{ session: object | null, n: Record<string, string> }} args
 * @returns {NavSection[]}
 */
function coreNavSections({ session, n }) {
  if (!canAccessAdministration(session)) return [];
  return [
    {
      sectionPath: '/administration',
      icon: 'admin_panel_settings',
      label: n.administration,
      rootHref: '/administration/citizens',
      items: [
        { href: '/administration/citizens', label: n.administrationCitizens, icon: 'groups' },
        { href: '/administration/workers',  label: n.administrationWorkers,  icon: 'badge' },
        { href: '/administration/records',  label: n.administrationRecords,  icon: 'receipt_long' },
      ],
    },
  ];
}

/**
 * Builds the gated list of navigation sections for the current user, composing
 * every registered feature-module contributor with the core sections, then
 * hiding any section whose feature module is disabled (core sections, which
 * have no owning module, always survive).
 *
 * @param {{ session: object | null, n: Record<string, string> }} args
 *   `n` is the `nav` slice of the common dictionary.
 * @returns {NavSection[]}
 */
export function buildNavSections({ session, n }) {
  const sections = [
    ...contributors.flatMap((contribute) => contribute({ session, n })),
    ...coreNavSections({ session, n }),
  ];

  return sections.filter((section) => isPathEnabled(section.sectionPath));
}
