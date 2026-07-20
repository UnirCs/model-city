'use client';

import dynamic from 'next/dynamic';

/**
 * Client-side entry point for RouteMap.
 * next/dynamic with ssr:false must live in a Client Component.
 * Server pages import this wrapper instead of RouteMap directly.
 */
const RouteMapClient = dynamic(
  () => import('@modelcity/leisure/components/molecules/RouteMap'),
  { ssr: false },
);

export default RouteMapClient;


