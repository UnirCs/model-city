import { isStaffUser } from '@modelcity/core/lib/auth/roles';
import { canViewMobilityOps } from '@modelcity/mobility/lib/auth/roles';

/**
 * Mobility module navigation contributor — the mobility section (citizen stays
 * and cars, plus staff ticket/sanction operations). Registered with the core
 * nav engine by the composition root (`src/lib/composition/registerModules.js`).
 *
 * @param {{ session: object | null, n: Record<string, string> }} args
 * @returns {import('@modelcity/core/lib/nav/sections').NavSection[]}
 */
export function mobilityNavSections({ session, n }) {
  const isStaff = isStaffUser(session);
  const canViewMobility = canViewMobilityOps(session);

  // Staff without mobility-ops access have no mobility section.
  if (isStaff && !canViewMobility) return [];

  return [
    {
      sectionPath: '/mobility',
      icon: 'directions_bus',
      label: n.mobility,
      rootHref: canViewMobility ? '/mobility/tickets' : '/mobility/my-stays',
      items: [
        ...(!isStaff
          ? [
              { href: '/mobility/my-stays',     label: n.mobilityMyStays, icon: 'location_on' },
              { href: '/mobility/cars',         label: n.mobilityCars, icon: 'directions_car' },
              { href: '/mobility/my-sanctions', label: n.mobilityMySanctions, icon: 'gavel' },
            ]
          : []),
        ...(canViewMobility
          ? [
              { href: '/mobility/tickets',   label: n.mobilityTickets,   icon: 'admin_panel_settings' },
              { href: '/mobility/sanctions', label: n.mobilitySanctions, icon: 'admin_panel_settings' },
            ]
          : []),
      ],
    },
  ];
}
