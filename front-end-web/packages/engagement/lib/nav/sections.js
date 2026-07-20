import { canViewGeneralSections } from '@modelcity/core/lib/auth/roles';
import { canManageSecurityAlerts } from '@modelcity/engagement/lib/auth/roles';

/**
 * Citizen-engagement module navigation contributor — the participation
 * (consultations) and security (alerts) sections. Registered with the core nav
 * engine by the composition root (`src/lib/composition/registerModules.js`).
 *
 * @param {{ session: object | null, n: Record<string, string> }} args
 * @returns {import('@modelcity/core/lib/nav/sections').NavSection[]}
 */
export function engagementNavSections({ session, n }) {
  const showGeneral = canViewGeneralSections(session);
  const canManageAlerts = canManageSecurityAlerts(session);

  const sections = [];

  if (showGeneral) {
    sections.push({
      sectionPath: '/participation',
      icon: 'campaign',
      label: n.participation,
      rootHref: '/participation/questions',
      items: [{ href: '/participation/questions', label: n.participationQuestions, icon: 'question_answer' }],
    });
  }

  sections.push({
    sectionPath: '/security',
    icon: 'warning',
    label: n.security,
    rootHref: '/security/alerts',
    items: [
      { href: '/security/alerts', label: n.securityAlerts, icon: 'notifications' },
      ...(canManageAlerts
        ? [{ href: '/security/alerts/manage', label: n.securityManage, icon: 'admin_panel_settings' }]
        : []),
    ],
  });

  return sections;
}
