import { notFound } from 'next/navigation';
import { isModuleEnabled, MODULES } from '@modelcity/core/lib/config/modules';

/**
 * Route guard for the leisure feature module. Covers every section served by
 * the leisure microservice: `events`, `sports-spaces` and `tourism`
 * (routes, locations). When the module is disabled every nested route in the
 * group responds with 404.
 *
 * @param {{ children: React.ReactNode }} props
 */
export default function LeisureModuleLayout({ children }) {
  if (!isModuleEnabled(MODULES.LEISURE)) notFound();
  return children;
}
