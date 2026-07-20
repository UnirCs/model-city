'use client';

import { useEffect, useRef, useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: CollapsibleHeader
 * A simple collapsible section header with +/- toggle button.
 * No container wrapper - just the header and conditional content rendering.
 * Height changes are animated using explicit pixel height + ResizeObserver.
 *
 * @param {{
 *   icon: string,
 *   iconClassName?: string,
 *   title: string,
 *   defaultOpen?: boolean,
 *   children: React.ReactNode,
 * }} props
 */
export default function CollapsibleHeader({
  icon,
  iconClassName = 'text-on-surface-variant',
  title,
  defaultOpen = false,
  children,
}) {
  const [open, setOpen] = useState(defaultOpen);
  const [contentHeight, setContentHeight] = useState(0);
  const innerRef = useRef(null);

  // Keep contentHeight in sync with the real rendered height of the inner div.
  // ResizeObserver fires on mount AND whenever children change (e.g. pagination).
  useEffect(() => {
    if (!innerRef.current) return;
    const measure = () => setContentHeight(innerRef.current?.scrollHeight ?? 0);
    const observer = new ResizeObserver(measure);
    observer.observe(innerRef.current);
    measure(); // initial read
    return () => observer.disconnect();
  }, []);

  return (
    <>
      {/* ── Collapsible header ── */}
      <div className="flex items-center justify-between mb-md">
        <div className="flex items-center gap-sm">
          <Icon name={icon} fill size={20} className={iconClassName} />
          <h2 className="text-h3 text-primary">{title}</h2>
        </div>
        <button
          type="button"
          onClick={() => setOpen(!open)}
          className="flex items-center justify-center w-8 h-8 rounded-full border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors"
          aria-expanded={open}
        >
          <Icon name={open ? 'remove' : 'add'} size={20} />
        </button>
      </div>

      {/* ── Collapsible content — explicit pixel height for smooth animation ── */}
      <div
        style={{
          height: open ? contentHeight : 0,
          overflow: 'hidden',
          transition: 'height 300ms ease-in-out',
        }}
      >
        <div ref={innerRef}>
          {children}
        </div>
      </div>
    </>
  );
}
