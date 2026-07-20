/**
 * Atom: Icon
 * Thin wrapper for Material Symbols Outlined.
 * Accepts 'fill' to enable the icon fill.
 */

/**
 * @param {{ name: string, fill?: boolean, size?: number, className?: string }} props
 */
export default function Icon({ name, fill = false, size = 24, className = '' }) {
  return (
    <span
      className={['material-symbols-outlined', className].join(' ')}
      style={{
        fontSize: size,
        fontVariationSettings: `'FILL' ${fill ? 1 : 0}, 'wght' 300, 'GRAD' 0, 'opsz' ${size}`,
      }}
      aria-hidden="true"
    >
      {name}
    </span>
  );
}
