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

/**
 * Sets the label language of all symbol layers in the loaded map style.
 * Falls back to the default OSM `name` property when unavailable.
 */
function setMapLanguage(map, lang) {
  const code = ['es', 'en', 'fr'].includes(lang) ? lang : 'es';
  map.getStyle().layers.forEach((layer) => {
    if (layer.type === 'symbol' && layer.layout?.['text-field']) {
      try {
        map.setLayoutProperty(layer.id, 'text-field', [
          'coalesce',
          ['get', `name:${code}`],
          ['get', 'name'],
        ]);
      } catch {
        // Skip layers that don't support expression overrides.
      }
    }
  });
}

/**
 * Initialises a MapLibre map for a single place, adds navigation controls,
 * a teardrop marker and opens the popup immediately on load.
 *
 * @param {HTMLElement} container
 * @param {{ latitude: number, longitude: number, name: string }} place
 * @param {string} lang
 * @returns {maplibregl.Map}
 */
function createMap(container, place, lang) {
  const map = new maplibregl.Map({
    container,
    style: STYLE_URL,
    center: [place.longitude, place.latitude],
    zoom: 15,
    attributionControl: false,
  });

  // Navigation controls — top-left so they never overlap the expand button
  map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-left');
  map.addControl(new maplibregl.AttributionControl({ compact: true }), 'bottom-right');

  /* ── Teardrop marker — keyboard-focusable ────────────────── */
  const el = document.createElement('div');
  el.setAttribute('role', 'button');
  el.setAttribute('tabindex', '0');
  el.setAttribute('aria-label', place.name);
  Object.assign(el.style, {
    width: '28px',
    height: '28px',
    background: COLOR_SECONDARY,
    borderRadius: '50% 50% 50% 0',
    transform: 'rotate(-45deg)',
    border: '2.5px solid #ffffff',
    boxShadow: '0 3px 10px rgba(0,32,69,0.4)',
  });

  const popup = new maplibregl.Popup({ offset: [0, -10], closeButton: true }).setHTML(
    `<span style="font-family:'Public Sans',sans-serif;font-size:13px;font-weight:600;color:${COLOR_PRIMARY}">${place.name}</span>`,
  );

  const marker = new maplibregl.Marker({ element: el, anchor: 'bottom' })
    .setLngLat([place.longitude, place.latitude])
    .setPopup(popup)
    .addTo(map);

  // Visible focus ring + keyboard popup toggle
  el.addEventListener('focusin', () => {
    el.style.boxShadow = '0 0 0 3px #ffffff, 0 0 0 5px ' + COLOR_SECONDARY;
  });
  el.addEventListener('focusout', () => {
    el.style.boxShadow = '0 3px 10px rgba(0,32,69,0.4)';
  });
  el.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      marker.togglePopup();
    }
  });

  map.on('load', () => {
    setMapLanguage(map, lang);
    marker.togglePopup();
  });

  return map;
}

/**
 * Molecule: PlaceMap (Client Component)
 *
 * Renders a focused map for a single city place with a teardrop marker.
 * An expand button (top-right) opens a full-screen overlay with a blurred
 * backdrop and smooth scale animation. Escape or clicking outside closes it.
 *
 * Navigation controls (+/−) are always top-left so they never overlap
 * the expand/close button.
 *
 * Must be imported via PlaceMapClient (dynamic ssr:false wrapper).
 *
 * @param {{
 *   latitude: number,
 *   longitude: number,
 *   name: string,
 *   mapTitle: string,
 *   lang?: string,
 *   expandLabel?: string,
 *   closeLabel?: string,
 * }} props
 */
export default function PlaceMap({
  latitude,
  longitude,
  name,
  mapTitle,
  lang        = 'es',
  expandLabel = 'Ampliar mapa',
  closeLabel  = 'Cerrar mapa',
}) {
  const { dict } = useTranslations();
  const ta = dict?.a11y ?? {};

  const containerRef         = useRef(null);
  const expandedContainerRef = useRef(null);

  const [overlayMounted, setOverlayMounted] = useState(false);
  const [overlayVisible, setOverlayVisible] = useState(false);

  const place = { latitude, longitude, name };

  /* ── Compact map ──────────────────────────────────────────── */
  useEffect(() => {
    if (!containerRef.current) return;
    const map = createMap(containerRef.current, place, lang);
    return () => map.remove();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [latitude, longitude, name, lang]);

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
    if (!overlayMounted || !expandedContainerRef.current) return;
    const map = createMap(expandedContainerRef.current, place, lang);
    return () => map.remove();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [overlayMounted, latitude, longitude, name, lang]);

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
    <>
      {/* ── Compact map ────────────────────────────────────── */}
      <div className="relative w-full">
        <div
          ref={containerRef}
          className="w-full h-64 rounded-md overflow-hidden shadow-sm border border-outline-variant"
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
      </div>

      {/* ── Keyboard-accessible alternatives ───────────────── */}
      <div className="flex flex-col gap-xs mt-xs">
        {/* Keyboard hint */}
        <p className="text-caption text-on-surface-variant">
          {ta.mapKeyboardHint}
        </p>
      </div>

      {/* ── Full-screen overlay ─────────────────────────────── */}
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
    </>
  );
}
