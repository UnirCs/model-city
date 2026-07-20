'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import Icon from '@modelcity/core/components/atoms/Icon';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/** OpenFreeMap positron — clean minimal style, no API key required. */
const STYLE_URL = 'https://tiles.openfreemap.org/styles/positron';

const COLOR_PRIMARY   = '#002045';
const COLOR_SECONDARY = '#13696a';

/** Duration of the expand/collapse CSS transition in ms. */
const TRANSITION_MS = 300;

/* ─────────────────────────────────────────────────────────────────────────
 * Shared map factory
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Initialises a MapLibre map in the given container and adds the route
 * polyline and numbered markers. Markers are keyboard-focusable.
 * Returns the map instance.
 *
 * @param {HTMLElement} container
 * @param {Array<{ id: number, name: string, latitude: number, longitude: number }>} places
 * @returns {maplibregl.Map}
 */
function createMap(container, places) {
  const first = places[0];

  const map = new maplibregl.Map({
    container,
    style: STYLE_URL,
    center: [first.longitude, first.latitude],
    zoom: 14,
    attributionControl: false,
  });

  map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-left');
  map.addControl(
    new maplibregl.AttributionControl({ compact: true }),
    'bottom-right',
  );

  map.on('load', () => {
    const coordinates = places.map((p) => [p.longitude, p.latitude]);

    map.addSource('route', {
      type: 'geojson',
      data: { type: 'Feature', geometry: { type: 'LineString', coordinates } },
    });
    map.addLayer({
      id: 'route-line-bg',
      type: 'line',
      source: 'route',
      layout: { 'line-join': 'round', 'line-cap': 'round' },
      paint: { 'line-color': '#ffffff', 'line-width': 5 },
    });
    map.addLayer({
      id: 'route-line',
      type: 'line',
      source: 'route',
      layout: { 'line-join': 'round', 'line-cap': 'round' },
      paint: {
        'line-color': COLOR_SECONDARY,
        'line-width': 3,
        'line-dasharray': [2, 1.5],
      },
    });

    places.forEach((place, idx) => {
      const el = document.createElement('div');
      el.setAttribute('role', 'button');
      el.setAttribute('tabindex', '0');
      el.setAttribute('aria-label', place.name);
      Object.assign(el.style, {
        width: '32px',
        height: '32px',
        background: COLOR_PRIMARY,
        color: '#ffffff',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '11px',
        fontWeight: '700',
        border: '2px solid #ffffff',
        boxShadow: '0 2px 8px rgba(0,32,69,0.35)',
        cursor: 'pointer',
        fontFamily: "'Public Sans', sans-serif",
        letterSpacing: '0.02em',
        userSelect: 'none',
        outline: 'none',
      });
      el.textContent = String(idx + 1).padStart(2, '0');

      // Visible focus ring
      el.addEventListener('focusin', () => {
        el.style.boxShadow = '0 0 0 3px #ffffff, 0 0 0 5px ' + COLOR_PRIMARY;
      });
      el.addEventListener('focusout', () => {
        el.style.boxShadow = '0 2px 8px rgba(0,32,69,0.35)';
      });

      const popup = new maplibregl.Popup({ offset: 20, closeButton: true }).setHTML(
        `<span style="font-family:'Public Sans',sans-serif;font-size:13px;font-weight:600;color:${COLOR_PRIMARY}">${place.name}</span>`,
      );

      const marker = new maplibregl.Marker({ element: el })
        .setLngLat([place.longitude, place.latitude])
        .setPopup(popup)
        .addTo(map);

      // Keyboard: Enter/Space opens the popup
      el.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          marker.togglePopup();
        }
      });
    });

    if (places.length > 1) {
      const bounds = places.reduce(
        (b, p) => b.extend([p.longitude, p.latitude]),
        new maplibregl.LngLatBounds(
          [first.longitude, first.latitude],
          [first.longitude, first.latitude],
        ),
      );
      map.fitBounds(bounds, { padding: 64, maxZoom: 16, duration: 0 });
    }
  });

  return map;
}

/* ─────────────────────────────────────────────────────────────────────────
 * Component
 * ───────────────────────────────────────────────────────────────────── */

