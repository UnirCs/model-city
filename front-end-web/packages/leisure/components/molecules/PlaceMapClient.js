'use client';

import dynamic from 'next/dynamic';

/**
 * Client-side entry point for PlaceMap.
 * next/dynamic with ssr:false must live in a Client Component.
 * Server pages import this wrapper instead of PlaceMap directly.
 */
const PlaceMapClient = dynamic(
  () => import('@modelcity/leisure/components/molecules/PlaceMap'),
  { ssr: false },
);

export default PlaceMapClient;


