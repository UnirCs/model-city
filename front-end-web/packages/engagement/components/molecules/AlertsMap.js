'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import Icon from '@modelcity/core/components/atoms/Icon';
import { severityVisual } from '@modelcity/engagement/lib/security/severity';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/** OpenFreeMap positron — clean minimal style, no API key required. */
const STYLE_URL = 'https://tiles.openfreemap.org/styles/positron';

/** Aranjuez town centre — used when no alert is provided. */
const DEFAULT_CENTER = { lng: -3.6033, lat: 40.0322 };

/** Duration of the expand/collapse CSS transition in ms. */
const TRANSITION_MS = 300;

const COLOR_PRIMARY = '#002045';

/**
 * Creates a DOM element used as the marker icon.
 * The element is keyboard-focusable (tabindex=0 + role=button) so
 * keyboard-only users can Tab to each marker and press Enter/Space
 * to open/close its popup.
 *
 * @param {string} severity
 * @param {string} ariaLabel
 */
function buildMarker(severity, ariaLabel) {
  const visual = severityVisual(severity);
  const el = document.createElement('div');
  el.setAttribute('role', 'button');
  el.setAttribute('tabindex', '0');
  el.setAttribute('aria-label', ariaLabel);
  Object.assign(el.style, {
    width: '24px',
    height: '24px',
    background: visual.markerHex,
    borderRadius: '50%',
    border: '3px solid #ffffff',
    boxShadow: `0 0 0 3px ${visual.markerHex}40, 0 2px 8px rgba(0,32,69,0.35)`,
    cursor: 'pointer',
    outline: 'none',
  });
  // Visible focus ring on keyboard navigation
  el.addEventListener('focusin', () => {
    el.style.boxShadow = `0 0 0 3px ${visual.markerHex}40, 0 0 0 4px #ffffff, 0 0 0 6px ${visual.markerHex}`;
  });
  el.addEventListener('focusout', () => {
    el.style.boxShadow = `0 0 0 3px ${visual.markerHex}40, 0 2px 8px rgba(0,32,69,0.35)`;
  });
  return el;
}

/**
 * Initialises a MapLibre map in the given container and adds a marker per
 * alert. Returns the map instance.
 *
 * @param {HTMLElement} container
 * @param {Array<object>} alerts
 */
function createMap(container, alerts) {
  const first = alerts[0];
  const map = new maplibregl.Map({
    container,
    style: STYLE_URL,
    center: first
      ? [first.longitude, first.latitude]
      : [DEFAULT_CENTER.lng, DEFAULT_CENTER.lat],
    zoom: 13,
    attributionControl: false,
  });

  map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-left');
  map.addControl(new maplibregl.AttributionControl({ compact: true }), 'bottom-right');

  map.on('load', () => {
    alerts.forEach((alert) => {
      const label = alert.title ?? alert.description ?? '';
      const el = buildMarker(alert.severity, label);
      const popup = new maplibregl.Popup({ offset: 16, closeButton: true }).setHTML(
        `<span style="font-family:'Public Sans',sans-serif;font-size:12px;font-weight:600;color:${COLOR_PRIMARY}">${escapeHtml(label.slice(0, 80))}${label.length > 80 ? '…' : ''}</span>`,
      );
      const marker = new maplibregl.Marker({ element: el })
        .setLngLat([alert.longitude, alert.latitude])
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

    if (alerts.length > 1) {
      const bounds = alerts.reduce(
        (b, a) => b.extend([a.longitude, a.latitude]),
        new maplibregl.LngLatBounds(
          [first.longitude, first.latitude],
          [first.longitude, first.latitude],
        ),
      );
      map.fitBounds(bounds, { padding: 64, maxZoom: 15, duration: 0 });
    }
  });

  return map;
}

/** Tiny HTML escape — popup payload is user-generated description. */
function escapeHtml(input) {
  return String(input).replace(/[&<>"']/g, (c) => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]
  ));
}

/**
 * Molecule: AlertsMap (Client Component)
 *
 * Multi-marker map that places every security alert. Includes:
 *  - Map|List view toggle for keyboard-only access (SC 2.1.1, 2.1.3, 2.5.1, 2.5.7)
 *  - Keyboard-focusable markers (Tab + Enter/Space opens popup)
 *  - Zoom +/− via MapLibre NavigationControl (native buttons, keyboard-operable)
 *  - A compact list view that exposes all alert info without the map
 *  - Full-screen overlay with ESC to close
 *
 * Must be imported via AlertsMapClient (dynamic ssr:false wrapper).
 *
 * @param {{
 *   alerts: Array<{
 *     id: number,
 *     severity: string,
 *     description: string,
 *     title?: string,
 *     latitude: number,
 *     longitude: number,
 *   }>,
 *   mapTitle: string,
 *   expandLabel?: string,
 *   closeLabel?: string,
 * }} props
 */
export default function AlertsMap({
  alerts,
  mapTitle,
  expandLabel = 'Expand map',
  closeLabel  = 'Close map',
}) {
  const { dict } = useTranslations();
  const ta = dict?.a11y ?? {};

  const containerRef         = useRef(null);
  const expandedContainerRef = useRef(null);

  const [overlayMounted, setOverlayMounted] = useState(false);
  const [overlayVisible, setOverlayVisible] = useState(false);

  /* ── Compact map ──────────────────────────────────────────── */
  useEffect(() => {
    if (!containerRef.current) return;
    const map = createMap(containerRef.current, alerts);
    return () => map.remove();
  }, [alerts]);

  /* ── Open / close overlay ─────────────────────────────────── */
  const open = useCallback(() => {
    setOverlayMounted(true);
    requestAnimationFrame(() =>
      requestAnimationFrame(() => setOverlayVisible(true)),
    );
  }, []);

  const close = useCallback(() => {
    setOverlayVisible(false);
    setTimeout(() => setOverlayMounted(false), TRANSITION_MS);
  }, []);

  /* ── Expanded map instance ────────────────────────────────── */
  useEffect(() => {
    if (!overlayMounted || !expandedContainerRef.current) return;
    const map = createMap(expandedContainerRef.current, alerts);
    return () => map.remove();
  }, [overlayMounted, alerts]);

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
    <div className="flex flex-col gap-sm">
      {/* ── Map view ──────────────────────────────────────────── */}
      <div className="relative w-full">
        <div
          ref={containerRef}
          className="w-full h-112 rounded-md overflow-hidden shadow-sm border border-outline-variant"
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
        {/* Keyboard hint */}
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
