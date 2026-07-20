/**
 * Skeleton — base placeholder atom used to compose loading UIs.
 *
 * Server component. Renders a single block with the design-system
 * `surface-container-high` color and Tailwind's built-in `animate-pulse`.
 * It is purely decorative, therefore always `aria-hidden`.
 *
 * @param {object} props
 * @param {string} [props.className]   Extra Tailwind classes (sizing, spacing).
 * @param {string|number} [props.width]   Explicit width (px when number).
 * @param {string|number} [props.height]  Explicit height (px when number).
 * @param {'md'|'full'} [props.rounded='md']  Border-radius token.
 * @param {'rect'|'circle'} [props.variant='rect']  Visual variant.
 * @param {keyof JSX.IntrinsicElements} [props.as='div']  HTML tag.
 */
export default function Skeleton({
  className = '',
  width,
  height,
  rounded = 'md',
  variant = 'rect',
  as: Tag = 'div',
}) {
  const radiusClass =
    variant === 'circle' ? 'rounded-full' : rounded === 'full' ? 'rounded-full' : 'rounded-md';

  const style = {};
  if (width != null) style.width = typeof width === 'number' ? `${width}px` : width;
  if (height != null) style.height = typeof height === 'number' ? `${height}px` : height;

  return (
    <Tag
      aria-hidden="true"
      style={style}
      className={`bg-surface-container-high animate-pulse ${radiusClass} ${className}`}
    />
  );
}