/**
 * Molecule: RouteMap (Client Component)
 *
 * Renders a compact interactive map for a city route. Includes:
 *  - Keyboard-focusable numbered markers (Tab + Enter/Space opens popup)
 *  - Zoom +/− via MapLibre NavigationControl (native buttons, keyboard-operable)
 *  - Full-screen overlay with ESC to close
 *
 * The keyboard-accessible alternative to the map is the numbered place list
 * rendered below the map on the route detail page.
 *
 * Must be imported via RouteMapClient (dynamic ssr:false wrapper).
 *
 * @param {{
 *   places: Array<{ id: number, name: string, latitude: number, longitude: number }>,
 *   mapTitle: string,
 *   expandLabel?: string,
 *   closeLabel?: string,
 * }} props
 */
export default function RouteMap({
  places,
  mapTitle,
  expandLabel = 'Ampliar mapa',
  closeLabel  = 'Cerrar mapa',
}) {
  const { dict } = useTranslations();
  const ta = dict?.a11y ?? {};

  const containerRef         = useRef(null);
  const expandedContainerRef = useRef(null);

  const [overlayMounted, setOverlayMounted] = useState(false);
  const [overlayVisible, setOverlayVisible] = useState(false);

  /* ── Small map ────────────────────────────────────────────── */
  useEffect(() => {
    if (!containerRef.current || places.length === 0) return;
    const map = createMap(containerRef.current, places);
    return () => map.remove();
  }, [places]);

  /* ── Open overlay ─────────────────────────────────────────── */
  const open = useCallback(() => {
    setOverlayMounted(true);
    requestAnimationFrame(() =>
      requestAnimationFrame(() => setOverlayVisible(true)),
    );
  }, []);

  /* ── Close overlay ────────────────────────────────────────── */
  const close = useCallback(() => {
    setOverlayVisible(false);
    setTimeout(() => setOverlayMounted(false), TRANSITION_MS);
  }, []);

  /* ── Expanded map instance ────────────────────────────────── */
  useEffect(() => {
    if (!overlayMounted || !expandedContainerRef.current || places.length === 0) return;
    const map = createMap(expandedContainerRef.current, places);
    return () => map.remove();
  }, [overlayMounted, places]);

  /* ── Escape key ───────────────────────────────────────────── */
  useEffect(() => {
    if (!overlayMounted) return;
    const handler = (e) => { if (e.key === 'Escape') close(); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [overlayMounted, close]);

  const btnClass =
    'absolute top-sm right-sm bg-surface-container-lowest/90 backdrop-blur-sm rounded-md p-xs shadow-sm hover:bg-surface-container transition-colors z-10 cursor-pointer';

  return (
    <div className="flex flex-col gap-sm w-full h-full min-h-80">
      {/* ── Map view ──────────────────────────────────────────── */}
      <div className="relative w-full h-full min-h-80">
        <div
          ref={containerRef}
          className="w-full h-full min-h-80 rounded-md overflow-hidden shadow-sm border border-outline-variant"
          aria-label={mapTitle}
          role="application"
        />
        <button
          type="button"
          onClick={open}
          className={btnClass}
          aria-label={expandLabel}
        >
          <Icon name="fullscreen" size={20} className="text-primary" />
        </button>
        <p className="mt-xs text-caption text-on-surface-variant">
          {ta.mapKeyboardHint}
        </p>
      </div>

      {/* ── Full-screen overlay ─────────────────────────────────── */}
      {overlayMounted && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-md"
          role="dialog"
          aria-modal="true"
          aria-label={mapTitle}
          style={{
            transition: `opacity ${TRANSITION_MS}ms ease`,
            opacity: overlayVisible ? 1 : 0,
          }}
        >
          <div
            className="absolute inset-0 bg-primary/60 backdrop-blur-sm"
            onClick={close}
            aria-hidden="true"
          />
          <div
            className="relative w-full max-w-5xl rounded-md overflow-hidden shadow-2xl border border-outline-variant"
            style={{
              height: '80vh',
              transition: `transform ${TRANSITION_MS}ms ease, opacity ${TRANSITION_MS}ms ease`,
              transform: overlayVisible ? 'scale(1)' : 'scale(0.92)',
              opacity:   overlayVisible ? 1 : 0,
            }}
          >
            <div
              ref={expandedContainerRef}
              className="w-full h-full"
              aria-label={mapTitle}
              role="application"
            />
            <button
              type="button"
              onClick={close}
              className={btnClass}
              aria-label={closeLabel}
            >
              <Icon name="fullscreen_exit" size={20} className="text-primary" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
