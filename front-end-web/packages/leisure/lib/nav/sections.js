import { canViewGeneralSections, canViewBookings, isStaffUser } from '@modelcity/core/lib/auth/roles';

/**
 * Leisure module navigation contributor — the leisure (events + sports spaces)
 * and tourism sections. Registered with the core nav engine by the composition
 * root (`src/lib/composition/registerModules.js`).
 *
 * @param {{ session: object | null, n: Record<string, string> }} args
 * @returns {import('@modelcity/core/lib/nav/sections').NavSection[]}
 */
export function leisureNavSections({ session, n }) {
  const showGeneral = canViewGeneralSections(session);
  const showBookings = canViewBookings(session);
  const isStaff = isStaffUser(session);

  const sections = [];

  if (showGeneral || showBookings) {
    sections.push({
      sectionPath: '/events',
      sectionPaths: ['/sports-spaces'],
      icon: 'local_activity',
      label: n.leisure,
      rootHref: showGeneral ? '/events' : '/sports-spaces',
      items: [
        ...(showGeneral
          ? [
              { href: '/events', label: n.leisureEvents, icon: 'event' },
              ...(!isStaff ? [{ href: '/events/my-tickets', label: n.eventsTickets, icon: 'confirmation_number' }] : []),
            ]
          : []),
        ...(showBookings
          ? [{ href: '/sports-spaces', label: n.leisureSports, icon: 'stadium' }]
          : []),
      ],
    });
  }

  if (showGeneral) {
    sections.push({
      sectionPath: '/tourism',
      icon: 'map',
      label: n.tourism,
      rootHref: '/tourism/routes',
      items: [
        { href: '/tourism/routes', label: n.tourismRoutes, icon: 'route' },
        { href: '/tourism/locations', label: n.tourismLocations, icon: 'place' },
      ],
    });
  }

  return sections;
}
