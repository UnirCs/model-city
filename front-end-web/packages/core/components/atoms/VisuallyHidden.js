/**
 * Atom: VisuallyHidden
 *
 * Renders content that is invisible on screen but exposed to assistive
 * technologies. Use it to add extra context to icon-only buttons, links
 * that share the same visible label ("Read more"), or to provide a
 * unique accessible name when a visual cue would suffice for sighted
 * users.
 *
 * Implementation mirrors the WHATWG-recommended "sr-only" pattern.
 *
 * @example
 *   <button>
 *     <Icon name="delete" />
 *     <VisuallyHidden> {item.title}</VisuallyHidden>
 *   </button>
 *
 * @param {{
 *   as?: keyof JSX.IntrinsicElements,  // default 'span'
 *   children: React.ReactNode,
 * }} props
 */
export default function VisuallyHidden({ as: Tag = 'span', children }) {
  return <Tag className="sr-only">{children}</Tag>;
}

