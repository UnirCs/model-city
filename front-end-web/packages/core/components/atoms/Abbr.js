/**
 * Atom: Abbr
 *
 * Thin wrapper around the native `<abbr>` element that exposes the
 * expansion of an acronym through the `title` attribute, satisfying
 * WCAG 2.2 SC 3.1.4 (Abbreviations).
 *
 * Renders `children` (visible token) and uses `title` as the spelled-out
 * expansion. Convenience helper: if `children` is omitted, the `term`
 * prop is used as the visible token.
 *
 * Pure presentational — works equally in server and client components.
 *
 * @param {{
 *   title: string,
 *   term?: string,
 *   children?: import('react').ReactNode,
 *   className?: string,
 * }} props
 */
export default function Abbr({ title, term, children, className }) {
  return (
    <abbr title={title} className={className}>
      {children ?? term}
    </abbr>
  );
}

