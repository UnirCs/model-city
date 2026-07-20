'use client';

import dynamic from 'next/dynamic';

/**
 * Client-side entry point for AlertsMap.
 * next/dynamic with ssr:false must live in a Client Component.
 * Server pages import this wrapper instead of AlertsMap directly.
 */
const AlertsMapClient = dynamic(
  () => import('@modelcity/engagement/components/molecules/AlertsMap'),
  { ssr: false },
);

export default AlertsMapClient;

