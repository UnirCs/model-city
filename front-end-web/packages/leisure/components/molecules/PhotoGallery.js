'use client';

import { useCallback, useEffect, useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';

/** Duration of the expand/collapse CSS transition in ms. */
const TRANSITION_MS = 300;

/**
 * Molecule: PhotoGallery (Client Component)
 *
 * Renders a responsive grid of thumbnails. Clicking on a thumbnail opens a
 * full-screen overlay with the photo at full resolution, a blurred backdrop
 * and a close button. Previous / next arrows allow navigating between
 * photos when there is more than one. Escape and clicking outside also close.
 *
 * Visual treatment mirrors the PlaceMap expand overlay so both feel part of
 * the same design language.
 *
 * @param {{
 *   photos:      string[],
 *   alt:         string,
 *   closeLabel?: string,
 *   prevLabel?:  string,
 *   nextLabel?:  string,
 * }} props
 */
export default function PhotoGallery({
  photos,
  alt,
  closeLabel = 'Close',
  prevLabel  = 'Previous photo',
  nextLabel  = 'Next photo',
}) {
  // overlayMounted: controls whether the overlay DOM node exists.
  // overlayVisible: controls the animated-in CSS state.
  const [overlayMounted, setOverlayMounted] = useState(false);
  const [overlayVisible, setOverlayVisible] = useState(false);
  const [activeIdx, setActiveIdx] = useState(0);

  const open = useCallback((idx) => {
    setActiveIdx(idx);
    setOverlayMounted(true);
    requestAnimationFrame(() =>
      requestAnimationFrame(() => setOverlayVisible(true)),
    );
  }, []);

  const close = useCallback(() => {
    setOverlayVisible(false);
    setTimeout(() => setOverlayMounted(false), TRANSITION_MS);
  }, []);

  const goPrev = useCallback(
    () => setActiveIdx((i) => (i - 1 + photos.length) % photos.length),
    [photos.length],
  );
  const goNext = useCallback(
    () => setActiveIdx((i) => (i + 1) % photos.length),
    [photos.length],
  );

  /* ── Keyboard navigation inside the overlay ────────────── */
  useEffect(() => {
    if (!overlayMounted) return;
    const handler = (e) => {
      if (e.key === 'Escape')     close();
      if (e.key === 'ArrowLeft')  goPrev();
      if (e.key === 'ArrowRight') goNext();
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [overlayMounted, close, goPrev, goNext]);

  if (!photos || photos.length === 0) return null;

  const btnClass =
    'absolute bg-surface-container-lowest/90 backdrop-blur-sm rounded-md p-xs shadow-sm hover:bg-surface-container transition-colors z-10 cursor-pointer';

  return (
    <>
      {/* ── Thumbnail grid ──────────────────────────────────── */}
      <div className="grid grid-cols-2 md:grid-cols-3 gap-sm">
        {photos.map((url, idx) => (
          <button
            key={idx}
            type="button"
            onClick={() => open(idx)}
            className="group aspect-4/3 rounded-md overflow-hidden bg-surface-container-high cursor-zoom-in focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            aria-label={`${alt} — ${idx + 1}`}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={url}
              alt={`${alt} — ${idx + 1}`}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              loading="lazy"
            />
          </button>
        ))}
      </div>

      {/* ── Full-screen overlay ─────────────────────────────── */}
      {overlayMounted && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-md"
          role="dialog"
          aria-modal="true"
          aria-label={`${alt} — ${activeIdx + 1}`}
          style={{
            transition: `opacity ${TRANSITION_MS}ms ease`,
            opacity: overlayVisible ? 1 : 0,
          }}
        >
          {/* Blurred backdrop */}
          <div
            className="absolute inset-0 bg-primary/60 backdrop-blur-sm"
            onClick={close}
            aria-hidden="true"
          />

          {/* Photo panel — scale + opacity animation */}
          <div
            className="relative max-w-5xl max-h-[85vh] flex items-center justify-center"
            style={{
              transition: `transform ${TRANSITION_MS}ms ease, opacity ${TRANSITION_MS}ms ease`,
              transform: overlayVisible ? 'scale(1)' : 'scale(0.92)',
              opacity:   overlayVisible ? 1 : 0,
            }}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={photos[activeIdx]}
              alt={`${alt} — ${activeIdx + 1}`}
              className="max-w-full max-h-[85vh] object-contain rounded-md shadow-2xl border border-outline-variant"
            />

            {/* Close */}
            <button
              type="button"
              onClick={close}
              className={`${btnClass} top-sm right-sm`}
              aria-label={closeLabel}
            >
              <Icon name="close" size={20} className="text-primary" />
            </button>

            {/* Prev / next */}
            {photos.length > 1 && (
              <>
                <button
                  type="button"
                  onClick={goPrev}
                  className={`${btnClass} top-1/2 left-sm -translate-y-1/2`}
                  aria-label={prevLabel}
                >
                  <Icon name="chevron_left" size={22} className="text-primary" />
                </button>
                <button
                  type="button"
                  onClick={goNext}
                  className={`${btnClass} top-1/2 right-sm -translate-y-1/2`}
                  aria-label={nextLabel}
                >
                  <Icon name="chevron_right" size={22} className="text-primary" />
                </button>
                <p className="absolute bottom-sm left-1/2 -translate-x-1/2 px-md py-xs rounded-full bg-surface-container-lowest/90 backdrop-blur-sm text-caption text-on-surface tabular-nums">
                  {activeIdx + 1} / {photos.length}
                </p>
              </>
            )}
          </div>
        </div>
      )}
    </>
  );
}


