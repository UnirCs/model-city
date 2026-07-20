'use client';

import dynamic from 'next/dynamic';

/**
 * Client-side entry point for LocationPickerMap.
 * next/dynamic with ssr:false must live in a Client Component.
 * Forms import this wrapper instead of LocationPickerMap directly.
 */
const LocationPickerMapClient = dynamic(
  () => import('@modelcity/core/components/molecules/LocationPickerMap'),
  { ssr: false },
);

export default LocationPickerMapClient;

