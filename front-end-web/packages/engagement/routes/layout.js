import { notFound } from 'next/navigation';
import { isModuleEnabled, MODULES } from '@modelcity/core/lib/config/modules';

/**
 * Route guard for the citizen-engagement feature module. Covers every section
 * served by the citizen-engagement microservice: `participation`
 * (consultations) and `security` (alerts). When the module is disabled every
 * nested route in the group responds with 404.
 *
 * @param {{ children: React.ReactNode }} props
 */
export default function CitizenEngagementModuleLayout({ children }) {
  if (!isModuleEnabled(MODULES.ENGAGEMENT)) notFound();
  return children;
}
