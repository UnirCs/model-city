/**
 * Atom: SkipLink
 *
 * Visually-hidden navigation link that becomes visible on keyboard focus.
 * Lets keyboard and screen-reader users bypass repetitive blocks (top nav,
 * brand bar) and jump straight to the page's main content.
 *
 * The link target must exist in the page as an element with the same id
 * and (recommended) tabIndex of -1 so it can receive programmatic focus.
 *
 * Renders a plain anchor — the target is an in-document fragment, never a
 * route, so we do not use LocalizedLink.
 *
 * WCAG 2.2 — 2.4.1 Bypass Blocks (Level A).
 *
 * @param {{
 *   href?: string,
 *   children: React.ReactNode,
 *   className?: string,
 * }} props
 */
export default function SkipLink({ href = '#main', children, className = '' }) {
  return (
    <a
      href={href}
      className={[
        'sr-only focus:not-sr-only',
        'focus:fixed focus:top-3 focus:left-3 focus:z-100',
        'focus:px-md focus:py-sm focus:rounded-md',
        'focus:bg-primary focus:text-on-primary focus:font-semibold',
        'focus:outline-2 focus:outline-offset-2 focus:outline-primary',
        'focus:shadow-lg',
        className,
      ].join(' ')}
    >
      {children}
    </a>
  );
}

