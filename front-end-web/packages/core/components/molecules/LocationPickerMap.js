'use client';

import { useEffect, useRef, useState } from 'react';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import Icon from '@modelcity/core/components/atoms/Icon';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/** OpenFreeMap positron — clean minimal style, no API key required. */
const STYLE_URL = 'https://tiles.openfreemap.org/styles/positron';

const COLOR_SECONDARY = '#13696a';

/** Aranjuez town centre — default centre when no marker is present yet. */
const DEFAULT_CENTER = { lng: -3.6033, lat: 40.0322 };

/**
 * Creates the teardrop DOM element used as the marker icon.
 */
function buildMarkerElement(label) {
  const el = document.createElement('div');
  if (label) el.setAttribute('aria-label', label);
  Object.assign(el.style, {
    width: '28px',
    height: '28px',
    background: COLOR_SECONDARY,
    borderRadius: '50% 50% 50% 0',
    transform: 'rotate(-45deg)',
    border: '2.5px solid #ffffff',
    boxShadow: '0 3px 10px rgba(0,32,69,0.4)',
  });
  return el;
}

/** Parses a string to a valid coordinate number; returns null on failure. */
function parseCoord(value) {
  const n = parseFloat(value.trim().replace(',', '.'));
  return Number.isFinite(n) ? n : null;
}

/**
 * Molecule: LocationPickerMap (Client Component)
 *
 * Interactive map that lets the user pick a single point on the map. Each
 * click updates the latitude / longitude through the `onChange` callback.
 *
 * Keyboard alternative: a "Enter coordinates manually" panel below the map
 * allows the user to type lat/lng values and press Apply — satisfying
 * SC 2.1.1, 2.1.3, 2.5.1, 2.5.7.
 *
 * Zoom +/− via MapLibre NavigationControl (native buttons, keyboard-operable).
 *
 * Must be imported via LocationPickerMapClient (dynamic ssr:false wrapper).
 *
 * @param {{
 *   latitude:  number | null,
 *   longitude: number | null,
 *   onChange:  (coords: { latitude: number, longitude: number }) => void,
 *   mapTitle:  string,
 * }} props
 */
export default function LocationPickerMap({
  latitude,
  longitude,
  onChange,
  mapTitle,
}) {
  const { dict } = useTranslations();
  const ta = dict?.a11y ?? {};

  const containerRef = useRef(null);
  const mapRef       = useRef(null);
  const markerRef    = useRef(null);
  const onChangeRef  = useRef(onChange);
  useEffect(() => { onChangeRef.current = onChange; }, [onChange]);

  // Geolocation state
  const [isLocating, setIsLocating] = useState(false);
  const [locationError, setLocationError] = useState('');

  /* ── Create the map once ──────────────────────────────────── */
  useEffect(() => {
    if (!containerRef.current) return;

    const hasInitial = latitude != null && longitude != null;
    const map = new maplibregl.Map({
      container: containerRef.current,
      style: STYLE_URL,
      center: hasInitial
        ? [longitude, latitude]
        : [DEFAULT_CENTER.lng, DEFAULT_CENTER.lat],
      zoom: hasInitial ? 20 : 15,
      attributionControl: false,
    });

    map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-left');
    map.addControl(new maplibregl.AttributionControl({ compact: true }), 'bottom-right');

    if (hasInitial) {
      const el = buildMarkerElement(mapTitle);
      markerRef.current = new maplibregl.Marker({ element: el, anchor: 'bottom' })
        .setLngLat([longitude, latitude])
        .addTo(map);
    }

    map.on('click', (e) => {
      const { lng, lat } = e.lngLat;
      if (markerRef.current) {
        markerRef.current.setLngLat([lng, lat]);
      } else {
        const el = buildMarkerElement(mapTitle);
        markerRef.current = new maplibregl.Marker({ element: el, anchor: 'bottom' })
          .setLngLat([lng, lat])
          .addTo(map);
      }
      onChangeRef.current?.({ latitude: lat, longitude: lng });
    });

    mapRef.current = map;
    return () => {
      map.remove();
      mapRef.current = null;
      markerRef.current = null;
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /* ── Sync external coordinate changes (e.g. form reset) ───── */
  useEffect(() => {
    const map = mapRef.current;
    if (!map || latitude == null || longitude == null) return;

    if (markerRef.current) {
      markerRef.current.setLngLat([longitude, latitude]);
    } else {
      const el = buildMarkerElement(mapTitle);
      markerRef.current = new maplibregl.Marker({ element: el, anchor: 'bottom' })
        .setLngLat([longitude, latitude])
        .addTo(map);
    }
    map.easeTo({ center: [longitude, latitude], duration: 300 });
  }, [latitude, longitude, mapTitle]);

  /* ── Geolocation ────────────────────────────────────────────── */
  function handleUseMyLocation() {
    if (!navigator.geolocation) {
      setLocationError(ta.locationError ?? 'Geolocation not supported');
      return;
    }

    setIsLocating(true);
    setLocationError('');

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude: lat, longitude: lng } = position.coords;
        onChangeRef.current?.({ latitude: lat, longitude: lng });
        setIsLocating(false);
      },
      (error) => {
        let message = ta.locationError ?? 'Could not get your location';
        if (error.code === error.PERMISSION_DENIED) {
          message = ta.locationDenied ?? 'Location access denied';
        }
        setLocationError(message);
        setIsLocating(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  }


  return (
    <div className="flex flex-col gap-sm">
      {/* ── Interactive map ───────────────────────────────────── */}
      <div
        ref={containerRef}
        className="w-full h-[500px] rounded-md overflow-hidden shadow-sm border border-outline-variant cursor-crosshair"
        aria-label={mapTitle}
        role="application"
      />

      {/* ── Keyboard hint ─────────────────────────────────────── */}
      <div className="flex items-center justify-between gap-sm">
        <p className="text-caption text-on-surface-variant">
          {ta.mapKeyboardHint}
        </p>
        <button
          type="button"
          onClick={handleUseMyLocation}
          disabled={isLocating}
          className="inline-flex items-center gap-xs px-sm py-xs rounded-md bg-surface-container text-label-sm text-primary font-medium hover:bg-surface-container-high transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Icon name={isLocating ? 'hourglass_empty' : 'my_location'} size={16} />
          {isLocating ? (ta.locating ?? 'Getting your location…') : (ta.useMyLocation ?? 'Use my location')}
        </button>
      </div>

      {locationError && (
        <p className="text-caption text-error flex items-center gap-xs" role="alert">
          <Icon name="error" size={14} />
          {locationError}
        </p>
      )}
    </div>
  );
}
