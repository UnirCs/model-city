import { notFound } from 'next/navigation';
import { isModuleEnabled, MODULES } from '@modelcity/core/lib/config/modules';

/**
 * Route guard for the mobility feature module. Covers every section served by
 * the mobility microservice (parking stays, sanctions, tickets and their
 * sub-pages). When the module is disabled every nested route in the group
 * responds with 404.
 *
 * @param {{ children: React.ReactNode }} props
 */
export default function MobilityModuleLayout({ children }) {
  if (!isModuleEnabled(MODULES.MOBILITY)) notFound();
  return children;
}
